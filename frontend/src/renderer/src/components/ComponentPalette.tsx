import type { Diagram } from '../api/types'

interface Props {
  diagram: Diagram | null
}

export function ComponentPalette({
  diagram,
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
