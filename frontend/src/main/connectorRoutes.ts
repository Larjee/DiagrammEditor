import { AvoidLib, type PinEnd } from 'libavoid-js'
import type { Block, Connector, Point } from '../renderer/src/api/types'
import { portAnchor, portKey, portSide } from '../shared/ports'

let ready: Promise<void> | undefined

function distance(a: Point, b: Point) {
  return Math.abs(a.x - b.x) + Math.abs(a.y - b.y)
}

function passesThrough(route: Point[], point: Point) {
  return route.some((end, index) => {
    if (index === 0) return false
    const start = route[index - 1]
    return Math.abs(distance(start, point) + distance(point, end) - distance(start, end)) < 0.001
  })
}

export async function connectorRoutes(blocks: Block[], connectors: Connector[]) {
  const routes: Record<string, Point[]> = {}
  await (ready ??= AvoidLib.load())
  const avoid = AvoidLib.getInstance()

  const router = new avoid.Router(avoid.RouterFlag.OrthogonalRouting)
  const directions = {
    LEFT: avoid.ConnDirFlag.ConnDirLeft, RIGHT: avoid.ConnDirFlag.ConnDirRight,
    TOP: avoid.ConnDirFlag.ConnDirUp, BOTTOM: avoid.ConnDirFlag.ConnDirDown
  }
  try {
    router.setRoutingParameter(avoid.RoutingParameter.shapeBufferDistance, 12)
    router.setRoutingParameter(avoid.RoutingParameter.segmentPenalty, 400)
    router.setRoutingParameter(avoid.RoutingParameter.crossingPenalty, 700)
    router.setRoutingParameter(avoid.RoutingParameter.fixedSharedPathPenalty, 700)

    const pins = new Map<string, { end: PinEnd; anchor: Point }>()
    for (const block of blocks) {
      const shape = router.addShape(block)
      block.ports.forEach((port, index) => {
        const anchor = portAnchor(block, port)
        const pin = index + 1
        shape.addPin({
          classId: pin, x: (anchor.x - block.x) / block.width, y: (anchor.y - block.y) / block.height,
          directions: directions[portSide(port)]
        }).setExclusive(false)
        pins.set(portKey(port), { end: { shape, pin }, anchor })
      })
    }

    const connections = connectors.flatMap(connector => {
      const source = pins.get(portKey(connector.source))
      const target = pins.get(portKey(connector.target))
      if (!source || !target) return []
      const { points } = connector
      const reverse = points.length > 0 &&
        distance(target.anchor, points[points.length - 1]) < distance(source.anchor, points[0])
      const link = reverse ? router.addConnector(target.end, source.end) : router.addConnector(source.end, target.end)
      link.setRoutingCheckpoints(reverse ? [...points].reverse() : points)
      return [{ connector, link, reverse }]
    })

    router.processTransaction()
    for (const { connector, link, reverse } of connections) {
      if (!link.hasValidRoute()) continue
      const route = link.displayRoute()
      if (reverse) route.reverse()
      if (route.length < 2 || !connector.points.every(point => passesThrough(route, point))) continue
      routes[connector.id] = route.map(({ x, y }) => ({ x, y }))
    }
    return routes
  } finally {
    router.delete()
  }
}
