import { Handle, Position, useUpdateNodeInternals, type Node, type NodeProps } from '@xyflow/react'
import { useEffect, type PointerEvent } from 'react'
import type { Block, PortRef } from '../api/types'
import { label } from './ComponentPalette'

export type BlockNode = Node<{
  block: Block
  move?: (event: PointerEvent<HTMLDivElement>) => void
}, 'block'>

const SIDES: Record<string, Position> = {
  TOP: Position.Top, RIGHT: Position.Right, BOTTOM: Position.Bottom, LEFT: Position.Left
}


export function handleId(port: PortRef) {
  return `${port.side}:${port.offset}`
}

export function portPosition(port: PortRef) {
  return SIDES[port.side.toUpperCase()] ?? Position.Right
}

const LOGICAL_FONTS: Record<string, string> = {
  SansSerif: 'sans-serif', Serif: 'serif', Monospaced: 'monospace'
}

export function DiagramBlock({ data }: NodeProps<BlockNode>) {
  const { block } = data
  const { style } = block
  const updateNodeInternals = useUpdateNodeInternals()
  const portsSignature = block.ports.map(handleId).join('|')
  useEffect(() => {
    updateNodeInternals(block.id)
  }, [block.id, portsSignature, updateNodeInternals])

  return <div className="diagram-block nopan" style={{
    backgroundColor: style.backgroundColor,
    border: `${style.borderWidth}px ${style.lineType.toLowerCase()} ${style.borderColor}`,
    fontFamily: LOGICAL_FONTS[style.font] ?? style.font,
    fontSize: style.fontSize
  }}
    onPointerDown={data.move}
  >
    <div className="block-heading" style={{ borderColor: style.borderColor }}>{label(block.type)}</div>
    {block.ports.map((port) => {
      const position = portPosition(port)
      const vertical = position === Position.Left || position === Position.Right
      return <Handle key={handleId(port)} id={handleId(port)} type="source" position={position}
        className="diagram-port nodrag nopan"
        isConnectable={false}
        style={{ [vertical ? 'top' : 'left']: `${port.offset * 100}%` }}
      />
    })}
  </div>
}
