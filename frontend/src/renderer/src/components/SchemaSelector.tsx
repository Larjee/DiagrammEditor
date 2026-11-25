import { useEffect, useState } from 'react'
import { api } from '../api/client'

export function SchemaSelector({ schema, disabled, onError, onSelect }: {
  schema: string | null
  disabled: boolean
  onError: (error: unknown) => void
  onSelect: (schema: string | null) => void
}) {
  const [schemas, setSchemas] = useState<string[]>([])

  useEffect(() => {
    let active = true
    api.schemas().then(
      (values) => { if (active) setSchemas(values) },
      (cause: unknown) => { if (active) onError(cause) }
    )
    return () => { active = false }
  }, [])

  const options = schema && !schemas.includes(schema) ? [...schemas, schema] : schemas
  return <label>
    Schema
    <select disabled={disabled} value={schema ?? ''} onChange={(event) => onSelect(event.target.value || null)}>
      <option value="">None (Free)</option>
      {options.map((value) => <option key={value}>{value}</option>)}
    </select>
  </label>
}
