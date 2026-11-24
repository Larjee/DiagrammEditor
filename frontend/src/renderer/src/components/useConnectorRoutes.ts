import { useEffect, useState } from 'react'
import type { Block, Connector, Point } from '../api/types'

export function useConnectorRoutes(blocks: Block[], connectors: Connector[]) {
  const [result, setResult] = useState<{
    blocks: Block[]; connectors: Connector[]; routes: Record<string, Point[]>
  }>()

  useEffect(() => {
    if (!connectors.length) return
    let active = true
    window.editor.routeConnectors(blocks, connectors).then(routes => {
      if (active) setResult({ blocks, connectors, routes })
    }).catch(console.error)
    return () => { active = false }
  }, [blocks, connectors])

  return result?.blocks === blocks && result.connectors === connectors ? result.routes : undefined
}
