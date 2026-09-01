import {
  Alert,
  Box,
  Chip,
  CircularProgress,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
} from "@mui/material";
import type {
  Submission,
  SubmissionState,
} from "../../api/submissions";

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

function formatDateTime(value: string): string {
  return new Date(value).toLocaleString();
}

function formatLanguage(value: string): string {
  return value.charAt(0).toUpperCase() + value.slice(1);
}

interface SubmissionsListProps {
  submissions: Submission[] | null;
  error: string | null;
  liveUpdatesError?: string | null;
  onSelectSubmission: (submissionId: string) => void;
}

export default function SubmissionsList({
  submissions,
  error,
  liveUpdatesError,
  onSelectSubmission,
}: SubmissionsListProps) {
  if (submissions === null && !error) {
    return (
      <Box sx={{ display: "flex", justifyContent: "center", py: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return <Alert severity="error">{error}</Alert>;
  }

  if (submissions !== null && submissions.length === 0) {
    return (
      <Alert severity="info">
        No submissions yet. Submit a solution to see it here.
      </Alert>
    );
  }

  return (
    <Box>
      {liveUpdatesError && (
        <Alert severity="warning" sx={{ mb: 2 }}>
          {liveUpdatesError}
        </Alert>
      )}
      <TableContainer component={Paper} variant="outlined">
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>State</TableCell>
              <TableCell>Language</TableCell>
              <TableCell>Submitted</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {submissions?.map((submission) => (
              <TableRow
                key={submission.submissionId}
                hover
                tabIndex={0}
                aria-label={`View submission ${submission.submissionId}`}
                onClick={() => onSelectSubmission(submission.submissionId)}
                onKeyDown={(event) => {
                  if (event.key === "Enter" || event.key === " ") {
                    event.preventDefault();
                    onSelectSubmission(submission.submissionId);
                  }
                }}
                sx={{ cursor: "pointer" }}
              >
                <TableCell>
                  <Chip
                    label={submission.state}
                    color={stateColors[submission.state]}
                    size="small"
                  />
                </TableCell>
                <TableCell>{formatLanguage(submission.language)}</TableCell>
                <TableCell>{formatDateTime(submission.createdAt)}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
}
