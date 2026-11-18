/// <reference types="vite/client" />

interface EditorBridge {
  backendUrl(): Promise<string>
  chooseFileToOpen(): Promise<string | null>
  chooseFileToSave(): Promise<string | null>
  confirm(message: string): Promise<boolean>
}

interface Window {
  editor: EditorBridge
}
