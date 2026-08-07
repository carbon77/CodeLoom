import {
  Button,
  FormControl,
  InputAdornment,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  Stack,
  TextField,
} from "@mui/material";
import SearchIcon from "@mui/icons-material/Search";
import type { Difficulty, Topic } from "../../api/problems";

const difficulties: Difficulty[] = ["EASY", "MEDIUM", "HARD"];

interface ProblemFiltersProps {
  search: string;
  onSearchChange: (value: string) => void;
  selectedDifficulties: Difficulty[];
  onSelectedDifficultiesChange: (values: Difficulty[]) => void;
  topics: Topic[];
  selectedTopics: string[];
  onSelectedTopicsChange: (values: string[]) => void;
  hasFilters: boolean;
  onClearFilters: () => void;
}

export default function ProblemFilters({
  search,
  onSearchChange,
  selectedDifficulties,
  onSelectedDifficultiesChange,
  topics,
  selectedTopics,
  onSelectedTopicsChange,
  hasFilters,
  onClearFilters,
}: ProblemFiltersProps) {
  return (
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
          onChange={(event) => onSearchChange(event.target.value)}
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
              onSelectedDifficultiesChange(event.target.value as Difficulty[])
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
              onSelectedTopicsChange(event.target.value as string[])
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
          onClick={onClearFilters}
        >
          Clear
        </Button>
      </Stack>
    </Paper>
  );
}
