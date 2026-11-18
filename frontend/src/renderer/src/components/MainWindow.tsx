import { useEffect, useState } from 'react'
import { api, ApiError } from '../api/client'
import type { Diagram } from '../api/types'
import { ComponentPalette } from './ComponentPalette'
import { DiagramCanvas } from './DiagramCanvas'
import { StylePanel } from './StylePanel'

export function MainWindow() {
  const [diagram, setDiagram] = useState<Diagram | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    api.diagram().then(setDiagram, (e: unknown) => {
      if (!(e instanceof ApiError && e.status === 409)) {
        setError(e instanceof Error ? e.message : String(e))
      }
    })
  }, [])

  return (
    <div className="main-window">
      <header className="toolbar">
        <strong>Diagram Editor</strong>
        {diagram && (
          <span>
            {diagram.componentSet} · {diagram.schema ?? 'no schema'} ·{' '}
            {diagram.mode === 'STRICT' ? 'strict mode' : 'free mode'}
          </span>
        )}
      </header>
      {error && <div className="error">{error}</div>}
      <ComponentPalette diagram={diagram} />
      <DiagramCanvas diagram={diagram} />
      <StylePanel />
    </div>
  )
}
