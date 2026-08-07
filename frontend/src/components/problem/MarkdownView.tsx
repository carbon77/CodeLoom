import { Box, Typography, Link as MuiLink } from "@mui/material";
import ReactMarkdown, { type Components } from "react-markdown";
import remarkGfm from "remark-gfm";

const markdownComponents: Components = {
  h1: ({ children }) => (
    <Typography variant="h5" sx={{ mt: 2, mb: 1 }}>
      {children}
    </Typography>
  ),
  h2: ({ children }) => (
    <Typography variant="h6" sx={{ mt: 2, mb: 1 }}>
      {children}
    </Typography>
  ),
  h3: ({ children }) => (
    <Typography variant="subtitle1" sx={{ mt: 2, mb: 1, fontWeight: "bold" }}>
      {children}
    </Typography>
  ),
  p: ({ children }) => (
    <Typography variant="body2" sx={{ mb: 1 }}>
      {children}
    </Typography>
  ),
  ul: ({ children }) => (
    <Box component="ul" sx={{ pl: 3, mb: 1, listStyleType: "disc" }}>
      {children}
    </Box>
  ),
  ol: ({ children }) => (
    <Box component="ol" sx={{ pl: 3, mb: 1, listStyleType: "decimal" }}>
      {children}
    </Box>
  ),
  li: ({ children }) => (
    <Typography component="li" variant="body2">
      {children}
    </Typography>
  ),
  a: ({ href, children }) => (
    <MuiLink href={href} target="_blank" rel="noreferrer">
      {children}
    </MuiLink>
  ),
  code: ({ className, children }) =>
    className?.includes("language-") ? (
      <code className={className}>{children}</code>
    ) : (
      <Typography
        component="code"
        sx={{
          bgcolor: "action.hover",
          px: 0.5,
          borderRadius: 0.5,
          fontFamily: "monospace",
          fontSize: "0.9em",
        }}
      >
        {children}
      </Typography>
    ),
  pre: ({ children }) => (
    <Box
      component="pre"
      sx={{
        bgcolor: (theme) =>
          theme.palette.mode === "dark"
            ? "rgba(255, 255, 255, 0.06)"
            : "rgba(0, 0, 0, 0.05)",
        p: 1.5,
        borderRadius: 1,
        overflowX: "auto",
        my: 1.5,
        fontFamily: "monospace",
        fontSize: "0.85rem",
      }}
    >
      {children}
    </Box>
  ),
  table: ({ children }) => (
    <Box
      component="table"
      sx={{
        borderCollapse: "collapse",
        mb: 1,
        fontSize: "0.85rem",
        overflowX: "auto",
      }}
    >
      {children}
    </Box>
  ),
  th: ({ children }) => (
    <Box component="th" sx={{ border: 1, borderColor: "divider", p: 0.5 }}>
      {children}
    </Box>
  ),
  td: ({ children }) => (
    <Box component="td" sx={{ border: 1, borderColor: "divider", p: 0.5 }}>
      {children}
    </Box>
  ),
};

interface MarkdownViewProps {
  children: string;
}

export default function MarkdownView({ children }: MarkdownViewProps) {
  return (
    <ReactMarkdown remarkPlugins={[remarkGfm]} components={markdownComponents}>
      {children}
    </ReactMarkdown>
  );
}
