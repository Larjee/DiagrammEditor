import { contextBridge, ipcRenderer } from 'electron'
import type { Block, Connector } from '../renderer/src/api/types'

contextBridge.exposeInMainWorld('editor', {
  routeConnectors: (blocks: Block[], connectors: Connector[]) =>
    ipcRenderer.invoke('diagram:route-connectors', blocks, connectors),
  backendUrl: (): Promise<string> => ipcRenderer.invoke('backend:url'),
  chooseFileToOpen: (): Promise<string | null> => ipcRenderer.invoke('dialog:open'),
  chooseFileToSave: (): Promise<string | null> => ipcRenderer.invoke('dialog:save'),
  confirm: (message: string): Promise<boolean> => ipcRenderer.invoke('dialog:confirm', message)
})
