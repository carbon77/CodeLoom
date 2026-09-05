import { useId, useState } from 'react'
import { editorThemes, useEditorSettings } from '../../editor/EditorSettingsContext'
import { IconButton, useDismiss } from '../ui/Controls'
import { Check, Settings } from '../ui/Icons'
import s from '../ui/ui.module.css'

export default function EditorSettings() {
  const { theme, setTheme } = useEditorSettings()
  const [open, setOpen] = useState(false)
  const root = useDismiss(open, () => setOpen(false))
  const id = useId()
  const close = () => { setOpen(false); root.current?.querySelector('button')?.focus() }
  return <div className={s.popoverRoot} ref={root} onBlur={(event) => { if (!event.currentTarget.contains(event.relatedTarget)) setOpen(false) }}>
    <IconButton aria-label="Editor settings" aria-haspopup="menu" aria-expanded={open} aria-controls={open ? id : undefined} onClick={() => setOpen(!open)}><Settings /></IconButton>
    {open && <div className={`${s.popover} ${s.popoverRight}`}>
      <p className={s.label}>Editor color scheme</p>
      <div id={id} role="menu" aria-label="Editor color scheme" onKeyDown={(event) => {
        const buttons = [...event.currentTarget.querySelectorAll<HTMLButtonElement>('button')]
        const current = buttons.indexOf(document.activeElement as HTMLButtonElement)
        const index = event.key === 'ArrowDown' ? (current + 1) % buttons.length : event.key === 'ArrowUp' ? (current + buttons.length - 1) % buttons.length : event.key === 'Home' ? 0 : event.key === 'End' ? buttons.length - 1 : -1
        if (index >= 0) { event.preventDefault(); buttons[index].focus() }
      }}>
        {editorThemes.map((option) => <button key={option.value} type="button" role="menuitemradio" aria-checked={theme === option.value} autoFocus={theme === option.value} className={s.option} onClick={() => { setTheme(option.value); close() }}>{theme === option.value && <Check />}{option.label}</button>)}
      </div>
    </div>}
  </div>
}
