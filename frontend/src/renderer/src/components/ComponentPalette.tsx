import type { Diagram } from '../api/types'

export function ComponentPalette({ diagram }: { diagram: Diagram | null }) {
  return (
    <aside className="palette">
      <h3>Blocks</h3>
      <ul>
        {diagram?.blockTypes.map((type) => (
          <li key={type}>{type}</li>
        ))}
      </ul>
      <h3>Connectors</h3>
      <ul>
        {diagram?.connectorTypes.map((type) => (
          <li key={type}>{type}</li>
        ))}
      </ul>
    </aside>
  )
}
