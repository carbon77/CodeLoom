import { useState, type MouseEvent } from "react";
import { Settings } from "@mui/icons-material";
import {
  IconButton,
  ListItemIcon,
  ListItemText,
  MenuItem,
  MenuList,
  Popover,
  Radio,
  Tooltip,
  Typography,
} from "@mui/material";
import {
  editorThemes,
  useEditorSettings,
} from "../../editor/EditorSettingsContext";

export default function EditorSettings() {
  const { theme, setTheme } = useEditorSettings();
  const [anchorElement, setAnchorElement] = useState<HTMLElement | null>(null);

  const openSettings = (event: MouseEvent<HTMLElement>) => {
    setAnchorElement(event.currentTarget);
  };

  return (
    <>
      <Tooltip title="Editor settings">
        <IconButton
          aria-label="Editor settings"
          aria-controls={anchorElement ? "editor-settings-menu" : undefined}
          aria-haspopup="menu"
          aria-expanded={anchorElement ? "true" : undefined}
          onClick={openSettings}
        >
          <Settings />
        </IconButton>
      </Tooltip>
      <Popover
        open={anchorElement !== null}
        anchorEl={anchorElement}
        onClose={() => setAnchorElement(null)}
        anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
        transformOrigin={{ vertical: "top", horizontal: "right" }}
      >
        <Typography variant="subtitle2" sx={{ px: 2, pt: 1.5 }}>
          Color scheme
        </Typography>
        <MenuList id="editor-settings-menu" aria-label="Editor color scheme">
          {editorThemes.map((option) => (
            <MenuItem
              key={option.value}
              aria-label={option.label}
              selected={theme === option.value}
              onClick={() => {
                setTheme(option.value);
                setAnchorElement(null);
              }}
            >
              <ListItemIcon>
                <Radio
                  checked={theme === option.value}
                  size="small"
                  slotProps={{ input: { "aria-label": option.label } }}
                />
              </ListItemIcon>
              <ListItemText>{option.label}</ListItemText>
            </MenuItem>
          ))}
        </MenuList>
      </Popover>
    </>
  );
}
