import {
  Background, ConnectionMode, Controls, MarkerType, ReactFlow, ReactFlowProvider,
  useReactFlow,
} from '@xyflow/react'
import {
  useRef,
  useState,
  type PointerEvent,
} from 'react'
import type {
  Diagram,
  Block,
  Point,
} from '../api/types'
import {
  DiagramBlock, handleId, type BlockNode,
  type ResizeDirection,
} from './DiagramBlock'
import { DiagramConnector, type ConnectorEdge } from './DiagramConnector'
import { usePointerDrag } from './usePointerDrag'

const nodeTypes = { block: DiagramBlock }
const edgeTypes = { connector: DiagramConnector }

interface Props {
  diagram: Diagram | null
  busy: boolean
  onZoom: (percent: number) => void
  selected: string | null
  onSelect: (id: string | null) => void
  keepProportions: boolean
  onPreviewSize: (size: { width: number; height: number } | null) => void
  onResize: (id: string, width: number, height: number) => void
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
  selected, onSelect,
  keepProportions, onPreviewSize, onResize,
}: Props & { diagram: Diagram }) {
  const fitOnOpen = useRef(diagram.blocks.length > 0)
  const { screenToFlowPosition } = useReactFlow()
  const beginDrag = usePointerDrag()
  const [blockPreview, setBlockPreview] = useState<Block>()




  function flowPoint(event: { clientX: number; clientY: number }) {
    return screenToFlowPosition({ x: event.clientX, y: event.clientY })
  }

  function startResize(event: PointerEvent<HTMLButtonElement>, block: Block, direction: ResizeDirection) {
    if (busy) return
    const start = flowPoint(event)
    const size = (next: globalThis.PointerEvent) => {
      const point = flowPoint(next)
      let width = direction === 'south' ? block.width : Math.max(1, Math.round(block.width + point.x - start.x))
      let height = direction === 'east' ? block.height : Math.max(1, Math.round(block.height + point.y - start.y))
      if (keepProportions || next.shiftKey) {
        const horizontal = width / block.width
        const vertical = height / block.height
        const factor = direction === 'east' || Math.abs(horizontal - 1) >= Math.abs(vertical - 1)
          ? horizontal
          : vertical
        width = Math.max(1, Math.round(block.width * factor))
        height = Math.max(1, Math.round(block.height * factor))
      }
      return { width, height }
    }
    beginDrag(event, (next) => {
      const dimensions = size(next)
      setBlockPreview({ ...block, ...dimensions })
      onPreviewSize(dimensions)
    }, (next, moved) => {
      if (!moved) return
      const { width, height } = size(next)
      if (width !== block.width || height !== block.height) onResize(block.id, width, height)
    }, () => {
      setBlockPreview(undefined)
      onPreviewSize(null)
    })
  }



  const nodes: BlockNode[] = diagram.blocks.map((saved) => {
    const block = blockPreview?.id === saved.id ? blockPreview : saved
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
        resize: selected === block.id && !busy ? (event, direction) => startResize(event, block, direction) : undefined,
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
      onViewportChange={({ zoom }) => onZoom(Math.round(zoom * 100))}
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
