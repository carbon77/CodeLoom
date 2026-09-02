import { useEffect, useState } from "react";
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
import { sendSubmission } from "../../api/submissions";
import { errorMessage } from "../../api/client";
import { fetchLanguageDisplay, type LanguageDisplay } from "../../api/languages";
import CodeEditor from "./CodeEditor";
import EditorSettings from "./EditorSettings";

const starterCode: Record<string, string> = {
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
  onSubmitted: (submissionId: string) => void;
}

export default function CodeEditorPanel({
  problemId,
  disabled,
  onSubmitted,
}: CodeEditorPanelProps) {
  const [languages, setLanguages] = useState<LanguageDisplay[] | null>(null);
  const [languageLoadError, setLanguageLoadError] = useState<string | null>(null);
  const [language, setLanguage] = useState("");
  const [code, setCode] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [submitSuccess, setSubmitSuccess] = useState(false);

  useEffect(() => {
    let active = true;
    fetchLanguageDisplay()
      .then((availableLanguages) => {
        if (!active) return;
        setLanguages(availableLanguages);
        const initial =
          availableLanguages.find((item) => item.key === "python") ??
          availableLanguages[0];
        if (initial) {
          setLanguage(initial.key);
          setCode(starterCode[initial.key] ?? "");
        }
      })
      .catch((cause) => {
        if (active) {
          setLanguageLoadError(
            errorMessage(cause, "Unable to load available languages."),
          );
        }
      });
    return () => {
      active = false;
    };
  }, []);

  const handleLanguageChange = (event: SelectChangeEvent<string>) => {
    const next = event.target.value;
    const currentStarter = starterCode[language] ?? "";
    const nextStarter = starterCode[next] ?? "";
    setCode((current) =>
      current.trim() === currentStarter.trim() ? nextStarter : current,
    );
    setLanguage(next);
  };

  const handleSubmit = async () => {
    if (problemId === null) {
      return;
    }
    if (code.trim() === "") {
      setSubmitError("Code must not be blank.");
      return;
    }
    setSubmitting(true);
    setSubmitError(null);
    setSubmitSuccess(false);
    try {
      const submission = await sendSubmission({ problemId, code, language });
      setSubmitSuccess(true);
      onSubmitted(submission.submissionId);
    } catch (cause) {
      setSubmitError(errorMessage(cause, "Failed to submit solution. Please try again."));
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
        <Select<string>
          value={language}
          onChange={handleLanguageChange}
          size="small"
          disabled={languages === null || languages.length === 0}
          sx={{ minWidth: 140 }}
        >
          {languages?.map((availableLanguage) => (
            <MenuItem key={availableLanguage.key} value={availableLanguage.key}>
              {availableLanguage.name}
            </MenuItem>
          ))}
        </Select>
        <Box sx={{ flexGrow: 1 }} />
        <EditorSettings />
        <Button
          variant="contained"
          startIcon={<Send />}
          disabled={
            submitting || disabled || problemId === null || language === ""
          }
          onClick={handleSubmit}
        >
          {submitting ? "Submitting…" : "Submit"}
        </Button>
      </Stack>
      <Box sx={{ flex: 1, minHeight: 0 }}>
        <CodeEditor
          language={language}
          value={code}
          onChange={setCode}
        />
      </Box>
      {submitSuccess && (
        <Alert severity="success" sx={{ m: 1.5 }}>
          Submission sent successfully.
        </Alert>
      )}
      {languageLoadError && (
        <Alert severity="error" sx={{ m: 1.5 }}>
          {languageLoadError}
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
