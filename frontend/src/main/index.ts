import { app, BrowserWindow, dialog, ipcMain, net, protocol } from 'electron'
import { spawn, type ChildProcess } from 'node:child_process'
import { createServer } from 'node:net'
import { join } from 'node:path'
import { pathToFileURL } from 'node:url'

const JSON_FILES = [{ name: 'Diagrams', extensions: ['json'] }]

let backend: ChildProcess | undefined
let backendUrl = process.env.BACKEND_URL ?? 'http://127.0.0.1:8080'

protocol.registerSchemesAsPrivileged([
  { scheme: 'app', privileges: { standard: true, secure: true, supportFetchAPI: true } }
])

app.whenReady().then(async () => {
  protocol.handle('app', (request) => {
    const path = decodeURIComponent(new URL(request.url).pathname)
    return net.fetch(pathToFileURL(join(__dirname, '../renderer', path)).toString())
  })
  if (app.isPackaged) {
    backendUrl = await startBackend()
  }
  registerIpc()
  createWindow()
})

app.on('window-all-closed', () => app.quit())
app.on('will-quit', () => backend?.kill())

function createWindow(): void {
  const window = new BrowserWindow({
    width: 1280,
    height: 800,
    title: 'Diagram Editor',
    webPreferences: { preload: join(__dirname, '../preload/index.js') }
  })
  window.loadURL(process.env.ELECTRON_RENDERER_URL ?? 'app://bundle/index.html')
}

function registerIpc(): void {
  ipcMain.handle('backend:url', () => backendUrl)
  ipcMain.handle('dialog:open', async () => {
    const result = await dialog.showOpenDialog({ filters: JSON_FILES, properties: ['openFile'] })
    return result.canceled ? null : result.filePaths[0]
  })
  ipcMain.handle('dialog:save', async () => {
    const result = await dialog.showSaveDialog({ filters: JSON_FILES })
    return result.canceled ? null : result.filePath
  })
  ipcMain.handle('dialog:confirm', async (_event, message: string) => {
    const result = await dialog.showMessageBox({
      type: 'question',
      buttons: ['OK', 'Cancel'],
      defaultId: 0,
      cancelId: 1,
      message
    })
    return result.response === 0
  })
}

async function startBackend(): Promise<string> {
  const port = await freePort()
  const java = join(process.resourcesPath, 'runtime', 'bin', process.platform === 'win32' ? 'java.exe' : 'java')
  const jar = join(process.resourcesPath, 'backend.jar')
  backend = spawn(java, ['-jar', jar, `--server.port=${port}`], { stdio: 'inherit' })
  const url = `http://127.0.0.1:${port}`
  await waitUntilReady(url)
  return url
}

function freePort(): Promise<number> {
  return new Promise((resolve, reject) => {
    const server = createServer()
    server.on('error', reject)
    server.listen(0, '127.0.0.1', () => {
      const address = server.address()
      server.close(() => resolve(typeof address === 'object' && address ? address.port : 0))
    })
  })
}

async function waitUntilReady(url: string): Promise<void> {
  for (let attempt = 0; attempt < 60; attempt++) {
    const ready = await fetch(`${url}/api/schemas`).then((response) => response.ok, () => false)
    if (ready) {
      return
    }
    await new Promise((resolve) => setTimeout(resolve, 500))
  }
  throw new Error(`Backend did not start at ${url}`)
}
