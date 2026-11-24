/// <reference types="vite/client" />

interface EditorBridge {
  routeConnectors(
    blocks: import('./api/types').Block[],
    connectors: import('./api/types').Connector[]
  ): Promise<Record<string, import('./api/types').Point[]>>
  backendUrl(): Promise<string>
  chooseFileToOpen(): Promise<string | null>
  chooseFileToSave(): Promise<string | null>
  confirm(message: string): Promise<boolean>
}

interface Window {
  editor: EditorBridge
}
