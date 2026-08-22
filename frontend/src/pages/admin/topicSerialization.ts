import type { ProblemTopicRequest, Topic } from '../../api/problems'

export function serializeTopics(values: Array<Topic | string>, catalog: Topic[]): ProblemTopicRequest[] {
  const seen = new Set<string>()
  const byName = new Map(catalog.map((topic) => [topic.name.trim().toLocaleLowerCase(), topic]))
  return values.flatMap((value) => {
    const name = (typeof value === 'string' ? value : value.name).trim()
    const key = name.toLocaleLowerCase()
    if (!name || seen.has(key)) return []
    seen.add(key)
    const existing = typeof value === 'string' ? byName.get(key) : value
    return [existing ? { topic_id: existing.id } : { name }]
  })
}
