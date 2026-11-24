import type { Block, Point, PortRef } from '../renderer/src/api/types'

const sides = ['TOP', 'RIGHT', 'BOTTOM', 'LEFT'] as const
export const DEFAULT_PORTS = sides.map(side => ({ side, offset: 0.5 }))

export function portSide(port: PortRef) {
  return sides.find(side => side === port.side.toUpperCase()) ?? 'RIGHT'
}

export function portKey(port: PortRef) {
  return JSON.stringify([port.blockId, handleId(port)])
}

export function handleId(port: PortRef) {
  return `${port.side}:${port.offset}`
}

export function portAnchor(block: Block, port: PortRef): Point {
  const position = portSide(port)
  if (position === 'LEFT' || position === 'RIGHT') {
    const border = Math.min(block.style.borderWidth, block.height / 2)
    return {
      x: block.x + (position === 'RIGHT' ? block.width : 0),
      y: block.y + border + (block.height - 2 * border) * port.offset
    }
  }
  const border = Math.min(block.style.borderWidth, block.width / 2)
  return {
    x: block.x + border + (block.width - 2 * border) * port.offset,
    y: block.y + (position === 'BOTTOM' ? block.height : 0)
  }
}
