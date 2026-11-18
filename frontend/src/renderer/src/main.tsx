import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import '@xyflow/react/dist/style.css'
import './styles.css'
import { MainWindow } from './components/MainWindow'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <MainWindow />
  </StrictMode>
)
