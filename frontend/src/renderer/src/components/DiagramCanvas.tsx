import {
  Background, ConnectionMode, Controls, MarkerType, ReactFlow, ReactFlowProvider,
  useReactFlow,
} from '@xyflow/react'
import {
  useRef,
  useEffect,
} from 'react'
import type {
  Diagram,
  Point,
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
  onZoom: (percent: number) => void
  tool: { kind: 'block' | 'connector'; type: string } | null
  onCancelTool: () => void
  onAddBlock: (type: string, point: Point) => void
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
  onZoom,
  tool, onCancelTool,
  onAddBlock,
}: Props & { diagram: Diagram }) {
  const fitOnOpen = useRef(diagram.blocks.length > 0)
  const { screenToFlowPosition } = useReactFlow()

  useEffect(() => {
    const cancel = (event: KeyboardEvent) => {
      if (event.key !== 'Escape') return
      if (tool) onCancelTool()
    }
    window.addEventListener('keydown', cancel)
    return () => window.removeEventListener('keydown', cancel)
  }, [tool, onCancelTool])



  function flowPoint(event: { clientX: number; clientY: number }) {
    return screenToFlowPosition({ x: event.clientX, y: event.clientY })
  }




  const nodes: BlockNode[] = diagram.blocks.map((saved) => {
    const block = saved
    const blockPorts = new Map(block.ports.map((port) => [handleId(port), port]))
    return {
      id: block.id,
      type: 'block',
      position: { x: block.x, y: block.y },
      width: block.width,
      height: block.height,
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
    ariaLabel: connector.type,
    data: {
      connector
    }
  }))



  return <main className="canvas"
    data-placing={tool?.kind === 'block'}
    onDragOver={(event) => {
      if (!busy && event.dataTransfer.types.includes('application/x-diagram-block')) {
        event.preventDefault()
        event.dataTransfer.dropEffect = 'copy'
      }
    }}
    onDrop={(event) => {
      const type = event.dataTransfer.getData('application/x-diagram-block')
      if (busy || tool?.kind !== 'block' || tool.type !== type) return
      event.preventDefault()
      onCancelTool()
      onAddBlock(type, flowPoint(event))
    }}
  >
    <ReactFlow nodes={nodes} edges={edges} nodeTypes={nodeTypes} edgeTypes={edgeTypes}
      nodesDraggable={false} nodesConnectable={false} connectionMode={ConnectionMode.Loose}
      deleteKeyCode={null} fitView={fitOnOpen.current} fitViewOptions={{ maxZoom: 1 }}
      onViewportChange={({ zoom }) => onZoom(Math.round(zoom * 100))}
      onPaneClick={(event) => {
        if (tool?.kind === 'block' && !busy) {
          onCancelTool()
          onAddBlock(tool.type, flowPoint(event))
          return
        }
      }}
    >
      <Background />
      <Controls showInteractive={false} />
    </ReactFlow>
  </main>
}
