import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  FormControl,
  InputAdornment,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from "@mui/material";
import SearchIcon from "@mui/icons-material/Search";
import {
  fetchProblems,
  fetchTopics,
  type Difficulty,
  type ProblemListDto,
  type Topic,
} from "../api/problems";

const difficultyColors: Record<Difficulty, "success" | "warning" | "error"> = {
  EASY: "success",
  MEDIUM: "warning",
  HARD: "error",
};

const difficulties: Difficulty[] = ["EASY", "MEDIUM", "HARD"];

function formatDate(value: string | null): string {
  if (!value) {
    return "—";
  }
  return new Date(value).toLocaleDateString();
}

export default function ProblemListPage() {
  const navigate = useNavigate();
  const [problems, setProblems] = useState<ProblemListDto[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  const [topics, setTopics] = useState<Topic[]>([]);
  const [selectedDifficulties, setSelectedDifficulties] = useState<
    Difficulty[]
  >([]);
  const [selectedTopics, setSelectedTopics] = useState<string[]>([]);
  const [search, setSearch] = useState("");

  useEffect(() => {
    let active = true;
    fetchTopics()
      .then((items) => {
        if (active) {
          setTopics(items);
        }
      })
      .catch(() => {});
    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    let active = true;
    setError(null);
    setProblems(null);
    const params: Record<string, string | string[]> = {};
    if (selectedDifficulties.length > 0) {
      params.difficulties = selectedDifficulties;
    }
    if (selectedTopics.length > 0) {
      params.topics = selectedTopics;
    }
    fetchProblems(params)
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
  }, [selectedDifficulties, selectedTopics]);

  const normalizedSearch = search.trim().toLowerCase();
  const filtered = problems?.filter((problem) =>
    problem.title.toLowerCase().includes(normalizedSearch),
  );

  const hasFilters =
    selectedDifficulties.length > 0 ||
    selectedTopics.length > 0 ||
    normalizedSearch !== "";

  const handleClearFilters = () => {
    setSelectedDifficulties([]);
    setSelectedTopics([]);
    setSearch("");
  };

  return (
    <Box>
      <Typography variant="h4" component="h1" sx={{ mb: 3 }}>
        Problems
      </Typography>

      <Paper sx={{ p: 2, mb: 3 }}>
        <Stack
          direction={{ xs: "column", md: "row" }}
          spacing={2}
          sx={{ alignItems: "center" }}
        >
          <TextField
            size="small"
            placeholder="Search by title"
            value={search}
            onChange={(event) => setSearch(event.target.value)}
            sx={{ flex: 1, minWidth: 200 }}
            slotProps={{
              input: {
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon fontSize="small" />
                  </InputAdornment>
                ),
              },
            }}
          />
          <FormControl size="small" sx={{ minWidth: 200 }}>
            <InputLabel>Difficulty</InputLabel>
            <Select<Difficulty[]>
              multiple
              label="Difficulty"
              value={selectedDifficulties}
              onChange={(event) =>
                setSelectedDifficulties(event.target.value as Difficulty[])
              }
            >
              {difficulties.map((difficulty) => (
                <MenuItem key={difficulty} value={difficulty}>
                  {difficulty}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <FormControl size="small" sx={{ minWidth: 200 }}>
            <InputLabel>Topic</InputLabel>
            <Select<string[]>
              multiple
              label="Topic"
              value={selectedTopics}
              onChange={(event) =>
                setSelectedTopics(event.target.value as string[])
              }
            >
              {topics.map((topic) => (
                <MenuItem key={topic.id} value={topic.name}>
                  {topic.name}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <Button
            variant="outlined"
            disabled={!hasFilters}
            onClick={handleClearFilters}
          >
            Clear
          </Button>
        </Stack>
      </Paper>

      {problems === null && !error && (
        <Box sx={{ display: "flex", justifyContent: "center", py: 4 }}>
          <CircularProgress />
        </Box>
      )}

      {error && <Alert severity="error">{error}</Alert>}

      {problems !== null && filtered?.length === 0 && (
        <Alert severity="info">
          {hasFilters
            ? "No problems match your filters."
            : "No problems available."}
        </Alert>
      )}

      {problems !== null && filtered && filtered.length > 0 && (
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
              {filtered.map((problem) => (
                <TableRow
                  key={problem.problemId}
                  hover
                  onClick={() => navigate(`/problems/${problem.slug}`)}
                  sx={{ cursor: "pointer" }}
                >
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
