import {
  Accordion,
  AccordionDetails,
  AccordionSummary,
  Box,
  Chip,
  Divider,
  Paper,
  Stack,
  Typography,
} from "@mui/material";
import { ExpandMore } from "@mui/icons-material";
import type { Difficulty, ProblemDetail } from "../../api/problems";
import MarkdownView from "./MarkdownView";

const difficultyColors: Record<Difficulty, "success" | "warning" | "error"> = {
  EASY: "success",
  MEDIUM: "warning",
  HARD: "error",
};

interface ProblemInfoProps {
  problem: ProblemDetail;
}

export default function ProblemInfo({ problem }: ProblemInfoProps) {
  return (
    <>
      <Box sx={{ display: "flex", alignItems: "center", gap: 1, mb: 2 }}>
        <Typography variant="h5" component="h1" sx={{ flex: 1 }}>
          {problem.title}
        </Typography>
        <Chip
          label={problem.difficulty}
          color={difficultyColors[problem.difficulty]}
          size="small"
        />
      </Box>
      {problem.topics.length > 0 && (
        <Stack direction="row" spacing={1} useFlexGap sx={{ mb: 2, flexWrap: "wrap" }}>
          {problem.topics.map((topic) => <Chip key={topic.id} label={topic.name} size="small" variant="outlined" />)}
        </Stack>
      )}
      <MarkdownView>{problem.description}</MarkdownView>

      {problem.constraints &&
        (problem.constraints.executionTimeLimitMs != null ||
          problem.constraints.memoryUsageLimitBytes != null) && (
          <>
            <Divider sx={{ my: 2 }} />
            <Typography variant="h6" sx={{ mb: 1 }}>
              Constraints
            </Typography>
            <Stack spacing={0.5}>
              {problem.constraints.executionTimeLimitMs != null && (
                <Typography variant="body2">
                  Time limit: {problem.constraints.executionTimeLimitMs} ms
                </Typography>
              )}
              {problem.constraints.memoryUsageLimitBytes != null && (
                <Typography variant="body2">
                  Memory limit:{" "}
                  {Math.round(
                    problem.constraints.memoryUsageLimitBytes / 1024 / 1024,
                  )}{" "}
                  MB
                </Typography>
              )}
            </Stack>
          </>
        )}

      {problem.examples?.examples.length ? (
        <>
          <Divider sx={{ my: 2 }} />
          <Typography variant="h6" sx={{ mb: 1 }}>
            Examples
          </Typography>
          {problem.examples.examples.map((example, index) => (
            <Paper key={index} variant="outlined" sx={{ p: 1.5, mb: 1.5 }}>
              <Typography variant="subtitle2" sx={{ mb: 1 }}>
                Example {index + 1}
              </Typography>
              <Typography
                component="div"
                sx={{
                  fontFamily: "monospace",
                  fontSize: "0.8rem",
                  whiteSpace: "pre-wrap",
                  bgcolor: "action.hover",
                  p: 1,
                  borderRadius: 1,
                  mb: 1,
                }}
              >
                <strong>Input:</strong>
                {`\n${example.input}`}
              </Typography>
              <Typography
                component="div"
                sx={{
                  fontFamily: "monospace",
                  fontSize: "0.8rem",
                  whiteSpace: "pre-wrap",
                  bgcolor: "action.hover",
                  p: 1,
                  borderRadius: 1,
                }}
              >
                <strong>Output:</strong>
                {`\n${example.output}`}
              </Typography>
              {example.explanation && (
                <Typography
                  variant="body2"
                  color="text.secondary"
                  sx={{ mt: 1 }}
                >
                  {example.explanation}
                </Typography>
              )}
            </Paper>
          ))}
        </>
      ) : null}

      {problem.hints.length > 0 && (
        <>
          <Typography variant="h6" sx={{ mt: 2, mb: 1 }}>
            Hints
          </Typography>
          <Stack spacing={1}>
            {problem.hints.map((hint, index) => (
              <Accordion
                key={index}
                disableGutters
                sx={{
                  border: 1,
                  borderColor: "divider",
                  borderRadius: 1,
                  boxShadow: "none",
                  "&:before": { display: "none" },
                }}
              >
                <AccordionSummary expandIcon={<ExpandMore />}>
                  <Typography variant="subtitle2">Hint {index + 1}</Typography>
                </AccordionSummary>
                <AccordionDetails>
                  <Typography variant="body2">{hint}</Typography>
                </AccordionDetails>
              </Accordion>
            ))}
          </Stack>
        </>
      )}
    </>
  );
}
