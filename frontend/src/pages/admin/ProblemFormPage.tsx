import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import {
  Alert,
  Autocomplete,
  Box,
  Button,
  Checkbox,
  CircularProgress,
  Divider,
  FormControl,
  FormControlLabel,
  IconButton,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import AddIcon from '@mui/icons-material/Add'
import DeleteIcon from '@mui/icons-material/Delete'
import {
  createProblem,
  createTestCase,
  deleteTestCase,
  fetchProblem,
  fetchProblemBySlug,
  fetchTestCases,
  fetchTopics,
  updateTestCase as updateTestCaseApi,
  updateProblem,
  type Difficulty,
  type ProblemExample,
  type TestCase,
  type Topic,
} from '../../api/problems'
import { errorMessage } from '../../api/client'
import { serializeTopics } from './topicSerialization'

interface ExampleRow {
  input: string
  output: string
  explanation: string
}

function deriveSlug(title: string): string {
  return title.toLowerCase().replaceAll(' ', '_')
}

function toNullableNumber(value: string): number | null {
  return value.trim() === '' ? null : Number(value)
}

export default function ProblemFormPage() {
  const { problemId } = useParams()
  const navigate = useNavigate()
  const isEdit = problemId !== undefined

  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [title, setTitle] = useState('')
  const [slug, setSlug] = useState('')
  const [slugTouched, setSlugTouched] = useState(false)
  const [description, setDescription] = useState('')
  const [difficulty, setDifficulty] = useState<Difficulty>('EASY')
  const [executionTimeLimitMs, setExecutionTimeLimitMs] = useState('')
  const [memoryUsageLimitBytes, setMemoryUsageLimitBytes] = useState('')
  const [examples, setExamples] = useState<ExampleRow[]>([])
  const [hints, setHints] = useState<string[]>([])
  const [testCases, setTestCases] = useState<TestCase[]>([])
  const [initialTestCases, setInitialTestCases] = useState<TestCase[]>([])
  const [topics, setTopics] = useState<Topic[]>([])
  const [selectedTopics, setSelectedTopics] = useState<Array<Topic | string>>([])

  useEffect(() => {
    let active = true
    const id = Number(problemId)
    const problemPromise = isEdit
      ? fetchProblem(id).then((raw) => fetchProblemBySlug(raw.slug))
      : Promise.resolve(null)
    Promise.all([fetchTopics(), problemPromise, isEdit ? fetchTestCases(id) : Promise.resolve([])])
      .then(([loadedTopics, problem, loadedTestCases]) => {
        if (!active) {
          return
        }
        setTopics(loadedTopics)
        if (!problem) return
        setTitle(problem.title)
        setSlug(problem.slug)
        setDescription(problem.description)
        setDifficulty(problem.difficulty)
        setExecutionTimeLimitMs(problem.constraints?.executionTimeLimitMs?.toString() ?? '')
        setMemoryUsageLimitBytes(problem.constraints?.memoryUsageLimitBytes?.toString() ?? '')
        setExamples(
          (problem.examples?.examples ?? []).map((example: ProblemExample) => ({
            input: example.input,
            output: example.output,
            explanation: example.explanation ?? '',
          })),
        )
        setHints(problem.hints)
        setTestCases(loadedTestCases)
        setInitialTestCases(loadedTestCases)
        setSelectedTopics(problem.topics)
      })
      .catch((cause: unknown) => {
        if (active) {
          setError(errorMessage(cause, 'Unable to load problem data. Please try again.'))
        }
      })
      .finally(() => {
        if (active) {
          setLoading(false)
        }
      })
    return () => {
      active = false
    }
  }, [isEdit, problemId])

  function updateExample(index: number, field: keyof ExampleRow, value: string): void {
    setExamples((rows) =>
      rows.map((row, i) => (i === index ? { ...row, [field]: value } : row)),
    )
  }

  function updateHint(index: number, value: string): void {
    setHints((items) => items.map((item, i) => (i === index ? value : item)))
  }

  function updateTestCase(index: number, patch: Partial<TestCase>): void {
    setTestCases((cases) =>
      cases.map((testCase, i) => (i === index ? { ...testCase, ...patch } : testCase)),
    )
  }

  async function handleSave(): Promise<void> {
    if (title.trim() === '') {
      setError('Title is mandatory.')
      return
    }
    setSaving(true)
    setError(null)
    const payload = {
      title: title.trim(),
      slug: slug.trim() === '' ? deriveSlug(title.trim()) : slug.trim(),
      description,
      difficulty,
      constraints: {
        executionTimeLimitMs: toNullableNumber(executionTimeLimitMs),
        memoryUsageLimitBytes: toNullableNumber(memoryUsageLimitBytes),
      },
      examples: { examples },
      hints,
      topics: serializeTopics(selectedTopics, topics),
    }
    try {
      if (isEdit) {
        const id = Number(problemId)
        await updateProblem(id, payload)
        const currentIds = new Set(
          testCases
            .map((testCase) => testCase.id)
            .filter((value): value is string => value !== undefined),
        )
        const removed = initialTestCases.filter(
          (testCase) => testCase.id !== undefined && !currentIds.has(testCase.id),
        )
        for (const testCase of removed) {
          await deleteTestCase(testCase.id as string)
        }
        for (const testCase of testCases) {
          if (testCase.id) {
            await updateTestCaseApi(testCase.id, {
              problemId: id,
              input: testCase.input,
              expectedOutput: testCase.expectedOutput,
              isPublic: testCase.isPublic,
            })
          } else {
            await createTestCase({ ...testCase, problemId: id })
          }
        }
      } else {
        const created = await createProblem(payload.title)
        await updateProblem(created.id, payload)
        for (const testCase of testCases) {
          await createTestCase({ ...testCase, problemId: created.id })
        }
      }
      navigate('/admin/problems')
    } catch (cause) {
      setError(errorMessage(cause, 'Unable to save problem. Please check the values and try again.'))
      setSaving(false)
    }
  }

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
        <CircularProgress />
      </Box>
    )
  }

  return (
    <Box sx={{ maxWidth: 900 }}>
      <Typography variant="h4" component="h1" sx={{ mb: 3 }}>
        {isEdit ? 'Edit Problem' : 'New Problem'}
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <Paper sx={{ p: 3, mb: 3 }}>
        <Typography variant="h6" component="h2" sx={{ mb: 2 }}>
          Problem
        </Typography>
        <Stack spacing={2}>
          <TextField
            label="Title"
            required
            value={title}
            onChange={(event) => {
              setTitle(event.target.value)
              if (!slugTouched) {
                setSlug(deriveSlug(event.target.value))
              }
            }}
          />
          <TextField
            label="Slug"
            value={slug}
            onChange={(event) => {
              setSlugTouched(true)
              setSlug(event.target.value)
            }}
          />
          <TextField
            label="Description"
            multiline
            minRows={4}
            value={description}
            onChange={(event) => setDescription(event.target.value)}
          />
          <FormControl>
            <InputLabel>Difficulty</InputLabel>
            <Select
              label="Difficulty"
              value={difficulty}
              onChange={(event) => setDifficulty(event.target.value as Difficulty)}
            >
              <MenuItem value="EASY">Easy</MenuItem>
              <MenuItem value="MEDIUM">Medium</MenuItem>
              <MenuItem value="HARD">Hard</MenuItem>
            </Select>
          </FormControl>
          <Autocomplete
            multiple
            freeSolo
            options={topics}
            value={selectedTopics}
            getOptionLabel={(option) => typeof option === 'string' ? option : option.name}
            isOptionEqualToValue={(option, value) =>
              typeof option !== 'string' && typeof value !== 'string' && option.id === value.id
            }
            onChange={(_, values) => setSelectedTopics(values)}
            renderInput={(params) => (
              <TextField {...params} label="Topics" helperText="Select existing topics or type a new one and press Enter" />
            )}
          />
          <Stack direction="row" spacing={2}>
            <TextField
              label="Time limit (ms)"
              type="number"
              value={executionTimeLimitMs}
              onChange={(event) => setExecutionTimeLimitMs(event.target.value)}
              sx={{ flexGrow: 1 }}
            />
            <TextField
              label="Memory limit (bytes)"
              type="number"
              value={memoryUsageLimitBytes}
              onChange={(event) => setMemoryUsageLimitBytes(event.target.value)}
              sx={{ flexGrow: 1 }}
            />
          </Stack>

          <Divider />

          <Stack direction="row" sx={{ alignItems: 'center' }}>
            <Typography variant="subtitle1" sx={{ flexGrow: 1 }}>
              Examples
            </Typography>
            <Button
              size="small"
              startIcon={<AddIcon />}
              onClick={() =>
                setExamples((rows) => [...rows, { input: '', output: '', explanation: '' }])
              }
            >
              Add example
            </Button>
          </Stack>
          {examples.map((example, index) => (
            <Stack key={index} direction="row" spacing={2} sx={{ alignItems: 'flex-start' }}>
              <TextField
                label="Input"
                multiline
                value={example.input}
                onChange={(event) => updateExample(index, 'input', event.target.value)}
                sx={{ flexGrow: 1 }}
              />
              <TextField
                label="Expected output"
                multiline
                value={example.output}
                onChange={(event) => updateExample(index, 'output', event.target.value)}
                sx={{ flexGrow: 1 }}
              />
              <TextField
                label="Explanation"
                value={example.explanation}
                onChange={(event) => updateExample(index, 'explanation', event.target.value)}
                sx={{ flexGrow: 1 }}
              />
              <IconButton
                aria-label="Remove example"
                color="error"
                onClick={() => setExamples((rows) => rows.filter((_, i) => i !== index))}
              >
                <DeleteIcon />
              </IconButton>
            </Stack>
          ))}

          <Divider />

          <Stack direction="row" sx={{ alignItems: 'center' }}>
            <Typography variant="subtitle1" sx={{ flexGrow: 1 }}>
              Hints
            </Typography>
            <Button size="small" startIcon={<AddIcon />} onClick={() => setHints((items) => [...items, ''])}>
              Add hint
            </Button>
          </Stack>
          {hints.map((hint, index) => (
            <Stack key={index} direction="row" spacing={2} sx={{ alignItems: 'center' }}>
              <TextField
                label={`Hint ${index + 1}`}
                value={hint}
                onChange={(event) => updateHint(index, event.target.value)}
                sx={{ flexGrow: 1 }}
              />
              <IconButton
                aria-label="Remove hint"
                color="error"
                onClick={() => setHints((items) => items.filter((_, i) => i !== index))}
              >
                <DeleteIcon />
              </IconButton>
            </Stack>
          ))}
        </Stack>
      </Paper>

      <Paper sx={{ p: 3, mb: 3 }}>
        <Stack direction="row" sx={{ alignItems: 'center', mb: 2 }}>
          <Typography variant="h6" component="h2" sx={{ flexGrow: 1 }}>
            Test Cases
          </Typography>
          <Button
            size="small"
            startIcon={<AddIcon />}
            onClick={() =>
              setTestCases((cases) => [
                ...cases,
                { input: '', expectedOutput: '', isPublic: false },
              ])
            }
          >
            Add test case
          </Button>
        </Stack>
        {testCases.length === 0 && (
          <Typography color="text.secondary">
            No test cases. Add at least one so the problem can be judged.
          </Typography>
        )}
        <Stack spacing={2}>
          {testCases.map((testCase, index) => (
            <Stack key={testCase.id ?? `new-${index}`} direction="row" spacing={2} sx={{ alignItems: 'flex-start' }}>
              <TextField
                label="Input"
                multiline
                value={testCase.input}
                onChange={(event) => updateTestCase(index, { input: event.target.value })}
                sx={{ flexGrow: 1 }}
              />
              <TextField
                label="Expected output"
                multiline
                value={testCase.expectedOutput}
                onChange={(event) => updateTestCase(index, { expectedOutput: event.target.value })}
                sx={{ flexGrow: 1 }}
              />
              <FormControlLabel
                control={
                  <Checkbox
                    checked={testCase.isPublic}
                    onChange={(event) => updateTestCase(index, { isPublic: event.target.checked })}
                  />
                }
                label="Public"
                sx={{ mt: 1 }}
              />
              <IconButton
                aria-label="Remove test case"
                color="error"
                onClick={() =>
                  setTestCases((cases) => cases.filter((_, i) => i !== index))
                }
              >
                <DeleteIcon />
              </IconButton>
            </Stack>
          ))}
        </Stack>
      </Paper>

      <Stack direction="row" spacing={2} sx={{ justifyContent: 'flex-end' }}>
        <Button onClick={() => navigate('/admin/problems')} disabled={saving}>
          Cancel
        </Button>
        <Button variant="contained" disabled={saving || loading} onClick={() => void handleSave()}>
          {saving ? 'Saving…' : 'Save'}
        </Button>
      </Stack>
    </Box>
  )
}
