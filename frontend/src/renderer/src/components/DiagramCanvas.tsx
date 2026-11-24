import {
  Background, ConnectionMode, Controls, MarkerType, ReactFlow, ReactFlowProvider,
} from '@xyflow/react'
import {
  useRef,
  useEffect,
  useState,
  type PointerEvent,
} from 'react'
import type {
  Diagram,
  Point,
  PortRef,
} from '../api/types'
import { DiagramBlock, type BlockNode } from './DiagramBlock'
import { DEFAULT_PORTS, handleId, portKey } from '../../../shared/ports'
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
  const beginDrag = usePointerDrag()
  const [source, setSource] = useState<PortRef>()

  useEffect(() => {
    const cancel = (event: KeyboardEvent) => {
      if (event.key !== 'Escape') return
      if (tool) onCancelTool()
    }
    window.addEventListener('keydown', cancel)
    return () => window.removeEventListener('keydown', cancel)
  }, [tool, onCancelTool])

  useEffect(() => { setSource(undefined) }, [tool])

  function choosePort(port: PortRef) {
    if (!tool || tool.kind !== 'connector' || busy) return
    if (!source) {
      setSource(port)
    } else if (portKey(source) !== portKey(port)) {
      connect(source, port)
    }
  }

  function connect(from: PortRef, to: PortRef) {
    if (!tool || tool.kind !== 'connector' || busy || portKey(from) === portKey(to)) return
    onCancelTool()
    onConnect(tool.type, from, to)
  }

  function startPort(event: PointerEvent<HTMLDivElement>, port: PortRef) {
    if (busy) return
    beginDrag(event, (next) => {
    }, (next, moved) => {
      if (!moved) {
        choosePort(port)
        return
      }
    }, () => {
    })
  }

  const connecting = tool?.kind === 'connector'
  const ports = new Map<string, PortRef>()

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
        source,
        onPortKey: choosePort,
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

  return <main className="canvas">
    <ReactFlow nodes={nodes} edges={edges} nodeTypes={nodeTypes} edgeTypes={edgeTypes}
      nodesDraggable={false} nodesConnectable={false} connectionMode={ConnectionMode.Loose}
      deleteKeyCode={null} fitView={fitOnOpen.current} fitViewOptions={{ maxZoom: 1 }}
      onViewportChange={({ zoom }) => onZoom(Math.round(zoom * 100))}
    >
      <Background />
      <Controls showInteractive={false} />
    </ReactFlow>
    {connecting && <div className="canvas-hint" role="status">
      {source ? 'Select target port · Esc to cancel' :
        'Select source port · Esc to cancel'
      }
    </div>}
  </main>
}
