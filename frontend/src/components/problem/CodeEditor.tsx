import { IconButton } from '../ui/Controls'
import { Check, ContentCopy } from '../ui/Icons'
import styles from './CodeEditor.module.css'
import { useState } from "react";
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
export default function CodeEditor({ language, value, onChange, readOnly = false, copyable = false, height = "100%", }: CodeEditorProps) {
  const [copied, setCopied] = useState(false);
  const { theme } = useEditorSettings();
  const copyCode = async () => {
    await navigator.clipboard.writeText(value);
    setCopied(true);
    window.setTimeout(() => setCopied(false), 1500);
  };
  return (<div className={styles.editor} style={{ "--editor-height": typeof height === "number" ? `${height}px` : height } as React.CSSProperties}>
    <Editor height="100%" language={language} value={value} onChange={(nextValue) => onChange?.(nextValue ?? "")} theme={theme} options={{
      readOnly,
      domReadOnly: readOnly,
      fontSize: 14,
      minimap: { enabled: false },
      scrollBeyondLastLine: false,
      automaticLayout: true,
      padding: { top: 12, bottom: 12 },
    }} />
    {copyable && (<>
      <IconButton aria-label={copied ? "Code copied" : "Copy code"} onClick={() => void copyCode()} className={styles.copyButton}>
        {copied ? <Check /> : <ContentCopy />}
      </IconButton>
    </>)}
  </div>);
}
