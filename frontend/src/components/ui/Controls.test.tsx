import { useState } from 'react'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'
import { Button, Modal, TopicPicker } from './Controls'
import type { Topic } from '../../api/problems'
import ProblemFilters from '../problem/ProblemFilters'
import type { Difficulty } from '../../api/problems'

const topics = [{ id: 'arrays', name: 'Arrays' }, { id: 'graphs', name: 'Graphs' }]

function Filters() {
  const [search, setSearch] = useState('')
  const [difficulty, setDifficulty] = useState<Difficulty[]>([])
  const [selectedTopics, setSelectedTopics] = useState<string[]>([])
  return <><ProblemFilters search={search} onSearchChange={setSearch} selectedDifficulties={difficulty} onSelectedDifficultiesChange={setDifficulty} topics={topics} selectedTopics={selectedTopics} onSelectedTopicsChange={setSelectedTopics} hasFilters={!!search || difficulty.length > 0 || selectedTopics.length > 0} onClearFilters={() => { setSearch(''); setDifficulty([]); setSelectedTopics([]) }} /><output>{JSON.stringify({ difficulty, selectedTopics })}</output></>
}

describe('custom controls', () => {
  it('selects multiple filters, dismisses with Escape, and clears all values', async () => {
    const user = userEvent.setup()
    render(<Filters />)
    expect(screen.getByRole('button', { name: 'Clear' })).toBeDisabled()
    await user.type(screen.getByRole('searchbox'), 'sum')
    await user.click(screen.getByRole('button', { name: 'Difficulty' }))
    await user.click(screen.getByRole('checkbox', { name: 'Easy' }))
    await user.click(screen.getByRole('checkbox', { name: 'Hard' }))
    await user.keyboard('{Escape}')
    expect(screen.getByRole('button', { name: /Difficulty/ })).toHaveFocus()
    expect(screen.queryByRole('checkbox')).not.toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Topic' }))
    await user.click(screen.getByRole('checkbox', { name: 'Arrays' }))
    expect(screen.getByRole('status')).toHaveTextContent('"difficulty":["EASY","HARD"],"selectedTopics":["Arrays"]')
    await user.click(screen.getByRole('button', { name: 'Clear' }))
    expect(screen.getByRole('searchbox')).toHaveValue('')
    expect(screen.getByRole('status')).toHaveTextContent('"difficulty":[],"selectedTopics":[]')
  })

  it('retains existing topic identities, creates new topics, and prevents duplicates', async () => {
    const user = userEvent.setup()
    function Picker() {
      const [value, setValue] = useState<(Topic | string)[]>([])
      return <><TopicPicker options={topics} value={value} onChange={setValue} /><output>{JSON.stringify(value)}</output></>
    }
    render(<Picker />)
    const input = screen.getByRole('combobox', { name: 'Topics' })
    await user.type(input, 'Arrays{Enter}')
    expect(screen.getByRole('status')).toHaveTextContent('"id":"arrays"')
    await user.type(input, 'Dynamic programming')
    await user.click(screen.getByRole('button', { name: 'Add topic' }))
    await user.type(input, 'arrays{Enter}')
    expect(screen.getAllByRole('button', { name: /Remove topic/ })).toHaveLength(2)
    await user.click(screen.getByRole('button', { name: 'Remove topic Arrays' }))
    expect(screen.getByRole('status')).toHaveTextContent('["Dynamic programming"]')
  })

  it('contains dialog focus, dismisses with Escape, and restores the trigger', async () => {
    const user = userEvent.setup()
    function DialogExample() {
      const [open, setOpen] = useState(false)
      return <><Button onClick={() => setOpen(true)}>Open dialog</Button><Modal title="Delete problem" open={open} onClose={() => setOpen(false)}><Button onClick={() => setOpen(false)}>Cancel</Button><Button>Delete</Button></Modal></>
    }
    render(<DialogExample />)
    await user.click(screen.getByRole('button', { name: 'Open dialog' }))
    expect(screen.getByRole('dialog', { name: 'Delete problem' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Cancel' })).toHaveFocus()
    await user.tab({ shift: true })
    expect(screen.getByRole('button', { name: 'Delete' })).toHaveFocus()
    await user.tab()
    expect(screen.getByRole('button', { name: 'Cancel' })).toHaveFocus()
    await user.keyboard('{Escape}')
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Open dialog' })).toHaveFocus()
  })
})
