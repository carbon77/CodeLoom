import { useEffect, useRef, useState } from "react";
import { Box } from "@mui/material";
import { useRouteLoaderData } from "react-router-dom";
import type { ProblemDetail } from "../api/problems";
import {
  fetchSubmissions,
  type Submission,
} from "../api/submissions";
import ProblemTabs from "../components/problem/ProblemTabs";
import CodeEditorPanel from "../components/problem/CodeEditorPanel";
import { errorMessage } from "../api/client";

const activeStatuses = new Set(["PENDING", "COMPILING", "RUNNING"]);

export default function ProblemDetailPage() {
  const problem = useRouteLoaderData("problem") as ProblemDetail | undefined;

  const [activeTab, setActiveTab] = useState(0);
  const [submissions, setSubmissions] = useState<Submission[] | null>(null);
  const [submissionsError, setSubmissionsError] = useState<string | null>(null);
  const [refreshKey, setRefreshKey] = useState(0);

  const [leftWidth, setLeftWidth] = useState(42);
  const [dragging, setDragging] = useState(false);
  const containerRef = useRef<HTMLDivElement | null>(null);

  const problemId = problem?.id ?? null;

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
    let timer: ReturnType<typeof setTimeout> | undefined;
    const load = () => fetchSubmissions(problemId)
      .then((items) => {
        if (active) {
          const sorted = [...items].sort(
            (a, b) =>
              new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime(),
          );
          setSubmissions(sorted);
          if (items.some((item) => activeStatuses.has(item.status))) {
            timer = setTimeout(load, 2000);
          }
        }
      })
      .catch((cause: unknown) => {
        if (active) {
          setSubmissions(null);
          setSubmissionsError(errorMessage(cause, "Unable to load submissions."));
        }
      });
    void load();
    return () => {
      active = false;
      if (timer !== undefined) clearTimeout(timer);
    };
  }, [activeTab, problemId, refreshKey]);

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
        onRefreshSubmissions={() => setRefreshKey((key) => key + 1)}
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
        onSubmitted={() => setRefreshKey((key) => key + 1)}
      />
    </Box>
  );
}
