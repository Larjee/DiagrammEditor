import type { Block, Connector, Style } from '../api/types'
import { label } from './ComponentPalette'

interface Props {
  component?: Block | Connector
}

export function StylePanel({
  component,
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
    {component && 'points' in component && <p className="muted">Right-click the connector to add a point.</p>}
  </aside>
}
