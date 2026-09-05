import { Spinner, Notice, Badge } from '../components/ui/Controls'
import ui from '../components/ui/ui.module.css'
import styles from './ProblemListPage.module.css'
import PageHeading from '../components/ui/PageHeading'
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchProblems, fetchTopics, type Difficulty, type ProblemListDto, type Topic, } from "../api/problems";
import ProblemFilters from "../components/problem/ProblemFilters";
const difficultyColors: Record<Difficulty, "success" | "warning" | "error"> = {
  EASY: "success",
  MEDIUM: "warning",
  HARD: "error",
};
function formatDate(value: string | null): string {
  if (!value) {
    return "—";
  }
  return new Date(value).toLocaleDateString();
}
export default function ProblemListPage() {
  const navigate = useNavigate();
  const [problems, setProblems] = useState<ProblemListDto[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [topics, setTopics] = useState<Topic[]>([]);
  const [selectedDifficulties, setSelectedDifficulties] = useState<Difficulty[]>([]);
  const [selectedTopics, setSelectedTopics] = useState<string[]>([]);
  const [search, setSearch] = useState("");
  useEffect(() => {
    let active = true;
    fetchTopics()
      .then((items) => {
        if (active) {
          setTopics(items);
        }
      })
      .catch(() => { });
    return () => {
      active = false;
    };
  }, []);
  useEffect(() => {
    let active = true;
    setError(null);
    setProblems(null);
    const params: Record<string, string | string[]> = {};
    if (selectedDifficulties.length > 0) {
      params.difficulties = selectedDifficulties;
    }
    if (selectedTopics.length > 0) {
      params.topics = selectedTopics;
    }
    fetchProblems(params)
      .then((items) => {
        if (active) {
          setProblems(items);
        }
      })
      .catch(() => {
        if (active) {
          setError("Unable to load problems. Please try again.");
        }
      });
    return () => {
      active = false;
    };
  }, [selectedDifficulties, selectedTopics]);
  const normalizedSearch = search.trim().toLowerCase();
  const filtered = problems?.filter((problem) => problem.title.toLowerCase().includes(normalizedSearch));
  const hasFilters = selectedDifficulties.length > 0 ||
    selectedTopics.length > 0 ||
    normalizedSearch !== "";
  const handleClearFilters = () => {
    setSelectedDifficulties([]);
    setSelectedTopics([]);
    setSearch("");
  };
  return (<div>
    <PageHeading eyebrow="Practice / Build / Improve" title="Problems" description="Find your next challenge. Explore topics, sharpen your skills, and make each solution count." />

    <ProblemFilters search={search} onSearchChange={setSearch} selectedDifficulties={selectedDifficulties} onSelectedDifficultiesChange={setSelectedDifficulties} topics={topics} selectedTopics={selectedTopics} onSelectedTopicsChange={setSelectedTopics} hasFilters={hasFilters} onClearFilters={handleClearFilters} />

    {problems === null && !error && (<div className={styles.loading}>
      <Spinner />
    </div>)}

    {error && <Notice tone="error">{error}</Notice>}

    {problems !== null && filtered?.length === 0 && (<Notice tone="info">
      {hasFilters
        ? "No problems match your filters."
        : "No problems available."}
    </Notice>)}

    {problems !== null && filtered && filtered.length > 0 && (<div className={ui.tableContainer}>
      <table className={ui.table}>
        <thead>
          <tr>
            <th>Title</th>
            <th>Difficulty</th>
            <th>Published</th>
          </tr>
        </thead>
        <tbody>
          {filtered.map((problem) => (<tr key={problem.problemId} onClick={() => navigate(`/problems/${problem.slug}`)} tabIndex={0} aria-label={`Open problem ${problem.title}`} onKeyDown={(event) => { if (event.key === 'Enter' || event.key === ' ') { event.preventDefault(); navigate(`/problems/${problem.slug}`); } }} className={styles.problemRow}>
            <td>{problem.title}</td>
            <td>
              <Badge tone={difficultyColors[problem.difficulty]}>{problem.difficulty}</Badge>
            </td>
            <td>{formatDate(problem.publishedAt)}</td>
          </tr>))}
        </tbody>
      </table>
    </div>)}
  </div>);
}
