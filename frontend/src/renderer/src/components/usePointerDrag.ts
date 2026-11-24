import { useEffect, useRef, type PointerEvent as ReactPointerEvent } from 'react'

export function usePointerDrag() {
  const drag = useRef<{ cancel: () => void; dispose: () => void } | null>(null)

  useEffect(() => {
    const escape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') drag.current?.cancel()
    }
    window.addEventListener('keydown', escape)
    return () => {
      window.removeEventListener('keydown', escape)
      drag.current?.dispose()
    }
  }, [])

  return function begin(
    event: ReactPointerEvent<HTMLElement>,
    preview: (event: PointerEvent) => void,
    commit: (event: PointerEvent, moved: boolean) => void,
    reset: () => void
  ) {
    if (event.button !== 0 || drag.current) return
    event.stopPropagation()

    const element = event.currentTarget
    const { pointerId, clientX, clientY } = event
    let moved = false
    let canceled = false
    const hasMoved = (next: PointerEvent) => Math.hypot(next.clientX - clientX, next.clientY - clientY) >= 3
    const clear = () => {
      drag.current = null
      window.removeEventListener('pointermove', move, true)
      window.removeEventListener('pointerup', finish, true)
      window.removeEventListener('pointercancel', interrupt, true)
      window.removeEventListener('blur', clear)
      if (element.hasPointerCapture(pointerId)) element.releasePointerCapture(pointerId)
      if (!canceled) reset()
    }
    const move = (next: PointerEvent) => {
      if (next.pointerId !== pointerId) return
      if (next.buttons === 0) {
        clear()
        return
      }
      if (canceled) return
      moved ||= hasMoved(next)
      if (moved) preview(next)
    }
    const finish = (next: PointerEvent) => {
      if (next.pointerId !== pointerId) return
      moved ||= hasMoved(next)
      clear()
      if (moved || canceled) suppressClick()
      if (!canceled) commit(next, moved)
    }
    const interrupt = (next: PointerEvent) => {
      if (next.pointerId === pointerId) clear()
    }
    drag.current = {
      cancel: () => {
        if (canceled) return
        canceled = true
        reset()
      },
      dispose: clear
    }
    element.setPointerCapture(pointerId)
    window.addEventListener('pointermove', move, true)
    window.addEventListener('pointerup', finish, true)
    window.addEventListener('pointercancel', interrupt, true)
    window.addEventListener('blur', clear)
  }
}

function suppressClick() {
  const cancel = (event: MouseEvent) => {
    event.preventDefault()
    event.stopPropagation()
  }
  window.addEventListener('click', cancel, { capture: true, once: true })
  window.setTimeout(() => window.removeEventListener('click', cancel, true), 0)
}
