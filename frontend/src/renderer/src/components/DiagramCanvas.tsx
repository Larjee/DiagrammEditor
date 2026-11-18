import { Background, Controls, ReactFlow, type Edge, type Node } from '@xyflow/react'
import type { Block, Connector, Diagram, LineType } from '../api/types'

const BORDER: Record<LineType, string> = { SOLID: 'solid', DASHED: 'dashed', DOTTED: 'dotted' }
const DASHES: Record<LineType, string | undefined> = { SOLID: undefined, DASHED: '6 4', DOTTED: '2 3' }

export function DiagramCanvas({ diagram }: { diagram: Diagram | null }) {
  if (!diagram) {
    return <main className="canvas empty">No diagram is open</main>
  }
  return (
    <main className="canvas">
      <ReactFlow nodes={diagram.blocks.map(toNode)} edges={diagram.connectors.map(toEdge)} fitView>
        <Background />
        <Controls />
      </ReactFlow>
    </main>
  )
}

function toNode(block: Block): Node {
  return {
    id: block.id,
    position: { x: block.x, y: block.y },
    data: { label: block.type },
    style: {
      width: block.width,
      height: block.height,
      background: block.style.backgroundColor,
      border: `${block.style.borderWidth}px ${BORDER[block.style.lineType]} ${block.style.borderColor}`,
      fontFamily: block.style.font,
      fontSize: block.style.fontSize
    }
  }
}

function toEdge(connector: Connector): Edge {
  return {
    id: connector.id,
    source: connector.source.blockId,
    target: connector.target.blockId,
    label: connector.type,
    style: { stroke: connector.style.borderColor, strokeDasharray: DASHES[connector.style.lineType] }
  }
}
