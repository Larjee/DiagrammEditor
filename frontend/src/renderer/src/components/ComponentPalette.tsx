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
              <span>{label(type)}</span>
            </li>
          ))}
        </ul>
        <h3>Connectors</h3>
        <ul>
          {diagram.connectorTypes.map((type) => (
            <li key={type}>
              <button type="button" disabled={busy}
                aria-pressed={tool?.kind === 'connector' && tool.type === type}
                onClick={() => onTool(tool?.kind === 'connector' && tool.type === type ? null : { kind: 'connector', type })}>
                {label(type)}
              </button>
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
