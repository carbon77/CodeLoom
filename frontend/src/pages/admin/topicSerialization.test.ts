import { describe, expect, it } from 'vitest'
import { serializeTopics } from './topicSerialization'

describe('topic serialization', () => {
  it('deduplicates, matches catalog names, and omits blanks', () => {
    const catalog = [{ id: 'arrays-id', name: 'Arrays' }]
    expect(serializeTopics([catalog[0], ' arrays ', ' Graphs ', 'graphs', ' '], catalog)).toEqual([
      { topic_id: 'arrays-id' }, { name: 'Graphs' },
    ])
  })
})
