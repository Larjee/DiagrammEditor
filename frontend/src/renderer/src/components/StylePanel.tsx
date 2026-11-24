import type { Block, Connector, Style } from '../api/types'
import { label } from './ComponentPalette'

interface Props {
  component?: Block | Connector
  previewSize: { width: number; height: number } | null
  keepProportions: boolean
  onKeepProportions: (value: boolean) => void
}

export function StylePanel({
  component,
  previewSize, keepProportions, onKeepProportions,
}: Props) {
  return <aside className="style-panel">
    <h2>Properties</h2>
    {component ? <dl className="component-details">
      <dt>Type</dt><dd>{label(component.type)}</dd>
      {'width' in component && <>
        <dt>Width</dt><dd>{previewSize?.width ?? component.width} px</dd>
        <dt>Height</dt><dd>{previewSize?.height ?? component.height} px</dd>
      </>}
    </dl> : <p className="muted">Select a component</p>}
    {component && 'width' in component && <label className="proportions">
      <input type="checkbox" checked={keepProportions}
        onChange={(event) => onKeepProportions(event.target.checked)} />
      Keep proportions
    </label>}
  </aside>
}
