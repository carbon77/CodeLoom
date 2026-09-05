import { Spinner, Notice } from '../components/ui/Controls'
import ui from '../components/ui/ui.module.css'
import styles from './LanguagesPage.module.css'
import PageHeading from '../components/ui/PageHeading'
import { useEffect, useState } from "react";
import { fetchLanguageDisplay, fetchLanguageSystem, type LanguageDisplay, type LanguageSystem, } from "../api/languages";
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
        if (!active)
          return;
        const systemByKey = new Map<string, LanguageSystem>(system.map((language) => [language.key, language]));
        setLanguages(display.flatMap((language) => {
          const details = systemByKey.get(language.key);
          return details ? [{ ...language, ...details }] : [];
        }));
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
  return (<div>
    <PageHeading eyebrow="Your toolkit" title="Languages" description="Choose your language. See how your code is compiled and run in the judge." />

    {languages === null && error === null && (<div className={styles.loading}>
      <Spinner />
    </div>)}
    {error && <Notice tone="error">{error}</Notice>}
    {languages?.length === 0 && (<Notice tone="info">No languages are currently available.</Notice>)}
    {languages && languages.length > 0 && (<div className={ui.tableContainer}>
      <table className={ui.table}>
        <thead>
          <tr>
            <th>Name</th>
            <th>Key</th>
            <th>Commands</th>
          </tr>
        </thead>
        <tbody>
          {languages.map((language) => (<tr key={language.key}>
            <td>{language.name}</td>
            <td>{language.key}</td>
            <td>
              <pre className={styles.commands}>
                {[language.compileCommand, language.runCommand]
                  .filter((command): command is string => command !== null)
                  .join("\n")}
              </pre>
            </td>
          </tr>))}
        </tbody>
      </table>
    </div>)}
  </div>);
}
