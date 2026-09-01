import { useEffect, useState } from "react";
import {
  Alert,
  Accordion,
  AccordionDetails,
  AccordionSummary,
  Box,
  Chip,
  CircularProgress,
  Divider,
  Stack,
  Typography,
  alpha,
} from "@mui/material";
import { CheckCircle, ExpandMore, Cancel } from "@mui/icons-material";
import {
  fetchSubmissionDetails,
  type SubmissionDetails as SubmissionDetailsDto,
  type SubmissionState,
  type TestCaseResult,
} from "../../api/submissions";
import { errorMessage } from "../../api/client";
import CodeEditor from "./CodeEditor";

const stateColors: Record<
  SubmissionState,
  "success" | "warning" | "error" | "info" | "default"
> = {
  PENDING: "info",
  COMPILING: "info",
  COMPILE_ERROR: "error",
  RUNNING: "info",
  ACCEPTED: "success",
  WRONG_ANSWER: "error",
  RUNTIME_ERROR: "error",
  TIME_LIMIT_EXCEEDED: "warning",
  MEMORY_LIMIT_EXCEEDED: "warning",
  SYSTEM_ERROR: "error",
};

const activeStates = new Set<SubmissionState>([
  "PENDING",
  "COMPILING",
  "RUNNING",
]);

function output(value: string | null): string {
  return value === null || value === "" ? "—" : value;
}

function ResultField({ label, value }: { label: string; value: string | null }) {
  return (
    <Box sx={{ minWidth: 0 }}>
      <Typography variant="caption" color="text.secondary">
        {label}
      </Typography>
      <Box
        component="pre"
        sx={{
          m: 0,
          mt: 0.25,
          fontSize: "0.8rem",
          lineHeight: 1.35,
          whiteSpace: "pre-wrap",
          overflowWrap: "anywhere",
        }}
      >
        {output(value)}
      </Box>
    </Box>
  );
}

function TestResult({ result, index }: { result: TestCaseResult; index: number }) {
  const passed = result.stdout?.trim() === result.expectedOutput?.trim();
  const color = passed ? "success" : "error";

  return (
    <Accordion
      disableGutters
      elevation={0}
      sx={{
        border: 1,
        borderColor: `${color}.main`,
        bgcolor: (theme) => alpha(theme.palette[color].main, 0.08),
        "&:before": { display: "none" },
      }}
    >
      <AccordionSummary
        expandIcon={<ExpandMore />}
        aria-controls={`test-result-${index}-content`}
        id={`test-result-${index}-header`}
        sx={{ minHeight: 40, "& .MuiAccordionSummary-content": { my: 0.5 } }}
      >
        <Stack direction="row" spacing={1} sx={{ width: "100%", alignItems: "center" }}>
          {passed ? <CheckCircle color="success" fontSize="small" /> : <Cancel color="error" fontSize="small" />}
          <Typography variant="subtitle2">Test case {index + 1}</Typography>
          <Typography variant="caption" color={`${color}.dark`} sx={{ fontWeight: 600 }}>
            {passed ? "Passed" : "Failed"}
          </Typography>
          <Typography variant="caption" color="text.secondary" sx={{ ml: "auto !important" }}>
            {result.executionTimeMs === null ? "—" : `${result.executionTimeMs} ms`}
            {" · "}
            {result.bytesUsed === null ? "—" : `${result.bytesUsed} bytes`}
          </Typography>
        </Stack>
      </AccordionSummary>
      <AccordionDetails
        id={`test-result-${index}-content`}
        sx={{ pt: 0.5, pb: 1, display: "grid", gridTemplateColumns: { xs: "1fr", md: "1fr 1fr" }, columnGap: 1.5, rowGap: 0.75 }}
      >
        <ResultField label="Input" value={result.input} />
        <ResultField label="Expected output" value={result.expectedOutput} />
        <ResultField label="Standard output" value={result.stdout} />
        <ResultField label="Standard error" value={result.stderr} />
      </AccordionDetails>
    </Accordion>
  );
}

interface SubmissionDetailsProps {
  submissionId: string;
  refreshKey: number;
}

export default function SubmissionDetails({
  submissionId,
  refreshKey,
}: SubmissionDetailsProps) {
  const [details, setDetails] = useState<SubmissionDetailsDto | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    setDetails(null);
    setError(null);
    fetchSubmissionDetails(submissionId)
      .then((value) => {
        if (active) setDetails(value);
      })
      .catch((cause: unknown) => {
        if (active) {
          setError(errorMessage(cause, "Unable to load submission details."));
        }
      });
    return () => {
      active = false;
    };
  }, [submissionId, refreshKey]);

  if (details === null && error === null) {
    return (
      <Box sx={{ display: "flex", justifyContent: "center", py: 4 }}>
        <CircularProgress />
      </Box>
    );
  }
  if (error !== null) return <Alert severity="error">{error}</Alert>;
  if (details === null) return null;

  return (
    <Stack spacing={1.25}>
      <Stack direction="row" spacing={1} useFlexGap sx={{ alignItems: "center", flexWrap: "wrap" }}>
        <Chip label={details.state} color={stateColors[details.state]} size="small" />
        <Typography variant="body2">{details.language.toUpperCase()}</Typography>
        <Typography variant="body2" color="text.secondary">
          {new Date(details.createdAt).toLocaleString()}
        </Typography>
      </Stack>

      {details.errorMessage && <Alert severity="error">{details.errorMessage}</Alert>}

      <Box>
        <Typography variant="h6" sx={{ mb: 1 }}>Source code</Typography>
        <CodeEditor
          language={details.language}
          value={details.code}
          readOnly
          copyable
          height={280}
        />
      </Box>

      <Divider />
      <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
        Test results
      </Typography>
      {details.results.length === 0 ? (
        <Alert severity="info">
          {activeStates.has(details.state)
            ? "This submission is still being processed."
            : "No test-case results are available for this submission."}
        </Alert>
      ) : (
        details.results.map((result, index) => (
          <TestResult key={index} result={result} index={index} />
        ))
      )}
    </Stack>
  );
}
