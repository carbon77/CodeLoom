import type { Difficulty, Topic } from '../../api/problems'
import { Button, Field, MultiSelect } from '../ui/Controls'
import s from './ProblemFilters.module.css'

interface ProblemFiltersProps {
  search: string
  onSearchChange: (value: string) => void
  selectedDifficulties: Difficulty[]
  onSelectedDifficultiesChange: (values: Difficulty[]) => void
  topics: Topic[]
  selectedTopics: string[]
  onSelectedTopicsChange: (values: string[]) => void
  hasFilters: boolean
  onClearFilters: () => void
}
const difficulties: Difficulty[] = ['EASY', 'MEDIUM', 'HARD']
export default function ProblemFilters(props: ProblemFiltersProps) {
  return <section className={s.filters} aria-label="Problem filters">
    <Field className={s.search} aria-label="Search by title" placeholder="Search problems…" type="search" value={props.search} onChange={(event) => props.onSearchChange(event.target.value)} />
    <MultiSelect label="Difficulty" options={difficulties.map((value) => ({ value, label: value.charAt(0) + value.slice(1).toLowerCase() }))} value={props.selectedDifficulties} onChange={(values) => props.onSelectedDifficultiesChange(values as Difficulty[])} />
    <MultiSelect label="Topic" options={props.topics.map((topic) => ({ value: topic.name, label: topic.name }))} value={props.selectedTopics} onChange={props.onSelectedTopicsChange} />
    <Button disabled={!props.hasFilters} onClick={props.onClearFilters}>Clear</Button>
  </section>
}
