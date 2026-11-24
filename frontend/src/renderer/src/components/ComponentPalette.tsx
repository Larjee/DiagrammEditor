import type { Diagram } from '../api/types'

interface Props {
  diagram: Diagram | null
  tool: { kind: 'block' | 'connector'; type: string } | null
  busy: boolean
  onTool: (tool: Props['tool']) => void
}

export function ComponentPalette({
  diagram,
  tool, busy, onTool
}: Props) {
  return (
    <aside className="palette">
      <h2>Components</h2>
      {diagram && <>
        <h3>Blocks</h3>
        <ul>
          {diagram.blockTypes.map((type) => (
            <li key={type}>
              <button type="button" draggable={!busy} disabled={busy}
                aria-pressed={tool?.kind === 'block' && tool.type === type}
                onClick={() => onTool(tool?.kind === 'block' && tool.type === type ? null : { kind: 'block', type })}
                onDragStart={(event) => {
                  event.dataTransfer.setData('application/x-diagram-block', type)
                  event.dataTransfer.effectAllowed = 'copy'
                  onTool({ kind: 'block', type })
                }}
                onDragEnd={() => onTool(null)}>
                {label(type)}
              </button>
            </li>
          ))}
        </ul>
        <h3>Connectors</h3>
        <ul>
          {diagram.connectorTypes.map((type) => (
            <li key={type}>
              <span>{label(type)}</span>
            </li>
          ))}
        </ul>
      </>}
    </aside>
  )
}

export function label(type: string) {
  return type.charAt(0).toUpperCase() + type.slice(1)
}
