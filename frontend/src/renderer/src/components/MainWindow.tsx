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
  const [selected, setSelected] = useState<string | null>(null)
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

  const component = diagram?.blocks.find((block) => block.id === selected)
    ?? diagram?.connectors.find((connector) => connector.id === selected)

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
      />
      <DiagramCanvas diagram={diagram}
        busy={busy}
        onZoom={setZoom}
        selected={selected}
        onSelect={setSelected}
        onAddPoint={(id, point) => { void edit(() => api.addInflectionPoint(id, point)) }}
        onMovePoint={(id, index, point) => { void edit(() => api.moveInflectionPoint(id, index, point)) }}
      />
      <StylePanel
        component={component}
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
