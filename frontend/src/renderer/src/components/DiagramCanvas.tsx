import {
  Background, ConnectionMode, Controls, MarkerType, ReactFlow, ReactFlowProvider,
  useReactFlow,
  ViewportPortal,
} from '@xyflow/react'
import {
  useRef,
  useMemo,
  useEffect,
  useState,
  type PointerEvent,
} from 'react'
import type {
  Diagram,
  Connector,
  Point,
} from '../api/types'
import { DiagramBlock, type BlockNode } from './DiagramBlock'
import { handleId } from '../../../shared/ports'
import { useConnectorRoutes } from './useConnectorRoutes'
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
  onAddPoint: (id: string, point: Point) => void
  onMovePoint: (id: string, index: number, point: Point) => void
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
  onAddPoint, onMovePoint,
}: Props & { diagram: Diagram }) {
  const fitOnOpen = useRef(diagram.blocks.length > 0)
  const { screenToFlowPosition } = useReactFlow()
  const beginDrag = usePointerDrag()
  const [pointPreview, setPointPreview] = useState<{ id: string; index: number; point: Point }>()
  const [pathMenu, setPathMenu] = useState<{ id: string; screen: Point; point: Point }>()

  useEffect(() => {
    const cancel = (event: KeyboardEvent) => {
      if (event.key !== 'Escape') return
      setPathMenu(undefined)
    }
    window.addEventListener('keydown', cancel)
    return () => window.removeEventListener('keydown', cancel)
  }, [])

  function flowPoint(event: { clientX: number; clientY: number }) {
    return screenToFlowPosition({ x: event.clientX, y: event.clientY })
  }

  function startPoint(event: PointerEvent<HTMLButtonElement>, connector: Connector, index: number) {
    if (busy) return
    const start = flowPoint(event)
    const original = connector.points[index]
    const position = (next: globalThis.PointerEvent) => {
      const point = flowPoint(next)
      return { x: original.x + point.x - start.x, y: original.y + point.y - start.y }
    }
    beginDrag(event, (next) => setPointPreview({ id: connector.id, index, point: position(next) }),
      (next, moved) => { if (moved) onMovePoint(connector.id, index, position(next)) },
      () => setPointPreview(undefined))
  }

  const nodes: BlockNode[] = diagram.blocks.map((block) => {
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

  const connectors = useMemo(() => pointPreview
    ? diagram.connectors.map(connector => connector.id === pointPreview.id ? {
      ...connector,
      points: connector.points.map((point, index) => index === pointPreview.index ? pointPreview.point : point)
    } : connector)
    : diagram.connectors, [diagram.connectors, pointPreview])
  const routes = useConnectorRoutes(diagram.blocks, connectors)
  const edges: ConnectorEdge[] = connectors.map((connector) => ({
    id: connector.id,
    type: 'connector',
    source: connector.source.blockId,
    target: connector.target.blockId,
    sourceHandle: handleId(connector.source),
    targetHandle: handleId(connector.target),
    markerEnd: { type: MarkerType.ArrowClosed, color: connector.style.borderColor },
    selected: selected === connector.id,
    ariaLabel: connector.type,
    data: { connector, route: routes?.[connector.id] }
  }))

  const selectedConnector = edges.find((edge) => edge.id === selected)?.data?.connector

  return <main className="canvas">
    <ReactFlow nodes={nodes} edges={edges} nodeTypes={nodeTypes} edgeTypes={edgeTypes}
      nodesDraggable={false} nodesConnectable={false} connectionMode={ConnectionMode.Loose}
      deleteKeyCode={null} fitView={fitOnOpen.current} fitViewOptions={{ maxZoom: 1 }}
      onViewportChange={({ zoom }) => onZoom(Math.round(zoom * 100))}
      onNodeClick={(_event, node) => onSelect(node.id)}
      onEdgeClick={(_event, edge) => onSelect(edge.id)}
      onEdgeContextMenu={(event, edge) => {
        event.preventDefault()
        if (busy) return
        onSelect(edge.id)
        const bounds = event.currentTarget.closest('.canvas')?.getBoundingClientRect()
        if (bounds) setPathMenu({
          id: edge.id,
          screen: { x: event.clientX - bounds.left, y: event.clientY - bounds.top },
          point: flowPoint(event)
        })
      }}
      onPaneClick={(event) => {
        setPathMenu(undefined)
        onSelect(null)
      }}
    >
      <Background />
      <Controls showInteractive={false} />
      {selectedConnector && <ViewportPortal>
        {selectedConnector.points.map((point, index) => (
          <button key={index} type="button" className="path-point nodrag nopan"
            aria-label={`Inflection point ${index + 1}`}
            style={{ transform: `translate(${point.x}px, ${point.y}px) translate(-50%, -50%)` }}
            onPointerDown={(event) => startPoint(event, selectedConnector, index)} />
        ))}
      </ViewportPortal>}
    </ReactFlow>
    {pathMenu && <div className="path-menu" style={{ left: pathMenu.screen.x, top: pathMenu.screen.y }}>
      <button type="button" onClick={() => {
        setPathMenu(undefined)
        onAddPoint(pathMenu.id, pathMenu.point)
      }}>Add point</button>
    </div>}
  </main>
}
