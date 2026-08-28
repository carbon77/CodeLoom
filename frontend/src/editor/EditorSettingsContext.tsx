// oxlint-disable react/only-export-components -- Context modules expose their provider and hook together.
import {
  createContext,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from "react";

export type EditorTheme = "vs-dark" | "vs" | "hc-black" | "hc-light";

export const editorThemes: ReadonlyArray<{
  value: EditorTheme;
  label: string;
}> = [
  { value: "vs-dark", label: "Dark" },
  { value: "vs", label: "Light" },
  { value: "hc-black", label: "High Contrast Dark" },
  { value: "hc-light", label: "High Contrast Light" },
];

export const editorThemeStorageKey = "codeloom.editor.theme";
const defaultEditorTheme: EditorTheme = "vs-dark";
const validThemes = new Set<EditorTheme>(editorThemes.map(({ value }) => value));

interface EditorSettingsValue {
  theme: EditorTheme;
  setTheme: (theme: EditorTheme) => void;
}

const EditorSettingsContext = createContext<EditorSettingsValue | null>(null);

function storedTheme(): EditorTheme {
  try {
    const value = window.localStorage.getItem(editorThemeStorageKey);
    return validThemes.has(value as EditorTheme)
      ? (value as EditorTheme)
      : defaultEditorTheme;
  } catch {
    return defaultEditorTheme;
  }
}

export function EditorSettingsProvider({ children }: { children: ReactNode }) {
  const [theme, setThemeState] = useState<EditorTheme>(storedTheme);

  const value = useMemo<EditorSettingsValue>(() => ({
    theme,
    setTheme: (nextTheme) => {
      setThemeState(nextTheme);
      try {
        window.localStorage.setItem(editorThemeStorageKey, nextTheme);
      } catch {
        // The in-memory preference still works when browser storage is unavailable.
      }
    },
  }), [theme]);

  return (
    <EditorSettingsContext.Provider value={value}>
      {children}
    </EditorSettingsContext.Provider>
  );
}

export function useEditorSettings(): EditorSettingsValue {
  const value = useContext(EditorSettingsContext);
  if (value === null) {
    throw new Error("useEditorSettings must be used inside EditorSettingsProvider");
  }
  return value;
}
