import { BaseEdge, getSmoothStepPath, Position, type Edge, type EdgeProps } from '@xyflow/react'
import type { Connector, Point } from '../api/types'

export type ConnectorEdge = Edge<{
  connector: Connector
  route?: Point[]
}, 'connector'>

const DASHES = { SOLID: undefined, DASHED: '6 4', DOTTED: '2 3' }

const PORT_LEADS: Record<Position, Point> = {
  [Position.Left]: { x: -12, y: 0 },
  [Position.Right]: { x: 12, y: 0 },
  [Position.Top]: { x: 0, y: -12 },
  [Position.Bottom]: { x: 0, y: 12 }
}

export function DiagramConnector(props: EdgeProps<ConnectorEdge>) {
  const { data, id, markerEnd, selected } = props
  if (!data) return null

  const { connector, route } = data
  const { points, style } = connector
  const path = route ? routedPath(props, route) : points.length
    ? pathThroughPoints(props, points)
    : getSmoothStepPath({ ...props, borderRadius: 0 })[0]

  return <BaseEdge id={id} path={path} markerEnd={markerEnd} style={{
    stroke: style.borderColor,
    strokeWidth: style.borderWidth + (selected ? 1 : 0),
    strokeDasharray: DASHES[style.lineType]
  }} />
}

function routedPath(props: EdgeProps<ConnectorEdge>, route: Point[]) {
  const points = [...route]
  const start = points[0]
  const end = points[points.length - 1]
  points[0] = start.x === points[1].x ? { ...start, y: props.sourceY } : { ...start, x: props.sourceX }
  const beforeEnd = route[route.length - 2]
  points[points.length - 1] = end.x === beforeEnd.x ? { ...end, y: props.targetY } : { ...end, x: props.targetX }
  return points.map(({ x, y }, index) => `${index ? 'L' : 'M'} ${x} ${y}`).join(' ')
}

function pathThroughPoints({ sourceX, sourceY, sourcePosition, targetX, targetY, targetPosition }: EdgeProps<ConnectorEdge>, points: Point[]) {
  const sourceLead = PORT_LEADS[sourcePosition]
  const targetLead = PORT_LEADS[targetPosition]
  let current = { x: sourceX + sourceLead.x, y: sourceY + sourceLead.y }
  let horizontal = sourceLead.x !== 0
  const route = [current]

  for (const point of [...points, { x: targetX + targetLead.x, y: targetY + targetLead.y }]) {
    const sameX = current.x === point.x
    const sameY = current.y === point.y
    if (sameX && sameY) continue

    if (!sameX && !sameY) {
      route.push(horizontal ? { x: point.x, y: current.y } : { x: current.x, y: point.y })
    } else {
      horizontal = sameX
    }
    route.push(point)
    current = point
  }

  route.push({ x: targetX, y: targetY })
  return `M ${sourceX} ${sourceY} ${route.map(({ x, y }) => `L ${x} ${y}`).join(' ')}`
}
