import { useEffect, useRef, useState } from 'react'
import { api, ApiError } from '../api/client'
import type { Diagram } from '../api/types'
import { ComponentPalette } from './ComponentPalette'
import { DiagramCanvas } from './DiagramCanvas'
import { StylePanel } from './StylePanel'

export function MainWindow() {
  const [diagram, setDiagram] = useState<Diagram | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(true)
  const pending = useRef(true)
  const [tool, setTool] = useState<{ kind: 'block' | 'connector'; type: string } | null>(null)
  const [zoom, setZoom] = useState(100)

  useEffect(() => {
    let active = true
    api.diagram().then(
      (value) => { if (active) setDiagram(value) },
      (cause: unknown) => {
        if (active && !(cause instanceof ApiError && cause.status === 409)) {
          setError(message(cause))
        }
      }
    ).finally(() => {
      if (active) {
        pending.current = false
        setBusy(false)
      }
    })
    return () => { active = false }
  }, [])

  async function change(operation: () => Promise<Diagram | null>, newDocument = false) {
    if (pending.current) return false
    pending.current = true
    setBusy(true)
    setError(null)
    try {
      const value = await operation()
      if (!value) return false
      setDiagram(value)
      return true
    } catch (cause) {
      setError(message(cause))
      return false
    } finally {
      pending.current = false
      setBusy(false)
    }
  }

  function edit(operation: () => Promise<unknown>) {
    return change(async () => {
      await operation()
      return api.diagram()
    })
  }

  return (
    <div className="main-window">
      <header className="toolbar">
      </header>
      {error && <div className="error" role="alert">
        <span>{error}</span>
        <button type="button" aria-label="Dismiss error" onClick={() => setError(null)}>×</button>
      </div>}
      <ComponentPalette diagram={diagram}
        tool={tool}
        onTool={(value) => {
          setTool(value)
        }}
        busy={busy}
      />
      <DiagramCanvas diagram={diagram}
        busy={busy}
        onZoom={setZoom}
        tool={tool}
        onCancelTool={() => setTool(null)}
        onConnect={(type, source, target) => { void edit(() => api.addConnector(type, source, target)) }}
      />
      <StylePanel
      />
      <footer className="status-bar">
        {diagram ? <>
          <span>Mode: {diagram.mode === 'STRICT' ? 'Strict' : 'Free'}</span>
          <span>Schema: {diagram.schema ?? 'None'}</span>
          <span>Set: {diagram.componentSet}</span>
          <span title={diagram.filePath ?? undefined}>
            {diagram.name ?? diagram.filePath?.split(/[\\/]/).pop() ?? 'Untitled diagram'}
          </span>
          <span>Zoom {zoom}%</span>
        </> : <span>{busy ? 'Loading…' : 'No diagram is open'}</span>}
      </footer>
    </div>
  )
}

function message(cause: unknown) {
  return cause instanceof Error ? cause.message : String(cause)
}
