import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { createMemoryRouter, RouterProvider } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import * as api from '../../api/problems'
import ProblemFormPage from './ProblemFormPage'
import AdminProblemListPage from './AdminProblemListPage'
import { exampleProblem } from '../../test/fixtures'

vi.mock('../../api/problems', () => ({
  fetchTopics: vi.fn(), fetchProblems: vi.fn(), createProblem: vi.fn(), updateProblem: vi.fn(),
  createTestCase: vi.fn(), deleteTestCase: vi.fn(), updateTestCase: vi.fn(), fetchProblem: vi.fn(),
  fetchProblemBySlug: vi.fn(), fetchTestCases: vi.fn(), deleteProblem: vi.fn(), publishProblem: vi.fn(), unpublishProblem: vi.fn(),
}))

describe('custom admin screens', () => {
  beforeEach(() => {
    vi.resetAllMocks()
    vi.mocked(api.fetchTopics).mockResolvedValue([{ id: 'arrays', name: 'Arrays' }])
    vi.mocked(api.fetchProblems).mockResolvedValue([{ problemId: 1, slug: 'two_sum', title: 'Two Sum', difficulty: 'EASY', publishedAt: null }])
    vi.mocked(api.createProblem).mockResolvedValue(exampleProblem)
  })

  it('validates the title and saves topics, hints, limits, and public test cases', async () => {
    const user = userEvent.setup()
    const router = createMemoryRouter([{ path: '/admin/problems/new', element: <ProblemFormPage /> }, { path: '/admin/problems', element: <p>Saved problem list</p> }], { initialEntries: ['/admin/problems/new'] })
    render(<RouterProvider router={router} />)
    await user.click(await screen.findByRole('button', { name: 'Save' }))
    expect(screen.getByRole('alert')).toHaveTextContent('Title is mandatory.')
    await user.type(screen.getByRole('textbox', { name: /Title/ }), 'New Challenge')
    await user.selectOptions(screen.getByRole('combobox', { name: 'Difficulty' }), 'HARD')
    await user.type(screen.getByRole('combobox', { name: 'Topics' }), 'Arrays{Enter}New topic{Enter}')
    await user.type(screen.getByRole('spinbutton', { name: 'Time limit (ms)' }), '1000')
    await user.click(screen.getByRole('button', { name: 'Add hint' }))
    await user.type(screen.getByRole('textbox', { name: 'Hint 1' }), 'Use a map')
    await user.click(screen.getByRole('button', { name: 'Add test case' }))
    await user.type(screen.getByRole('textbox', { name: 'Input' }), '1 2')
    await user.type(screen.getByRole('textbox', { name: 'Expected output' }), '3')
    await user.click(screen.getByRole('checkbox', { name: 'Public' }))
    await user.click(screen.getByRole('button', { name: 'Save' }))
    await screen.findByText('Saved problem list')
    expect(api.updateProblem).toHaveBeenCalledWith(7, expect.objectContaining({ title: 'New Challenge', difficulty: 'HARD', hints: ['Use a map'], topics: [{ topic_id: 'arrays' }, { name: 'New topic' }], constraints: { executionTimeLimitMs: 1000, memoryUsageLimitMb: null } }))
    expect(api.createTestCase).toHaveBeenCalledWith(expect.objectContaining({ problemId: 7, input: '1 2', expectedOutput: '3', isPublic: true }))
  }, 10_000)

  it('publishes a problem and requires a confirmation before deletion', async () => {
    const user = userEvent.setup()
    const router = createMemoryRouter([{ path: '/admin/problems', element: <AdminProblemListPage /> }], { initialEntries: ['/admin/problems'] })
    render(<RouterProvider router={router} />)
    await user.click(await screen.findByRole('button', { name: 'Publish' }))
    await waitFor(() => expect(api.publishProblem).toHaveBeenCalledWith(1))
    await user.click(screen.getByRole('button', { name: 'Delete' }))
    expect(api.deleteProblem).not.toHaveBeenCalled()
    await user.click(within(screen.getByRole('dialog')).getByRole('button', { name: 'Cancel' }))
    expect(api.deleteProblem).not.toHaveBeenCalled()
    await user.click(screen.getByRole('button', { name: 'Delete' }))
    await user.click(within(screen.getByRole('dialog')).getByRole('button', { name: 'Delete' }))
    await waitFor(() => expect(api.deleteProblem).toHaveBeenCalledWith(1))
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument())
  })
})
