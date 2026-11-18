import { contextBridge, ipcRenderer } from 'electron'

contextBridge.exposeInMainWorld('editor', {
  backendUrl: (): Promise<string> => ipcRenderer.invoke('backend:url'),
  chooseFileToOpen: (): Promise<string | null> => ipcRenderer.invoke('dialog:open'),
  chooseFileToSave: (): Promise<string | null> => ipcRenderer.invoke('dialog:save'),
  confirm: (message: string): Promise<boolean> => ipcRenderer.invoke('dialog:confirm', message)
})
