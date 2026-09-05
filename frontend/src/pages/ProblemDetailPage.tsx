import styles from './ProblemDetailPage.module.css'
import { useEffect, useRef, useState } from "react";
import { useRouteLoaderData } from "react-router-dom";
import type { ProblemDetail } from "../api/problems";
import { fetchSubmissions, subscribeToSubmissionStates, type Submission, type SubmissionState, } from "../api/submissions";
import ProblemTabs from "../components/problem/ProblemTabs";
import CodeEditorPanel from "../components/problem/CodeEditorPanel";
import { errorMessage } from "../api/client";
const terminalStates = new Set<SubmissionState>([
  "COMPILE_ERROR",
  "ACCEPTED",
  "WRONG_ANSWER",
  "RUNTIME_ERROR",
  "TIME_LIMIT_EXCEEDED",
  "MEMORY_LIMIT_EXCEEDED",
  "SYSTEM_ERROR",
]);
export default function ProblemDetailPage() {
  const problem = useRouteLoaderData("problem") as ProblemDetail | undefined;
  const [activeTab, setActiveTab] = useState(0);
  const [submissions, setSubmissions] = useState<Submission[] | null>(null);
  const [submissionsError, setSubmissionsError] = useState<string | null>(null);
  const [liveUpdatesError, setLiveUpdatesError] = useState<string | null>(null);
  const [refreshKey, setRefreshKey] = useState(0);
  const [selectedSubmissionId, setSelectedSubmissionId] = useState<string | null>(null);
  const [submissionDetailsRefreshKey, setSubmissionDetailsRefreshKey] = useState(0);
  const selectedSubmissionIdRef = useRef<string | null>(null);
  const lastSubmittedIdRef = useRef<string | null>(null);
  const [leftWidth, setLeftWidth] = useState(42);
  const [dragging, setDragging] = useState(false);
  const containerRef = useRef<HTMLDivElement | null>(null);
  const problemId = problem?.id ?? null;
  selectedSubmissionIdRef.current = selectedSubmissionId;
  useEffect(() => {
    setSelectedSubmissionId(null);
    setActiveTab(0);
    setSubmissions(null);
    lastSubmittedIdRef.current = null;
  }, [problemId]);
  useEffect(() => {
    if (!dragging) {
      return;
    }
    const handleMove = (event: PointerEvent) => {
      const rect = containerRef.current?.getBoundingClientRect();
      if (!rect) {
        return;
      }
      const percent = ((event.clientX - rect.left) / rect.width) * 100;
      setLeftWidth(Math.min(70, Math.max(20, percent)));
    };
    const handleUp = () => setDragging(false);
    window.addEventListener("pointermove", handleMove);
    window.addEventListener("pointerup", handleUp);
    return () => {
      window.removeEventListener("pointermove", handleMove);
      window.removeEventListener("pointerup", handleUp);
    };
  }, [dragging]);
  useEffect(() => {
    if (activeTab !== 1 || problemId === null) {
      return;
    }
    let active = true;
    setSubmissionsError(null);
    fetchSubmissions(problemId)
      .then((items) => {
        if (active) {
          const sorted = [...items].sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
          setSubmissions(sorted);
          const lastSubmittedId = lastSubmittedIdRef.current;
          const lastSubmission = sorted.find((item) => item.submissionId === lastSubmittedId);
          if (lastSubmission && terminalStates.has(lastSubmission.state)) {
            lastSubmittedIdRef.current = null;
            setSelectedSubmissionId(lastSubmission.submissionId);
            setActiveTab(2);
          }
        }
      })
      .catch((cause: unknown) => {
        if (active) {
          setSubmissions(null);
          setSubmissionsError(errorMessage(cause, "Unable to load submissions."));
        }
      });
    return () => {
      active = false;
    };
  }, [activeTab, problemId, refreshKey]);
  useEffect(() => {
    if (problemId === null)
      return;
    const controller = new AbortController();
    setLiveUpdatesError(null);
    void subscribeToSubmissionStates({
      signal: controller.signal,
      onConnected: () => {
        setLiveUpdatesError(null);
        setRefreshKey((key) => key + 1);
        if (selectedSubmissionIdRef.current !== null) {
          setSubmissionDetailsRefreshKey((key) => key + 1);
        }
      },
      onState: (state) => {
        setSubmissions((items) => items?.map((item) => item.submissionId === state.submissionId
          ? { ...item, state: state.state }
          : item) ?? null);
        if (selectedSubmissionIdRef.current === state.submissionId) {
          setSubmissionDetailsRefreshKey((key) => key + 1);
        }
        if (lastSubmittedIdRef.current === state.submissionId &&
          terminalStates.has(state.state)) {
          lastSubmittedIdRef.current = null;
          setSelectedSubmissionId(state.submissionId);
          setSubmissionDetailsRefreshKey((key) => key + 1);
          setActiveTab(2);
        }
      },
    }).catch(() => {
      if (!controller.signal.aborted) {
        setLiveUpdatesError("Live submission updates are unavailable. Use refresh to get the latest state.");
      }
    });
    return () => controller.abort();
  }, [problemId]);
  const selectSubmission = (submissionId: string) => {
    setSelectedSubmissionId(submissionId);
    setActiveTab(2);
  };
  const closeSubmission = () => {
    setSelectedSubmissionId(null);
    setActiveTab(1);
  };
  return (<div ref={containerRef} className={styles.workspace}>
    <ProblemTabs problem={problem ?? null} activeTab={activeTab} onTabChange={setActiveTab} submissions={submissions} submissionsError={submissionsError} liveUpdatesError={liveUpdatesError} onRefreshSubmissions={() => setRefreshKey((key) => key + 1)} selectedSubmissionId={selectedSubmissionId} submissionDetailsRefreshKey={submissionDetailsRefreshKey} onSelectSubmission={selectSubmission} onCloseSubmission={closeSubmission} width={leftWidth} />
    <div role="separator" aria-label="Resize problem panel" aria-orientation="vertical" aria-valuemin={20} aria-valuemax={70} aria-valuenow={Math.round(leftWidth)} tabIndex={0} data-dragging={dragging} onKeyDown={(event) => {
      if (['ArrowLeft', 'ArrowRight', 'Home', 'End'].includes(event.key)) {
        event.preventDefault();
        setLeftWidth((width) => event.key === 'Home' ? 20 : event.key === 'End' ? 70 : Math.min(70, Math.max(20, width + (event.key === 'ArrowLeft' ? -2 : 2))));
      }
    }} onPointerCancel={() => setDragging(false)} onPointerDown={(event) => {
      event.preventDefault();
      setDragging(true);
    }} className={styles.resizer} />
    <CodeEditorPanel problemId={problemId} disabled={problem === undefined} onSubmitted={(submissionId) => {
      lastSubmittedIdRef.current = submissionId;
      setRefreshKey((key) => key + 1);
    }} />
  </div>);
}
