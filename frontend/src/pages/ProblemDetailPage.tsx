import { useEffect, useRef, useState } from "react";
import { Box } from "@mui/material";
import { useRouteLoaderData } from "react-router-dom";
import type { ProblemDetail } from "../api/problems";
import {
  fetchSubmissions,
  subscribeToSubmissionStatuses,
  type Submission,
  type SubmissionStatus,
} from "../api/submissions";
import ProblemTabs from "../components/problem/ProblemTabs";
import CodeEditorPanel from "../components/problem/CodeEditorPanel";
import { errorMessage } from "../api/client";

const terminalStatuses = new Set<SubmissionStatus>([
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
          const sorted = [...items].sort(
            (a, b) =>
              new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime(),
          );
          setSubmissions(sorted);
          const lastSubmittedId = lastSubmittedIdRef.current;
          const lastSubmission = sorted.find(
            (item) => item.submissionId === lastSubmittedId,
          );
          if (lastSubmission && terminalStatuses.has(lastSubmission.status)) {
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
    if (problemId === null) return;
    const controller = new AbortController();
    setLiveUpdatesError(null);
    void subscribeToSubmissionStatuses({
      signal: controller.signal,
      onConnected: () => {
        setLiveUpdatesError(null);
        setRefreshKey((key) => key + 1);
        if (selectedSubmissionIdRef.current !== null) {
          setSubmissionDetailsRefreshKey((key) => key + 1);
        }
      },
      onStatus: (status) => {
        setSubmissions((items) =>
          items?.map((item) =>
            item.submissionId === status.submissionId
              ? { ...item, status: status.status }
              : item,
          ) ?? null,
        );
        if (selectedSubmissionIdRef.current === status.submissionId) {
          setSubmissionDetailsRefreshKey((key) => key + 1);
        }
        if (
          lastSubmittedIdRef.current === status.submissionId &&
          terminalStatuses.has(status.status)
        ) {
          lastSubmittedIdRef.current = null;
          setSelectedSubmissionId(status.submissionId);
          setSubmissionDetailsRefreshKey((key) => key + 1);
          setActiveTab(2);
        }
      },
    }).catch(() => {
      if (!controller.signal.aborted) {
        setLiveUpdatesError(
          "Live submission updates are unavailable. Use refresh to get the latest status.",
        );
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

  return (
    <Box
      ref={containerRef}
      sx={{
        display: "flex",
        flexDirection: { xs: "column", lg: "row" },
        gap: 2,
        height: "calc(100dvh - 112px)",
      }}
    >
      <ProblemTabs
        problem={problem ?? null}
        activeTab={activeTab}
        onTabChange={setActiveTab}
        submissions={submissions}
        submissionsError={submissionsError}
        liveUpdatesError={liveUpdatesError}
        onRefreshSubmissions={() => setRefreshKey((key) => key + 1)}
        selectedSubmissionId={selectedSubmissionId}
        submissionDetailsRefreshKey={submissionDetailsRefreshKey}
        onSelectSubmission={selectSubmission}
        onCloseSubmission={closeSubmission}
        width={leftWidth}
      />
      <Box
        onPointerDown={(event) => {
          event.preventDefault();
          setDragging(true);
        }}
        sx={{
          display: { xs: "none", lg: "block" },
          width: 8,
          flexShrink: 0,
          cursor: "col-resize",
          bgcolor: dragging ? "primary.main" : "divider",
          borderRadius: 1,
          alignSelf: "stretch",
          touchAction: "none",
          "&:hover": { bgcolor: "primary.light" },
        }}
      />
      <CodeEditorPanel
        problemId={problemId}
        disabled={problem === undefined}
        onSubmitted={(submissionId) => {
          lastSubmittedIdRef.current = submissionId;
          setRefreshKey((key) => key + 1);
        }}
      />
    </Box>
  );
}
