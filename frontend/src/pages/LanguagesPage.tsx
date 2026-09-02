import { useEffect, useState } from "react";
import {
  Alert,
  Box,
  CircularProgress,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from "@mui/material";
import {
  fetchLanguageDisplay,
  fetchLanguageSystem,
  type LanguageDisplay,
  type LanguageSystem,
} from "../api/languages";
import { errorMessage } from "../api/client";

interface LanguageRow extends LanguageDisplay {
  compileCommand: string | null;
  runCommand: string;
}

export default function LanguagesPage() {
  const [languages, setLanguages] = useState<LanguageRow[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    Promise.all([fetchLanguageDisplay(), fetchLanguageSystem()])
      .then(([display, system]) => {
        if (!active) return;
        const systemByKey = new Map<string, LanguageSystem>(
          system.map((language) => [language.key, language]),
        );
        setLanguages(
          display.flatMap((language) => {
            const details = systemByKey.get(language.key);
            return details ? [{ ...language, ...details }] : [];
          }),
        );
      })
      .catch((cause) => {
        if (active) {
          setError(errorMessage(cause, "Unable to load language information."));
        }
      });
    return () => {
      active = false;
    };
  }, []);

  return (
    <Box>
      <Typography variant="h4" component="h1" sx={{ mb: 3 }}>
        Languages
      </Typography>

      {languages === null && error === null && (
        <Box sx={{ display: "flex", justifyContent: "center", py: 4 }}>
          <CircularProgress />
        </Box>
      )}
      {error && <Alert severity="error">{error}</Alert>}
      {languages?.length === 0 && (
        <Alert severity="info">No languages are currently available.</Alert>
      )}
      {languages && languages.length > 0 && (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Name</TableCell>
                <TableCell>Key</TableCell>
                <TableCell>Commands</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {languages.map((language) => (
                <TableRow key={language.key}>
                  <TableCell>{language.name}</TableCell>
                  <TableCell>{language.key}</TableCell>
                  <TableCell>
                    <Box
                      component="pre"
                      sx={{ m: 0, fontFamily: "monospace", whiteSpace: "pre-wrap" }}
                    >
                      {[language.compileCommand, language.runCommand]
                        .filter((command): command is string => command !== null)
                        .join("\n")}
                    </Box>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}
    </Box>
  );
}
