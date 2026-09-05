import { SelectField, Button, Notice } from '../ui/Controls'
import { Send } from '../ui/Icons'
import ui from '../ui/ui.module.css'
import styles from './CodeEditorPanel.module.css'
import { useEffect, useState } from "react";
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
export default function CodeEditorPanel({ problemId, disabled, onSubmitted, }: CodeEditorPanelProps) {
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
        if (!active)
          return;
        setLanguages(availableLanguages);
        const initial = availableLanguages.find((item) => item.key === "python") ??
          availableLanguages[0];
        if (initial) {
          setLanguage(initial.key);
          setCode(starterCode[initial.key] ?? "");
        }
      })
      .catch((cause) => {
        if (active) {
          setLanguageLoadError(errorMessage(cause, "Unable to load available languages."));
        }
      });
    return () => {
      active = false;
    };
  }, []);
  const handleLanguageChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    const next = event.target.value;
    const currentStarter = starterCode[language] ?? "";
    const nextStarter = starterCode[next] ?? "";
    setCode((current) => current.trim() === currentStarter.trim() ? nextStarter : current);
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
    }
    catch (cause) {
      setSubmitError(errorMessage(cause, "Failed to submit solution. Please try again."));
    }
    finally {
      setSubmitting(false);
    }
  };
  return (<div className={[ui.panel, styles.panel].join(" ")}>
    <div className={[ui.stack, styles.toolbar].join(" ")}>
      <SelectField label="Language" value={language} onChange={handleLanguageChange} disabled={languages === null || languages.length === 0} className={styles.languageField}>
        {languages?.map((availableLanguage) => (<option key={availableLanguage.key} value={availableLanguage.key}>
          {availableLanguage.name}
        </option>))}
      </SelectField>
      <div className={styles.spacer} />
      <EditorSettings />
      <Button appearance="primary" icon={<Send />} disabled={submitting || disabled || problemId === null || language === ""} onClick={handleSubmit}>
        {submitting ? "Submitting…" : "Submit"}
      </Button>
    </div>
    <div className={styles.editor}>
      <CodeEditor language={language} value={code} onChange={setCode} />
    </div>
    {submitSuccess && (<Notice tone="success" className={styles.submitSuccess}>
      Submission sent successfully.
    </Notice>)}
    {languageLoadError && (<Notice tone="error" className={styles.loadError}>
      {languageLoadError}
    </Notice>)}
    {submitError && (<Notice tone="error" className={styles.submitError}>
      {submitError}
    </Notice>)}
  </div>);
}
