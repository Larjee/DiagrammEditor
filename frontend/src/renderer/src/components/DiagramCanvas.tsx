import {
  Background, ConnectionMode, Controls, MarkerType, ReactFlow, ReactFlowProvider,
} from '@xyflow/react'
import {
  useRef,
} from 'react'
import type {
  Diagram,
} from '../api/types'
import {
  DiagramBlock, handleId, type BlockNode,
} from './DiagramBlock'
import { DiagramConnector, type ConnectorEdge } from './DiagramConnector'

const nodeTypes = { block: DiagramBlock }
const edgeTypes = { connector: DiagramConnector }

interface Props {
  diagram: Diagram | null
  busy: boolean
  selected: string | null
  onSelect: (id: string | null) => void
}

export function DiagramCanvas(props: Props) {
  if (!props.diagram) return <main className="canvas empty">No diagram is open</main>

  return <ReactFlowProvider>
    <Canvas {...props} diagram={props.diagram} />
  </ReactFlowProvider>
}

function Canvas({
  diagram,
  busy,
  selected, onSelect,
}: Props & { diagram: Diagram }) {
  const fitOnOpen = useRef(diagram.blocks.length > 0)








  const nodes: BlockNode[] = diagram.blocks.map((saved) => {
    const block = saved
    const blockPorts = new Map(block.ports.map((port) => [handleId(port), port]))
    return {
      id: block.id,
      type: 'block',
      position: { x: block.x, y: block.y },
      width: block.width,
      height: block.height,
      selected: selected === block.id,
      data: {
        block: { ...block, ports: [...blockPorts.values()] },
      }
    }
  })

  const edges: ConnectorEdge[] = diagram.connectors.map((connector) => ({
    id: connector.id,
    type: 'connector',
    source: connector.source.blockId,
    target: connector.target.blockId,
    sourceHandle: handleId(connector.source),
    targetHandle: handleId(connector.target),
    markerEnd: { type: MarkerType.ArrowClosed, color: connector.style.borderColor },
    selected: selected === connector.id,
    ariaLabel: connector.type,
    data: {
      connector
    }
  }))



  return <main className="canvas"
  >
    <ReactFlow nodes={nodes} edges={edges} nodeTypes={nodeTypes} edgeTypes={edgeTypes}
      nodesDraggable={false} nodesConnectable={false} connectionMode={ConnectionMode.Loose}
      deleteKeyCode={null} fitView={fitOnOpen.current} fitViewOptions={{ maxZoom: 1 }}
      onNodeClick={(_event, node) => onSelect(node.id)}
      onEdgeClick={(_event, edge) => onSelect(edge.id)}
      onPaneClick={(event) => {
        onSelect(null)
      }}
    >
      <Background />
      <Controls showInteractive={false} />
    </ReactFlow>
  </main>
}
