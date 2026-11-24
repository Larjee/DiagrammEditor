import { BaseEdge, getSmoothStepPath, type Edge, type EdgeProps } from '@xyflow/react'
import type { Connector } from '../api/types'

export type ConnectorEdge = Edge<{
  connector: Connector
}, 'connector'>

const DASHES = { SOLID: undefined, DASHED: '6 4', DOTTED: '2 3' }

export function DiagramConnector(props: EdgeProps<ConnectorEdge>) {
  const { data, id, sourceX, sourceY, targetX, targetY, markerEnd, selected } = props
  if (!data) return null

  const { connector } = data
  const { points, style } = connector
  const path = points.length
    ? `M ${sourceX} ${sourceY} ${points.map(({ x, y }) => `L ${x} ${y}`).join(' ')} L ${targetX} ${targetY}`
    : getSmoothStepPath({ ...props, borderRadius: 0 })[0]

  return <BaseEdge id={id} path={path} markerEnd={markerEnd} style={{
    stroke: style.borderColor,
    strokeWidth: style.borderWidth + (selected ? 1 : 0),
    strokeDasharray: DASHES[style.lineType]
  }} />
}
