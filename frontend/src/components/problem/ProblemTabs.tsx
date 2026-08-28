import {
  Box,
  CircularProgress,
  IconButton,
  Paper,
  Stack,
  Tab,
  Tabs,
} from "@mui/material";
import { ArrowBack, Close, Refresh } from "@mui/icons-material";
import { Link as RouterLink } from "react-router-dom";
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

export default function ProblemTabs({
  problem,
  activeTab,
  onTabChange,
  submissions,
  submissionsError,
  liveUpdatesError,
  onRefreshSubmissions,
  selectedSubmissionId,
  submissionDetailsRefreshKey,
  onSelectSubmission,
  onCloseSubmission,
  width,
}: ProblemTabsProps) {
  return (
    <Paper
      variant="outlined"
      sx={{
        flex: { xs: "1 1 50%", lg: "0 0 auto" },
        width: { lg: `${width}%` },
        minWidth: 0,
        minHeight: 0,
        display: "flex",
        flexDirection: "column",
        overflow: "hidden",
      }}
    >
      <Stack
        direction="row"
        sx={{
          alignItems: "center",
          borderBottom: 1,
          borderColor: "divider",
          px: 1,
        }}
      >
        <IconButton
          component={RouterLink}
          to="/problems"
          aria-label="Back to problems"
        >
          <ArrowBack />
        </IconButton>
        <Tabs
          value={activeTab}
          onChange={(_event, value: number) => onTabChange(value)}
          sx={{ flex: 1 }}
        >
          <Tab label="Problem" />
          <Tab
            label={
              submissions === null
                ? "Submissions"
                : `Submissions (${submissions.length})`
            }
          />
          {selectedSubmissionId !== null && <Tab label="Submission" />}
        </Tabs>
        {activeTab === 1 && (
          <IconButton
            aria-label="Refresh submissions"
            onClick={onRefreshSubmissions}
          >
            <Refresh />
          </IconButton>
        )}
        {activeTab === 2 && selectedSubmissionId !== null && (
          <IconButton
            aria-label="Close submission details"
            onClick={onCloseSubmission}
          >
            <Close />
          </IconButton>
        )}
      </Stack>

      <Box sx={{ flex: 1, minHeight: 0, overflowY: "auto", p: 3 }}>
        {activeTab === 0 && (
          <>
            {problem === null && (
              <Box sx={{ display: "flex", justifyContent: "center", py: 4 }}>
                <CircularProgress />
              </Box>
            )}
            {problem !== null && <ProblemInfo problem={problem} />}
          </>
        )}
        {activeTab === 1 && (
          <SubmissionsList
            submissions={submissions}
            error={submissionsError}
            liveUpdatesError={liveUpdatesError}
            onSelectSubmission={onSelectSubmission}
          />
        )}
        {activeTab === 2 && selectedSubmissionId !== null && (
          <SubmissionDetails
            submissionId={selectedSubmissionId}
            refreshKey={submissionDetailsRefreshKey}
          />
        )}
      </Box>
    </Paper>
  );
}
