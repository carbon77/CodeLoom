import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import DeleteIcon from "@mui/icons-material/Delete";
import EditIcon from "@mui/icons-material/Edit";
import PublishIcon from "@mui/icons-material/Publish";
import UnpublishedIcon from "@mui/icons-material/Unpublished";
import {
  deleteProblem,
  fetchProblems,
  publishProblem,
  unpublishProblem,
  type Difficulty,
  type ProblemListDto,
} from "../../api/problems";

const difficultyColors: Record<Difficulty, "success" | "warning" | "error"> = {
  EASY: "success",
  MEDIUM: "warning",
  HARD: "error",
};

export default function AdminProblemListPage() {
  const [problems, setProblems] = useState<ProblemListDto[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busyId, setBusyId] = useState<number | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<ProblemListDto | null>(null);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    let active = true;
    setError(null);
    void loadProblems(active);
    return () => {
      active = false;
    };
  }, []);

  async function loadProblems(active: boolean): Promise<void> {
    try {
      const items = await fetchProblems({ publishedOnly: "false" });
      if (active) {
        setProblems(items);
      }
    } catch {
      if (active) {
        setError("Unable to load problems. Please try again.");
      }
    }
  }

  async function handleTogglePublished(problem: ProblemListDto): Promise<void> {
    setBusyId(problem.problemId);
    setError(null);
    try {
      if (problem.publishedAt) {
        await unpublishProblem(problem.problemId);
      } else {
        await publishProblem(problem.problemId);
      }
      await loadProblems(true);
    } catch {
      setError("Unable to update publication status. Please try again.");
    } finally {
      setBusyId(null);
    }
  }

  async function handleDelete(): Promise<void> {
    if (!deleteTarget) {
      return;
    }
    setDeleting(true);
    setError(null);
    try {
      await deleteProblem(deleteTarget.problemId);
      setDeleteTarget(null);
      await loadProblems(true);
    } catch {
      setError("Unable to delete problem. Please try again.");
    } finally {
      setDeleting(false);
    }
  }

  return (
    <Box>
      <Box sx={{ display: "flex", alignItems: "center", mb: 3 }}>
        <Typography variant="h4" component="h1" sx={{ flexGrow: 1 }}>
          Manage Problems
        </Typography>
        <Button
          component={Link}
          to="/admin/problems/new"
          variant="contained"
          startIcon={<AddIcon />}
        >
          New Problem
        </Button>
      </Box>

      {problems === null && !error && (
        <Box sx={{ display: "flex", justifyContent: "center", py: 4 }}>
          <CircularProgress />
        </Box>
      )}

      {error && <Alert severity="error">{error}</Alert>}

      {problems !== null && problems.length === 0 && (
        <Alert severity="info">No problems yet. Create your first one.</Alert>
      )}

      {problems !== null && problems.length > 0 && (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Title</TableCell>
                <TableCell>Slug</TableCell>
                <TableCell>Difficulty</TableCell>
                <TableCell>Status</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {problems.map((problem) => (
                <TableRow key={problem.problemId} hover>
                  <TableCell>{problem.title}</TableCell>
                  <TableCell>{problem.slug}</TableCell>
                  <TableCell>
                    <Chip
                      label={problem.difficulty}
                      color={difficultyColors[problem.difficulty]}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={problem.publishedAt ? "Published" : "Draft"}
                      color={problem.publishedAt ? "success" : "default"}
                      size="small"
                      variant="outlined"
                    />
                  </TableCell>
                  <TableCell align="right">
                    <Box
                      sx={{
                        display: "flex",
                        gap: 1,
                        justifyContent: "flex-end",
                      }}
                    >
                      <Button
                        component={Link}
                        to={`/admin/problems/${problem.problemId}/edit`}
                        size="small"
                        startIcon={<EditIcon />}
                      >
                        Edit
                      </Button>
                      <Button
                        size="small"
                        color={problem.publishedAt ? "warning" : "success"}
                        startIcon={
                          problem.publishedAt ? (
                            <UnpublishedIcon />
                          ) : (
                            <PublishIcon />
                          )
                        }
                        disabled={busyId === problem.problemId}
                        onClick={() => void handleTogglePublished(problem)}
                      >
                        {problem.publishedAt ? "Unpublish" : "Publish"}
                      </Button>
                      <Button
                        size="small"
                        color="error"
                        startIcon={<DeleteIcon />}
                        onClick={() => setDeleteTarget(problem)}
                      >
                        Delete
                      </Button>
                    </Box>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog
        open={deleteTarget !== null}
        onClose={() => setDeleteTarget(null)}
      >
        <DialogTitle>Delete problem</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to delete "{deleteTarget?.title}"? This cannot
            be undone.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteTarget(null)} disabled={deleting}>
            Cancel
          </Button>
          <Button
            color="error"
            variant="contained"
            disabled={deleting}
            onClick={() => void handleDelete()}
          >
            {deleting ? "Deleting…" : "Delete"}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
