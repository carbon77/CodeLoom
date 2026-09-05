import { Spinner, Notice, Field, SelectField, Button, IconButton, TopicPicker } from '../../components/ui/Controls'
import { Add as AddIcon, Delete as DeleteIcon } from '../../components/ui/Icons'
import ui from '../../components/ui/ui.module.css'
import styles from './ProblemFormPage.module.css'
import PageHeading from '../../components/ui/PageHeading'
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { createProblem, createTestCase, deleteTestCase, fetchProblem, fetchProblemBySlug, fetchTestCases, fetchTopics, updateTestCase as updateTestCaseApi, updateProblem, type Difficulty, type TestCase, type Topic, } from '../../api/problems';
import { errorMessage } from '../../api/client';
import { serializeTopics } from './topicSerialization';
function deriveSlug(title: string): string {
  return title.toLowerCase().replaceAll(' ', '_');
}
function toNullableNumber(value: string): number | null {
  return value.trim() === '' ? null : Number(value);
}
export default function ProblemFormPage() {
  const { problemId } = useParams();
  const navigate = useNavigate();
  const isEdit = problemId !== undefined;
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [title, setTitle] = useState('');
  const [slug, setSlug] = useState('');
  const [slugTouched, setSlugTouched] = useState(false);
  const [description, setDescription] = useState('');
  const [difficulty, setDifficulty] = useState<Difficulty>('EASY');
  const [executionTimeLimitMs, setExecutionTimeLimitMs] = useState('');
  const [memoryUsageLimitMb, setMemoryUsageLimitMb] = useState('');
  const [hints, setHints] = useState<string[]>([]);
  const [testCases, setTestCases] = useState<TestCase[]>([]);
  const [initialTestCases, setInitialTestCases] = useState<TestCase[]>([]);
  const [topics, setTopics] = useState<Topic[]>([]);
  const [selectedTopics, setSelectedTopics] = useState<Array<Topic | string>>([]);
  useEffect(() => {
    let active = true;
    const id = Number(problemId);
    const problemPromise = isEdit
      ? fetchProblem(id).then((raw) => fetchProblemBySlug(raw.slug))
      : Promise.resolve(null);
    Promise.all([fetchTopics(), problemPromise, isEdit ? fetchTestCases(id) : Promise.resolve([])])
      .then(([loadedTopics, problem, loadedTestCases]) => {
        if (!active) {
          return;
        }
        setTopics(loadedTopics);
        if (!problem)
          return;
        setTitle(problem.title);
        setSlug(problem.slug);
        setDescription(problem.description);
        setDifficulty(problem.difficulty);
        setExecutionTimeLimitMs(problem.constraints?.executionTimeLimitMs?.toString() ?? '');
        setMemoryUsageLimitMb(problem.constraints?.memoryUsageLimitMb?.toString() ?? '');
        setHints(problem.hints);
        setTestCases(loadedTestCases);
        setInitialTestCases(loadedTestCases);
        setSelectedTopics(problem.topics);
      })
      .catch((cause: unknown) => {
        if (active) {
          setError(errorMessage(cause, 'Unable to load problem data. Please try again.'));
        }
      })
      .finally(() => {
        if (active) {
          setLoading(false);
        }
      });
    return () => {
      active = false;
    };
  }, [isEdit, problemId]);
  function updateHint(index: number, value: string): void {
    setHints((items) => items.map((item, i) => (i === index ? value : item)));
  }
  function updateTestCase(index: number, patch: Partial<TestCase>): void {
    setTestCases((cases) => cases.map((testCase, i) => (i === index ? { ...testCase, ...patch } : testCase)));
  }
  async function handleSave(): Promise<void> {
    if (title.trim() === '') {
      setError('Title is mandatory.');
      return;
    }
    setSaving(true);
    setError(null);
    const payload = {
      title: title.trim(),
      slug: slug.trim() === '' ? deriveSlug(title.trim()) : slug.trim(),
      description,
      difficulty,
      constraints: {
        executionTimeLimitMs: toNullableNumber(executionTimeLimitMs),
        memoryUsageLimitMb: toNullableNumber(memoryUsageLimitMb),
      },
      hints,
      topics: serializeTopics(selectedTopics, topics),
    };
    try {
      if (isEdit) {
        const id = Number(problemId);
        await updateProblem(id, payload);
        const currentIds = new Set(testCases
          .map((testCase) => testCase.id)
          .filter((value): value is string => value !== undefined));
        const removed = initialTestCases.filter((testCase) => testCase.id !== undefined && !currentIds.has(testCase.id));
        for (const testCase of removed) {
          await deleteTestCase(testCase.id as string);
        }
        for (const testCase of testCases) {
          if (testCase.id) {
            await updateTestCaseApi(testCase.id, {
              problemId: id,
              input: testCase.input,
              expectedOutput: testCase.expectedOutput,
              isPublic: testCase.isPublic,
              explanation: testCase.explanation,
            });
          }
          else {
            await createTestCase({ ...testCase, problemId: id });
          }
        }
      }
      else {
        const created = await createProblem(payload.title);
        await updateProblem(created.id, payload);
        for (const testCase of testCases) {
          await createTestCase({ ...testCase, problemId: created.id });
        }
      }
      navigate('/admin/problems');
    }
    catch (cause) {
      setError(errorMessage(cause, 'Unable to save problem. Please check the values and try again.'));
      setSaving(false);
    }
  }
  if (loading) {
    return (<div className={styles.loading}>
      <Spinner />
    </div>);
  }
  return (<form noValidate onSubmit={(event) => { event.preventDefault(); if (!saving) void handleSave(); }} className={styles.form}>
    <PageHeading eyebrow="Content studio / Editor" title={isEdit ? 'Edit Problem' : 'New Problem'} description="Shape the challenge, define its limits, and add the tests that make it work." />

    {error && (<Notice tone="error" className={styles.error}>
      {error}
    </Notice>)}

    <fieldset disabled={saving} className={styles.fields}>
      <div className={[ui.panel, styles.problemSection].join(" ")}>
        <h2 className={styles.sectionTitle}>
          Problem
        </h2>
        <div className={[ui.stack, styles.problemFields].join(" ")}>
          <Field label="Title" required value={title} onChange={(event) => {
            setTitle(event.target.value);
            if (!slugTouched) {
              setSlug(deriveSlug(event.target.value));
            }
          }} />
          <Field label="Slug" value={slug} onChange={(event) => {
            setSlugTouched(true);
            setSlug(event.target.value);
          }} />
          <Field label="Description" multiline rows={4} value={description} onChange={(event) => setDescription(event.target.value)} />
          <div>

            <SelectField label="Difficulty" value={difficulty} onChange={(event) => setDifficulty(event.target.value as Difficulty)}>
              <option value="EASY">Easy</option>
              <option value="MEDIUM">Medium</option>
              <option value="HARD">Hard</option>
            </SelectField>
          </div>
          <TopicPicker options={topics} value={selectedTopics} onChange={setSelectedTopics} />
          <div className={[ui.stack, styles.limits].join(" ")}>
            <Field label="Time limit (ms)" type="number" value={executionTimeLimitMs} onChange={(event) => setExecutionTimeLimitMs(event.target.value)} className={styles.timeLimit} />
            <Field label="Memory limit (MB)" type="number" value={memoryUsageLimitMb} onChange={(event) => setMemoryUsageLimitMb(event.target.value)} className={styles.memoryLimit} />
          </div>

          <hr />

          <div className={[ui.stack, styles.hintsHeader].join(" ")}>
            <h3 className={styles.hintsTitle}>
              Hints
            </h3>
            <Button icon={<AddIcon />} onClick={() => setHints((items) => [...items, ''])}>
              Add hint
            </Button>
          </div>
          {hints.map((hint, index) => (<div key={index} className={[ui.stack, styles.hintRow].join(" ")}>
            <Field label={`Hint ${index + 1}`} value={hint} onChange={(event) => updateHint(index, event.target.value)} className={styles.hintField} />
            <IconButton aria-label="Remove hint" tone="error" onClick={() => setHints((items) => items.filter((_, i) => i !== index))}>
              <DeleteIcon />
            </IconButton>
          </div>))}
        </div>
      </div>

      <div className={[ui.panel, styles.testsSection].join(" ")}>
        <div className={[ui.stack, styles.testsHeader].join(" ")}>
          <h2 className={styles.testsTitle}>
            Test Cases
          </h2>
          <Button icon={<AddIcon />} onClick={() => setTestCases((cases) => [
            ...cases,
            { input: '', expectedOutput: '', isPublic: false, explanation: '' },
          ])}>
            Add test case
          </Button>
        </div>
        {testCases.length === 0 && (<p className={styles.emptyTests}>
          No test cases. Add at least one so the problem can be judged.
        </p>)}
        <div className={[ui.stack, styles.testCases].join(" ")}>
          {testCases.map((testCase, index) => (<div key={testCase.id ?? `new-${index}`} className={[ui.stack, styles.testCase].join(" ")}>
            <Field label="Input" multiline value={testCase.input} onChange={(event) => updateTestCase(index, { input: event.target.value })} className={styles.testInput} />
            <Field label="Expected output" multiline value={testCase.expectedOutput} onChange={(event) => updateTestCase(index, { expectedOutput: event.target.value })} className={styles.testOutput} />
            <Field label="Explanation" value={testCase.explanation ?? ''} onChange={(event) => updateTestCase(index, { explanation: event.target.value })} className={styles.testExplanation} />
            <label className={[ui.checkbox, styles.testPublic].join(" ")}><input checked={testCase.isPublic} onChange={(event) => updateTestCase(index, { isPublic: event.target.checked })} type="checkbox" />{"Public"}</label>
            <IconButton aria-label="Remove test case" tone="error" onClick={() => setTestCases((cases) => cases.filter((_, i) => i !== index))}>
              <DeleteIcon />
            </IconButton>
          </div>))}
        </div>
      </div>

    </fieldset>
    <div className={[ui.stack, styles.actions].join(" ")}>
      <Button onClick={() => navigate('/admin/problems')} disabled={saving}>
        Cancel
      </Button>
      <Button type="submit" appearance="primary" disabled={saving || loading}>
        {saving ? 'Saving…' : 'Save'}
      </Button>
    </div>
  </form>);
}
