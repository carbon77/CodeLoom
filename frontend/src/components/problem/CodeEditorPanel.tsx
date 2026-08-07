import { useState } from "react";
import {
  Alert,
  Box,
  Button,
  MenuItem,
  Paper,
  Select,
  Stack,
  type SelectChangeEvent,
} from "@mui/material";
import { Send } from "@mui/icons-material";
import Editor, { loader } from "@monaco-editor/react";
import * as monaco from "monaco-editor";
import { sendSubmission } from "../../api/submissions";

loader.config({ monaco });

type Language = "java" | "cpp" | "python";

const starterCode: Record<Language, string> = {
  java: `import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        // TODO: solve the problem
    }
}
`,
  cpp: `#include <iostream>

int main() {
    // TODO: solve the problem
    return 0;
}
`,
  python: `# TODO: solve the problem
`,
};

interface CodeEditorPanelProps {
  problemId: number | null;
  disabled: boolean;
  onSubmitted: () => void;
}

export default function CodeEditorPanel({
  problemId,
  disabled,
  onSubmitted,
}: CodeEditorPanelProps) {
  const [language, setLanguage] = useState<Language>("python");
  const [code, setCode] = useState<string>(starterCode.python);
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [submitSuccess, setSubmitSuccess] = useState(false);

  const handleLanguageChange = (event: SelectChangeEvent<Language>) => {
    const next = event.target.value;
    setCode((current) =>
      current.trim() === starterCode[language].trim()
        ? starterCode[next]
        : current,
    );
    setLanguage(next);
  };

  const handleSubmit = async () => {
    if (problemId === null) {
      return;
    }
    setSubmitting(true);
    setSubmitError(null);
    setSubmitSuccess(false);
    try {
      await sendSubmission({ problemId, code, language });
      setSubmitSuccess(true);
      onSubmitted();
    } catch {
      setSubmitError("Failed to submit solution. Please try again.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Paper
      variant="outlined"
      sx={{
        flex: { xs: "1 1 50%", lg: "1 1 auto" },
        minWidth: 0,
        minHeight: 0,
        display: "flex",
        flexDirection: "column",
        overflow: "hidden",
      }}
    >
      <Stack
        direction="row"
        spacing={2}
        sx={{
          alignItems: "center",
          p: 1.5,
          borderBottom: 1,
          borderColor: "divider",
        }}
      >
        <Select<Language>
          value={language}
          onChange={handleLanguageChange}
          size="small"
          sx={{ minWidth: 140 }}
        >
          <MenuItem value="java">Java</MenuItem>
          <MenuItem value="cpp">C++</MenuItem>
          <MenuItem value="python">Python</MenuItem>
        </Select>
        <Box sx={{ flexGrow: 1 }} />
        <Button
          variant="contained"
          startIcon={<Send />}
          disabled={submitting || disabled || problemId === null}
          onClick={handleSubmit}
        >
          {submitting ? "Submitting…" : "Submit"}
        </Button>
      </Stack>
      <Box sx={{ flex: 1, minHeight: 0 }}>
        <Editor
          height="100%"
          language={language}
          value={code}
          onChange={(value) => setCode(value ?? "")}
          theme="vs-dark"
          options={{
            fontSize: 14,
            minimap: { enabled: false },
            scrollBeyondLastLine: false,
            automaticLayout: true,
          }}
        />
      </Box>
      {submitSuccess && (
        <Alert severity="success" sx={{ m: 1.5 }}>
          Submission sent successfully.
        </Alert>
      )}
      {submitError && (
        <Alert severity="error" sx={{ m: 1.5 }}>
          {submitError}
        </Alert>
      )}
    </Paper>
  );
}
