import { Spinner, Notice, Badge } from '../ui/Controls'
import ui from '../ui/ui.module.css'
import styles from './SubmissionsList.module.css'
import type { Submission, SubmissionState, } from "../../api/submissions";
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
function formatDateTime(value: string): string {
  return new Date(value).toLocaleString();
}
function formatLanguage(value: string): string {
  return value.charAt(0).toUpperCase() + value.slice(1);
}
interface SubmissionsListProps {
  submissions: Submission[] | null;
  error: string | null;
  liveUpdatesError?: string | null;
  onSelectSubmission: (submissionId: string) => void;
}
export default function SubmissionsList({ submissions, error, liveUpdatesError, onSelectSubmission, }: SubmissionsListProps) {
  if (submissions === null && !error) {
    return (<div className={styles.loading}>
      <Spinner />
    </div>);
  }
  if (error) {
    return <Notice tone="error">{error}</Notice>;
  }
  if (submissions !== null && submissions.length === 0) {
    return (<Notice tone="info">
      No submissions yet. Submit a solution to see it here.
    </Notice>);
  }
  return (<div>
    {liveUpdatesError && (<Notice tone="warning" className={styles.liveWarning}>
      {liveUpdatesError}
    </Notice>)}
    <div className={ui.tableContainer}>
      <table className={ui.table}>
        <thead>
          <tr>
            <th>State</th>
            <th>Language</th>
            <th>Submitted</th>
          </tr>
        </thead>
        <tbody>
          {submissions?.map((submission) => (<tr key={submission.submissionId} tabIndex={0} aria-label={`View submission ${submission.submissionId}`} onClick={() => onSelectSubmission(submission.submissionId)} onKeyDown={(event) => {
            if (event.key === "Enter" || event.key === " ") {
              event.preventDefault();
              onSelectSubmission(submission.submissionId);
            }
          }} className={styles.submissionRow}>
            <td>
              <Badge tone={stateColors[submission.state]}>{submission.state}</Badge>
            </td>
            <td>{formatLanguage(submission.language)}</td>
            <td>{formatDateTime(submission.createdAt)}</td>
          </tr>))}
        </tbody>
      </table>
    </div>
  </div>);
}
