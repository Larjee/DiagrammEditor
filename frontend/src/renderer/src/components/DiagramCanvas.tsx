import {
  Background, ConnectionMode, Controls, MarkerType, ReactFlow, ReactFlowProvider,
  useReactFlow,
  ViewportPortal,
} from '@xyflow/react'
import {
  useRef,
  useEffect,
  useState,
  type PointerEvent,
} from 'react'
import type {
  Diagram,
  Block,
  Point,
  PortRef,
} from '../api/types'
import { DiagramBlock, type BlockNode } from './DiagramBlock'
import { DEFAULT_PORTS, handleId, portAnchor, portKey } from '../../../shared/ports'
import { useConnectorRoutes } from './useConnectorRoutes'
import { DiagramConnector, type ConnectorEdge } from './DiagramConnector'
import { usePointerDrag } from './usePointerDrag'

const nodeTypes = { block: DiagramBlock }
const edgeTypes = { connector: DiagramConnector }

interface Props {
  diagram: Diagram | null
  busy: boolean
  onZoom: (percent: number) => void
  tool: { kind: 'block' | 'connector'; type: string } | null
  onCancelTool: () => void
  onConnect: (type: string, source: PortRef, target: PortRef) => void
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
  onConnect,
}: Props & { diagram: Diagram }) {
  const fitOnOpen = useRef(diagram.blocks.length > 0)
  const { screenToFlowPosition } = useReactFlow()
  const beginDrag = usePointerDrag()
  const [wire, setWire] = useState<{ source: PortRef; point: Point }>()

  useEffect(() => {
    const cancel = (event: KeyboardEvent) => {
      if (event.key !== 'Escape') return
      if (tool) onCancelTool()
    }
    window.addEventListener('keydown', cancel)
    return () => window.removeEventListener('keydown', cancel)
  }, [tool, onCancelTool])

  function connect(from: PortRef, to: PortRef) {
    if (!tool || tool.kind !== 'connector' || busy || portKey(from) === portKey(to)) return
    onCancelTool()
    onConnect(tool.type, from, to)
  }

  function startPort(event: PointerEvent<HTMLDivElement>, port: PortRef) {
    if (busy) return
    beginDrag(event, (next) => {
      setWire({ source: port, point: flowPoint(next) })
    }, (next, moved) => {
      if (!moved) {
        return
      }
      const element = document.elementFromPoint(next.clientX, next.clientY)
        ?.closest<HTMLElement>('[data-editor-port]')
      const target = ports.get(element?.dataset.editorPort ?? '')
      if (target) connect(port, target)
    }, () => {
      setWire(undefined)
    })
  }

  const connecting = tool?.kind === 'connector'
  const ports = new Map<string, PortRef>()

  function flowPoint(event: { clientX: number; clientY: number }) {
    return screenToFlowPosition({ x: event.clientX, y: event.clientY })
  }

  const nodes: BlockNode[] = diagram.blocks.map((block) => {
    const blockPorts = new Map(block.ports.map((port) => [handleId(port), port]))
    if (connecting) {
      for (const preset of DEFAULT_PORTS) {
        const port = { blockId: block.id, ...preset }
        if (!blockPorts.has(handleId(port))) blockPorts.set(handleId(port), port)
      }
      for (const port of blockPorts.values()) ports.set(portKey(port), port)
    }
    return {
      id: block.id,
      type: 'block',
      position: { x: block.x, y: block.y },
      width: block.width,
      height: block.height,
      data: {
        block: { ...block, ports: [...blockPorts.values()] },
        connecting,
        onPortDown: startPort,
      }
    }
  })

  const routes = useConnectorRoutes(diagram.blocks, diagram.connectors)
  const edges: ConnectorEdge[] = diagram.connectors.map((connector) => ({
    id: connector.id,
    type: 'connector',
    source: connector.source.blockId,
    target: connector.target.blockId,
    sourceHandle: handleId(connector.source),
    targetHandle: handleId(connector.target),
    markerEnd: { type: MarkerType.ArrowClosed, color: connector.style.borderColor },
    ariaLabel: connector.type,
    data: { connector, route: routes?.[connector.id] }
  }))

  const origin = wire && diagram.blocks.find((block) => block.id === wire.source.blockId)
  const wireStart = origin && wire ? portAnchor(origin, wire.source) : undefined

  return <main className="canvas">
    <ReactFlow nodes={nodes} edges={edges} nodeTypes={nodeTypes} edgeTypes={edgeTypes}
      nodesDraggable={false} nodesConnectable={false} connectionMode={ConnectionMode.Loose}
      deleteKeyCode={null} fitView={fitOnOpen.current} fitViewOptions={{ maxZoom: 1 }}
      onViewportChange={({ zoom }) => onZoom(Math.round(zoom * 100))}
    >
      <Background />
      <Controls showInteractive={false} />
      {wire && wireStart && <ViewportPortal>
        <svg className="wire-preview" aria-hidden="true">
          <line x1={wireStart.x} y1={wireStart.y} x2={wire.point.x} y2={wire.point.y} />
        </svg>
      </ViewportPortal>}
    </ReactFlow>
    {connecting && <div className="canvas-hint" role="status">
      Drag from source port to target port · Esc to cancel
    </div>}
  </main>
}
