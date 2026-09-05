import { Spinner, Notice, Badge } from '../ui/Controls'
import { CheckCircle, Cancel } from '../ui/Icons'
import ui from '../ui/ui.module.css'
import styles from './SubmissionDetails.module.css'
import { useEffect, useState } from "react";
import { fetchSubmissionDetails, type SubmissionDetails as SubmissionDetailsDto, type SubmissionState, type TestCaseResult, } from "../../api/submissions";
import { errorMessage } from "../../api/client";
import CodeEditor from "./CodeEditor";
const stateColors: Record<SubmissionState, "success" | "warning" | "error" | "info" | "default"> = {
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
const activeStates = new Set<SubmissionState>([
  "PENDING",
  "COMPILING",
  "RUNNING",
]);
function output(value: string | null): string {
  return value === null || value === "" ? "—" : value;
}
function ResultField({ label, value }: {
  label: string;
  value: string | null;
}) {
  return (<div className={styles.field}>
    <span className={[ui.caption, styles.label].join(" ")}>
      {label}
    </span>
    <pre className={styles.value}>
      {output(value)}
    </pre>
  </div>);
}
function TestResult({ result, index }: {
  result: TestCaseResult;
  index: number;
}) {
  const passed = result.stdout?.trim() === result.expectedOutput?.trim();

  return (<details data-passed={passed} className={[ui.disclosure, styles.result].join(" ")}>
    <summary aria-controls={`test-result-${index}-content`} id={`test-result-${index}-header`} className={[ui.summary, styles.resultSummary].join(" ")}>
      <div className={[ui.stack, styles.resultHeader].join(" ")}>
        {passed ? <CheckCircle /> : <Cancel />}
        <span className={ui.subtitle}>Test case {index + 1}</span>
        <span className={[ui.caption, styles.verdict].join(" ")}>
          {passed ? "Passed" : "Failed"}
        </span>
        <span className={[ui.caption, styles.timing].join(" ")}>
          {result.executionTimeMs === null ? "—" : `${result.executionTimeMs} ms`}
          {" · "}
          {result.bytesUsed === null ? "—" : `${result.bytesUsed} bytes`}
        </span>
      </div>
    </summary>
    <div id={`test-result-${index}-content`} className={[ui.details, styles.resultFields].join(" ")}>
      <ResultField label="Input" value={result.input} />
      <ResultField label="Expected output" value={result.expectedOutput} />
      <ResultField label="Standard output" value={result.stdout} />
      <ResultField label="Standard error" value={result.stderr} />
    </div>
  </details>);
}
interface SubmissionDetailsProps {
  submissionId: string;
  refreshKey: number;
}
export default function SubmissionDetails({ submissionId, refreshKey, }: SubmissionDetailsProps) {
  const [details, setDetails] = useState<SubmissionDetailsDto | null>(null);
  const [error, setError] = useState<string | null>(null);
  useEffect(() => {
    let active = true;
    setDetails(null);
    setError(null);
    fetchSubmissionDetails(submissionId)
      .then((value) => {
        if (active)
          setDetails(value);
      })
      .catch((cause: unknown) => {
        if (active) {
          setError(errorMessage(cause, "Unable to load submission details."));
        }
      });
    return () => {
      active = false;
    };
  }, [submissionId, refreshKey]);
  if (details === null && error === null) {
    return (<div className={styles.loading}>
      <Spinner />
    </div>);
  }
  if (error !== null)
    return <Notice tone="error">{error}</Notice>;
  if (details === null)
    return null;
  return (<div className={[ui.stack, styles.content].join(" ")}>
    <div className={[ui.stack, styles.metadata].join(" ")}>
      <Badge tone={stateColors[details.state]}>{details.state}</Badge>
      <p className={ui.body}>{details.language.toUpperCase()}</p>
      <p className={[ui.body, styles.timestamp].join(" ")}>
        {new Date(details.createdAt).toLocaleString()}
      </p>
    </div>

    {details.errorMessage && <Notice tone="error">{details.errorMessage}</Notice>}

    <div>
      <h2 className={styles.sourceTitle}>Source code</h2>
      <CodeEditor language={details.language} value={details.code} readOnly copyable height={280} />
    </div>

    <hr />
    <h3 className={styles.resultsTitle}>
      Test results
    </h3>
    {details.results.length === 0 ? (<Notice tone="info">
      {activeStates.has(details.state)
        ? "This submission is still being processed."
        : "No test-case results are available for this submission."}
    </Notice>) : (details.results.map((result, index) => (<TestResult key={index} result={result} index={index} />)))}
  </div>);
}
