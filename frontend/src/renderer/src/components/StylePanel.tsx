import type { Block, Connector, Style } from '../api/types'
import { label } from './ComponentPalette'

interface Props {
  component?: Block | Connector
  schema?: string | null
}

export function StylePanel({
  component,
  schema,
}: Props) {
  return <aside className="style-panel">
    <h2>Properties</h2>
    {component ? <dl className="component-details">
      <dt>Type</dt><dd>{label(component.type)}</dd>
      {'width' in component && <>
        <dt>Width</dt><dd>{component.width} px</dd>
        <dt>Height</dt><dd>{component.height} px</dd>
      </>}
    </dl> : <p className="muted">Select a component</p>}
    {schema !== undefined && <section className="schema-check">
      <h3>Schema check</h3>
      <p>{schema ? `Strict · ${schema}` : 'Free'}</p>
    </section>}
  </aside>
}
