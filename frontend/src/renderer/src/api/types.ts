export type EditMode = 'STRICT' | 'FREE'

export type LineType = 'SOLID' | 'DASHED' | 'DOTTED'

export interface Style {
  borderColor: string
  borderWidth: number
  backgroundColor: string
  font: string
  fontSize: number
  lineType: LineType
}

export interface PortRef {
  blockId: string
  side: string
  offset: number
}

export interface Point {
  x: number
  y: number
}

export interface Block {
  id: string
  type: string
  x: number
  y: number
  width: number
  height: number
  ports: PortRef[]
  style: Style
}

export interface Connector {
  id: string
  type: string
  source: PortRef
  target: PortRef
  points: Point[]
  style: Style
}

export interface Diagram {
  name: string | null
  filePath: string | null
  componentSet: string
  schema: string | null
  mode: EditMode
  blockTypes: string[]
  connectorTypes: string[]
  blocks: Block[]
  connectors: Connector[]
}
