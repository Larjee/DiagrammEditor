import { useState } from 'react'
import type { Block, Connector, Style } from '../api/types'
import { label } from './ComponentPalette'

interface Props {
  component?: Block | Connector
  busy: boolean
  onApply: (id: string, style: Style) => Promise<boolean>
}

export function StylePanel({
  component,
  busy, onApply,
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
    {component && <StyleForm component={component} busy={busy} onApply={onApply} />}
  </aside>
}

function StyleForm({ component, busy, onApply }: {
  component: Block | Connector
  busy: boolean
  onApply: (id: string, style: Style) => Promise<boolean>
}) {
  const [draft, setDraft] = useState<Style | null>(null)
  const style = draft ?? component.style

  function update<K extends keyof Style>(key: K, value: Style[K]) {
    setDraft({ ...style, [key]: value })
  }

  return <form className="style-form" onSubmit={async (event) => {
    event.preventDefault()
    if (draft && await onApply(component.id, draft)) setDraft(null)
  }} onKeyDown={(event) => {
    if (event.key === 'Escape' && !busy) setDraft(null)
  }}>
    <h3>Style</h3>
    <fieldset disabled={busy}>
      <label>
        Border color
        <input type="color" value={style.borderColor} onChange={(event) => update('borderColor', event.target.value)} />
      </label>
      <label>
        Border width
        <input type="number" min="0" step="any" required value={Number.isNaN(style.borderWidth) ? '' : style.borderWidth}
          onChange={(event) => update('borderWidth', event.target.valueAsNumber)} />
      </label>
      {'width' in component && <>
        <label>
          Background
          <input type="color" value={style.backgroundColor}
            onChange={(event) => update('backgroundColor', event.target.value)} />
        </label>
        <label>
          Font family
          <select value={style.font} onChange={(event) => update('font', event.target.value)}>
            {[...new Set(['SansSerif', 'Serif', 'Monospaced', style.font])].map((font) => <option key={font}>{font}</option>)}
          </select>
        </label>
        <label>
          Font size
          <input type="number" min="1" step="1" required value={Number.isNaN(style.fontSize) ? '' : style.fontSize}
            onChange={(event) => update('fontSize', event.target.valueAsNumber)} />
        </label>
      </>}
      <label>
        Line
        <select value={style.lineType}
          onChange={(event) => {
            const lineType = event.target.value
            if (lineType === 'SOLID' || lineType === 'DASHED' || lineType === 'DOTTED') update('lineType', lineType)
          }}>
          <option value="SOLID">Solid</option>
          <option value="DASHED">Dashed</option>
          <option value="DOTTED">Dotted</option>
        </select>
      </label>
      <div className="form-actions">
        <button type="button" disabled={!draft} onClick={() => setDraft(null)}>Cancel</button>
        <button type="submit" disabled={!draft}>Apply</button>
      </div>
    </fieldset>
  </form>
}
