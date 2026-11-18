import type { Block, Connector, Diagram, Point, PortRef, Style } from './types'

export class ApiError extends Error {
  constructor(
    readonly status: number,
    message: string
  ) {
    super(message)
  }
}

let backendUrl: Promise<string> | undefined

async function request<T>(method: string, path: string, body?: unknown): Promise<T> {
  backendUrl ??= window.editor.backendUrl()
  const response = await fetch(`${await backendUrl}/api${path}`, {
    method,
    headers: body === undefined ? undefined : { 'Content-Type': 'application/json' },
    body: body === undefined ? undefined : JSON.stringify(body)
  })
  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: response.statusText }))
    throw new ApiError(response.status, error.message)
  }
  return response.status === 204 ? (undefined as T) : response.json()
}

const component = (id: string) => `/diagram/components/${encodeURIComponent(id)}`
const connector = (id: string) => `/diagram/connectors/${encodeURIComponent(id)}`

export const api = {
  componentSets: () => request<string[]>('GET', '/component-sets'),
  schemas: () => request<string[]>('GET', '/schemas'),
  diagram: () => request<Diagram>('GET', '/diagram'),
  createDiagram: (componentSet: string, schema: string | null) =>
    request<Diagram>('POST', '/diagram', { componentSet, schema }),
  addBlock: (blockType: string, x: number, y: number) =>
    request<Block>('POST', '/diagram/blocks', { blockType, x, y }),
  addConnector: (connectorType: string, source: PortRef, target: PortRef) =>
    request<Connector>('POST', '/diagram/connectors', { connectorType, source, target }),
  resize: (id: string, width: number, height: number) =>
    request<Block>('PUT', `${component(id)}/size`, { width, height }),
  move: (id: string, x: number, y: number) => request<Block>('PUT', `${component(id)}/position`, { x, y }),
  changeStyle: (id: string, style: Style) => request<Style>('PUT', `${component(id)}/style`, style),
  addInflectionPoint: (connectorId: string, point: Point) =>
    request<Connector>('POST', `${connector(connectorId)}/points`, point),
  moveInflectionPoint: (connectorId: string, index: number, point: Point) =>
    request<Connector>('PUT', `${connector(connectorId)}/points/${index}`, point),
  selectComponentSet: (componentSet: string) =>
    request<Diagram>('PUT', '/diagram/component-set', { componentSet }),
  selectSchema: (schema: string | null) => request<Diagram>('PUT', '/diagram/schema', { schema }),
  openDiagram: (path: string) => request<Diagram>('POST', '/diagram/open', { path }),
  saveDiagram: (path: string | null, overwrite = false) =>
    request<Diagram>('POST', '/diagram/save', { path, overwrite }),
  deleteDiagram: () => request<void>('DELETE', '/diagram')
}
