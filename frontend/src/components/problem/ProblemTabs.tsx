import { LinkButton, IconButton, Spinner } from '../ui/Controls'
import { ArrowBack, Close, Refresh } from '../ui/Icons'
import ui from '../ui/ui.module.css'
import styles from './ProblemTabs.module.css'
import { useId } from 'react';
import type { ProblemDetail } from "../../api/problems";
import type { Submission } from "../../api/submissions";
import ProblemInfo from "./ProblemInfo";
import SubmissionsList from "./SubmissionsList";
import SubmissionDetails from "./SubmissionDetails";
interface ProblemTabsProps {
  problem: ProblemDetail | null;
  activeTab: number;
  onTabChange: (value: number) => void;
  submissions: Submission[] | null;
  submissionsError: string | null;
  liveUpdatesError: string | null;
  onRefreshSubmissions: () => void;
  selectedSubmissionId: string | null;
  submissionDetailsRefreshKey: number;
  onSelectSubmission: (submissionId: string) => void;
  onCloseSubmission: () => void;
  width: number;
}
export default function ProblemTabs({ problem, activeTab, onTabChange, submissions, submissionsError, liveUpdatesError, onRefreshSubmissions, selectedSubmissionId, submissionDetailsRefreshKey, onSelectSubmission, onCloseSubmission, width, }: ProblemTabsProps) {
  const id = useId();
  const tabs = ['Problem', submissions === null ? 'Submissions' : `Submissions (${submissions.length})`, ...(selectedSubmissionId !== null ? ['Submission'] : [])];
  return (<div className={[ui.panel, styles.panel].join(" ")} style={{ "--panel-width": `${width}%` } as React.CSSProperties}>
    <div className={[ui.stack, styles.toolbar].join(" ")}>
      <LinkButton to="/problems" aria-label="Back to problems" className={ui.iconButton}>
        <ArrowBack />
      </LinkButton>
      <div role="tablist" aria-label="Problem workspace" className={styles.tabs} onKeyDown={(event) => {
        const next = event.key === 'ArrowRight' ? (activeTab + 1) % tabs.length : event.key === 'ArrowLeft' ? (activeTab + tabs.length - 1) % tabs.length : event.key === 'Home' ? 0 : event.key === 'End' ? tabs.length - 1 : -1;
        if (next >= 0) { event.preventDefault(); onTabChange(next); event.currentTarget.querySelectorAll<HTMLButtonElement>('button')[next].focus(); }
      }}>
        {tabs.map((label, index) => <button type="button" key={index} role="tab" id={`${id}-tab-${index}`} aria-selected={activeTab === index} aria-controls={`${id}-panel`} tabIndex={activeTab === index ? 0 : -1} onClick={() => onTabChange(index)}>{label}</button>)}
      </div>
      {activeTab === 1 && (<IconButton aria-label="Refresh submissions" onClick={onRefreshSubmissions}>
        <Refresh />
      </IconButton>)}
      {activeTab === 2 && selectedSubmissionId !== null && (<IconButton aria-label="Close submission details" onClick={onCloseSubmission}>
        <Close />
      </IconButton>)}
    </div>

    <div className={styles.content} role="tabpanel" id={`${id}-panel`} aria-labelledby={`${id}-tab-${activeTab}`} tabIndex={0}>
      {activeTab === 0 && (<>
        {problem === null && (<div className={styles.loading}>
          <Spinner />
        </div>)}
        {problem !== null && <ProblemInfo problem={problem} />}
      </>)}
      {activeTab === 1 && (<SubmissionsList submissions={submissions} error={submissionsError} liveUpdatesError={liveUpdatesError} onSelectSubmission={onSelectSubmission} />)}
      {activeTab === 2 && selectedSubmissionId !== null && (<SubmissionDetails submissionId={selectedSubmissionId} refreshKey={submissionDetailsRefreshKey} />)}
    </div>
  </div>);
}
