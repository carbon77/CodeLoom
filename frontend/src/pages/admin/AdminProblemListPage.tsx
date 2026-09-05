import { LinkButton, Spinner, Notice, Badge, Button, Modal } from '../../components/ui/Controls'
import { Add as AddIcon, Delete as DeleteIcon, Edit as EditIcon, Publish as PublishIcon, Unpublished as UnpublishedIcon } from '../../components/ui/Icons'
import ui from '../../components/ui/ui.module.css'
import styles from './AdminProblemListPage.module.css'
import PageHeading from '../../components/ui/PageHeading'
import { useCallback, useEffect, useState } from "react";
import { deleteProblem, fetchProblems, fetchTopics, publishProblem, unpublishProblem, type Difficulty, type ProblemListDto, type Topic, } from "../../api/problems";
import ProblemFilters from "../../components/problem/ProblemFilters";
import { errorMessage } from "../../api/client";
const difficultyColors: Record<Difficulty, "success" | "warning" | "error"> = {
  EASY: "success",
  MEDIUM: "warning",
  HARD: "error",
};
export default function AdminProblemListPage() {
  const [problems, setProblems] = useState<ProblemListDto[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState("");
  const [topics, setTopics] = useState<Topic[]>([]);
  const [selectedDifficulties, setSelectedDifficulties] = useState<Difficulty[]>([]);
  const [selectedTopics, setSelectedTopics] = useState<string[]>([]);
  const [busyId, setBusyId] = useState<number | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<ProblemListDto | null>(null);
  const [deleting, setDeleting] = useState(false);
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
  const loadProblems = useCallback(async (active: boolean): Promise<void> => {
    try {
      const params: Record<string, string | string[]> = {
        publishedOnly: "false",
      };
      if (selectedDifficulties.length > 0) {
        params.difficulties = selectedDifficulties;
      }
      if (selectedTopics.length > 0) {
        params.topics = selectedTopics;
      }
      const items = await fetchProblems(params);
      if (active) {
        setProblems(items);
      }
    }
    catch (cause) {
      if (active) {
        setError(errorMessage(cause, "Unable to load problems. Please try again."));
      }
    }
  }, [selectedDifficulties, selectedTopics]);
  useEffect(() => {
    let active = true;
    setError(null);
    void loadProblems(active);
    return () => {
      active = false;
    };
  }, [loadProblems]);
  const normalizedSearch = search.trim().toLowerCase();
  const filtered = problems?.filter((problem) => problem.title.toLowerCase().includes(normalizedSearch)) ?? null;
  const hasFilters = selectedDifficulties.length > 0 ||
    selectedTopics.length > 0 ||
    normalizedSearch !== "";
  const handleClearFilters = () => {
    setSelectedDifficulties([]);
    setSelectedTopics([]);
    setSearch("");
  };
  async function handleTogglePublished(problem: ProblemListDto): Promise<void> {
    setBusyId(problem.problemId);
    setError(null);
    try {
      if (problem.publishedAt) {
        await unpublishProblem(problem.problemId);
      }
      else {
        await publishProblem(problem.problemId);
      }
      await loadProblems(true);
    }
    catch (cause) {
      setError(errorMessage(cause, "Unable to update publication status. Please try again."));
    }
    finally {
      setBusyId(null);
    }
  }
  async function handleDelete(): Promise<void> {
    if (!deleteTarget) {
      return;
    }
    setDeleting(true);
    setError(null);
    try {
      await deleteProblem(deleteTarget.problemId);
      setDeleteTarget(null);
      await loadProblems(true);
    }
    catch (cause) {
      setError(errorMessage(cause, "Unable to delete problem. Please try again."));
    }
    finally {
      setDeleting(false);
    }
  }
  return (<div>
    <PageHeading eyebrow="Content studio" title="Manage Problems" description="Create challenges, refine test cases, and publish when everything is ready.">
      <LinkButton to="/admin/problems/new" appearance="primary" icon={<AddIcon />}>
        New Problem
      </LinkButton>
    </PageHeading>

    <ProblemFilters search={search} onSearchChange={setSearch} selectedDifficulties={selectedDifficulties} onSelectedDifficultiesChange={setSelectedDifficulties} topics={topics} selectedTopics={selectedTopics} onSelectedTopicsChange={setSelectedTopics} hasFilters={hasFilters} onClearFilters={handleClearFilters} />

    {problems === null && !error && (<div className={styles.loading}>
      <Spinner />
    </div>)}

    {error && <Notice tone="error">{error}</Notice>}

    {problems !== null && problems.length === 0 && (<Notice tone="info">No problems yet. Create your first one.</Notice>)}

    {problems !== null && problems.length > 0 && filtered?.length === 0 && (<Notice tone="info">No problems match your filters.</Notice>)}

    {problems !== null && filtered && filtered.length > 0 && (<div className={ui.tableContainer}>
      <table className={ui.table}>
        <thead>
          <tr>
            <th>Title</th>
            <th>Slug</th>
            <th>Difficulty</th>
            <th>Status</th>
            <th className={ui.alignRight}>Actions</th>
          </tr>
        </thead>
        <tbody>
          {filtered.map((problem) => (<tr key={problem.problemId}>
            <td>{problem.title}</td>
            <td>{problem.slug}</td>
            <td>
              <Badge tone={difficultyColors[problem.difficulty]}>{problem.difficulty}</Badge>
            </td>
            <td>
              <Badge tone={problem.publishedAt ? "success" : "default"}>{problem.publishedAt ? "Published" : "Draft"}</Badge>
            </td>
            <td className={ui.alignRight}>
              <div className={styles.actions}>
                <LinkButton to={`/admin/problems/${problem.problemId}/edit`} icon={<EditIcon />}>
                  Edit
                </LinkButton>
                <Button tone={problem.publishedAt ? "warning" : "success"} icon={problem.publishedAt ? (<UnpublishedIcon />) : (<PublishIcon />)} disabled={busyId === problem.problemId} onClick={() => void handleTogglePublished(problem)}>
                  {problem.publishedAt ? "Unpublish" : "Publish"}
                </Button>
                <Button tone="error" icon={<DeleteIcon />} onClick={() => setDeleteTarget(problem)}>
                  Delete
                </Button>
              </div>
            </td>
          </tr>))}
        </tbody>
      </table>
    </div>)}

    <Modal title="Delete problem" busy={deleting} open={deleteTarget !== null} onClose={() => setDeleteTarget(null)}>

      <div>
        <p>
          Are you sure you want to delete "{deleteTarget?.title}"? This cannot
          be undone.
        </p>
      </div>
      <div className={ui.modalActions}>
        <Button onClick={() => setDeleteTarget(null)} disabled={deleting}>
          Cancel
        </Button>
        <Button tone="error" appearance="primary" disabled={deleting} onClick={() => void handleDelete()}>
          {deleting ? "Deleting…" : "Delete"}
        </Button>
      </div>
    </Modal>
  </div>);
}
