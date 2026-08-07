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
  SubmissionStatus,
} from "../../api/submissions";

const statusColors: Record<
  SubmissionStatus,
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
}

export default function SubmissionsList({
  submissions,
  error,
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
    <TableContainer component={Paper} variant="outlined">
      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell>Status</TableCell>
            <TableCell>Language</TableCell>
            <TableCell>Submitted</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {submissions?.map((submission) => (
            <TableRow key={submission.id}>
              <TableCell>
                <Chip
                  label={submission.status}
                  color={statusColors[submission.status]}
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
  );
}
