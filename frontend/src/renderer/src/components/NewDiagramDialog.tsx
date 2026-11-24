import { useEffect, useRef, useState } from 'react'
import { api } from '../api/client'

interface Props {
  busy: boolean
  error: string | null
  onClose: () => void
  onCreate: (componentSet: string, schema: string | null) => Promise<void>
}

export function NewDiagramDialog({ busy, error, onClose, onCreate }: Props) {
  const dialog = useRef<HTMLDialogElement>(null)
  const [resources, setResources] = useState<{ sets: string[]; schemas: string[] }>()
  const [resourceError, setResourceError] = useState<string>()
  const [componentSet, setComponentSet] = useState('')
  const [schema, setSchema] = useState('')
  const [strict, setStrict] = useState(false)

  useEffect(() => {
    dialog.current?.showModal()
    let active = true
    Promise.all([api.componentSets(), api.schemas()]).then(([sets, schemas]) => {
      if (!active) return
      setResources({ sets, schemas })
      setComponentSet(sets[0] ?? '')
      setSchema(schemas.includes(sets[0]) ? sets[0] : schemas[0] ?? '')
      setStrict(schemas.length > 0)
    }, (cause: unknown) => {
      if (active) setResourceError(cause instanceof Error ? cause.message : String(cause))
    })
    return () => { active = false }
  }, [])

  return <dialog className="new-diagram" ref={dialog} aria-labelledby="new-title"
    onCancel={(event) => { event.preventDefault(); if (!busy) onClose() }}>
    <form onSubmit={(event) => {
      event.preventDefault()
      if (componentSet && (!strict || schema)) void onCreate(componentSet, strict ? schema : null)
    }}>
      <header>
        <h2 id="new-title">New diagram</h2>
        <button type="button" onClick={onClose} disabled={busy} aria-label="Close">×</button>
      </header>
      <div className="new-fields">
        <label>
          Component set
          <select value={componentSet} disabled={busy || !resources} onChange={(event) => {
            const value = event.target.value
            setComponentSet(value)
            if (resources?.schemas.includes(value)) setSchema(value)
          }}>
            {resources?.sets.map((value) => <option key={value}>{value}</option>)}
          </select>
        </label>
        <div className="mode-field" role="group" aria-label="Mode">
          <span>Mode</span>
          <div>
            <label><input type="radio" name="mode" checked={strict}
              disabled={busy || !resources?.schemas.length} onChange={() => setStrict(true)} /> Strict</label>
            <label><input type="radio" name="mode" checked={!strict}
              disabled={busy || !resources} onChange={() => setStrict(false)} /> Free</label>
          </div>
        </div>
        <label>
          Schema
          <select value={schema} disabled={busy || !strict} onChange={(event) => setSchema(event.target.value)}>
            {resources?.schemas.map((value) => <option key={value}>{value}</option>)}
          </select>
        </label>
        {(error ?? resourceError) && <p className="form-error" role="alert">{error ?? resourceError}</p>}
      </div>
      <footer>
        <button type="button" onClick={onClose} disabled={busy}>Cancel</button>
        <button type="submit" disabled={busy || !componentSet || (strict && !schema)}>Create</button>
      </footer>
    </form>
  </dialog>
}
