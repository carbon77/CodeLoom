import { useState } from "react";
import { Check, ContentCopy } from "@mui/icons-material";
import { Box, IconButton, Tooltip } from "@mui/material";
import Editor, { loader } from "@monaco-editor/react";
import * as monaco from "monaco-editor";
import { useEditorSettings } from "../../editor/EditorSettingsContext";

loader.config({ monaco });

interface CodeEditorProps {
  language: string;
  value: string;
  onChange?: (value: string) => void;
  readOnly?: boolean;
  copyable?: boolean;
  height?: string | number;
}

export default function CodeEditor({
  language,
  value,
  onChange,
  readOnly = false,
  copyable = false,
  height = "100%",
}: CodeEditorProps) {
  const [copied, setCopied] = useState(false);
  const { theme } = useEditorSettings();

  const copyCode = async () => {
    await navigator.clipboard.writeText(value);
    setCopied(true);
    window.setTimeout(() => setCopied(false), 1500);
  };

  return (
    <Box sx={{ height, minHeight: 0, position: "relative" }}>
      <Editor
        height="100%"
        language={language}
        value={value}
        onChange={(nextValue) => onChange?.(nextValue ?? "")}
        theme={theme}
        options={{
          readOnly,
          domReadOnly: readOnly,
          fontSize: 14,
          minimap: { enabled: false },
          scrollBeyondLastLine: false,
          automaticLayout: true,
          padding: { top: 12, bottom: 12 },
        }}
      />
      {copyable && (
        <Tooltip title={copied ? "Copied" : "Copy code"}>
          <IconButton
            aria-label={copied ? "Code copied" : "Copy code"}
            size="small"
            onClick={() => void copyCode()}
            sx={{
              position: "absolute",
              top: 8,
              right: 16,
              color: "grey.300",
              bgcolor: "rgba(0, 0, 0, 0.45)",
              "&:hover": { bgcolor: "rgba(0, 0, 0, 0.7)" },
            }}
          >
            {copied ? <Check fontSize="small" /> : <ContentCopy fontSize="small" />}
          </IconButton>
        </Tooltip>
      )}
    </Box>
  );
}
