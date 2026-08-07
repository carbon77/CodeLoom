import { useEffect, useState } from "react";
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
  Typography,
} from "@mui/material";
import {
  fetchProblems,
  type Difficulty,
  type ProblemListDto,
} from "../api/problems";

const difficultyColors: Record<Difficulty, "success" | "warning" | "error"> = {
  EASY: "success",
  MEDIUM: "warning",
  HARD: "error",
};

function formatDate(value: string | null): string {
  if (!value) {
    return "—";
  }
  return new Date(value).toLocaleDateString();
}

export default function ProblemListPage() {
  const [problems, setProblems] = useState<ProblemListDto[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    setError(null);
    fetchProblems()
      .then((items) => {
        if (active) {
          setProblems(items);
        }
      })
      .catch(() => {
        if (active) {
          setError("Unable to load problems. Please try again.");
        }
      });
    return () => {
      active = false;
    };
  }, []);

  return (
    <Box>
      <Typography variant="h4" component="h1" sx={{ mb: 3 }}>
        Problems
      </Typography>

      {problems === null && !error && (
        <Box sx={{ display: "flex", justifyContent: "center", py: 4 }}>
          <CircularProgress />
        </Box>
      )}

      {error && <Alert severity="error">{error}</Alert>}

      {problems !== null && problems.length === 0 && (
        <Alert severity="info">No problems available.</Alert>
      )}

      {problems !== null && problems.length > 0 && (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Title</TableCell>
                <TableCell>Difficulty</TableCell>
                <TableCell>Published</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {problems.map((problem) => (
                <TableRow key={problem.id} hover>
                  <TableCell>{problem.title}</TableCell>
                  <TableCell>
                    <Chip
                      label={problem.difficulty}
                      color={difficultyColors[problem.difficulty]}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>{formatDate(problem.publishedAt)}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}
    </Box>
  );
}
