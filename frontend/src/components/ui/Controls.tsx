// oxlint-disable react/only-export-components -- The dismissal hook is shared by these controls and editor settings.
import { useEffect, useId, useRef, useState, type ButtonHTMLAttributes, type InputHTMLAttributes, type ReactNode, type SelectHTMLAttributes, type ChangeEventHandler } from 'react'
import { createPortal } from 'react-dom'
import { Link, type LinkProps } from 'react-router-dom'
import type { Topic } from '../../api/problems'
import s from './ui.module.css'

type Appearance = 'primary' | 'secondary' | 'ghost'
type Tone = 'primary' | 'success' | 'error' | 'warning' | 'info' | 'default' | 'inherit'
type ButtonStyle = { appearance?: Appearance; tone?: Tone; icon?: ReactNode }
export function Button({ appearance = 'ghost', tone, icon, className = '', children, ...props }: ButtonHTMLAttributes<HTMLButtonElement> & ButtonStyle) {
  return <button type="button" {...props} className={`${s.button} ${className}`} data-appearance={appearance} data-tone={tone}>{icon}{children}</button>
}
export function LinkButton({ appearance = 'ghost', tone, icon, className = '', children, ...props }: LinkProps & ButtonStyle) {
  return <Link {...props} className={`${s.button} ${className}`} data-appearance={appearance} data-tone={tone}>{icon}{children}</Link>
}
export function IconButton(props: ButtonHTMLAttributes<HTMLButtonElement> & ButtonStyle) {
  return <Button {...props} className={`${s.iconButton} ${props.className ?? ''}`} title={props.title ?? props['aria-label']} />
}
export function Badge({ children, tone = 'default', className = '' }: { children: ReactNode; tone?: Tone; className?: string }) {
  return <span className={`${s.badge} ${className}`} data-tone={tone}>{children}</span>
}
export function Notice({ children, tone = 'info', className = '' }: { children: ReactNode; tone?: Tone; className?: string }) {
  return <div role={tone === 'error' ? 'alert' : 'status'} className={`${s.notice} ${className}`} data-tone={tone}>{children}</div>
}
export function Spinner() { return <span role="progressbar" aria-label="Loading" className={s.spinner} /> }

type FieldProps = Omit<InputHTMLAttributes<HTMLInputElement>, 'onChange'> & {
  label?: string; help?: string; multiline?: boolean; rows?: number;
  onChange?: ChangeEventHandler<HTMLInputElement | HTMLTextAreaElement>
}
export function Field({ label, help, multiline, rows = 4, className = '', id, ...props }: FieldProps) {
  const generatedId = useId()
  const fieldId = id ?? generatedId
  return <div className={`${s.field} ${className}`}>
    {label && <label className={s.label} htmlFor={fieldId}>{label}{props.required && ' *'}</label>}
    {multiline
      ? <textarea {...props as React.TextareaHTMLAttributes<HTMLTextAreaElement>} rows={rows} id={fieldId} aria-describedby={help ? `${fieldId}-help` : undefined} className={s.input} />
      : <input {...props} id={fieldId} aria-describedby={help ? `${fieldId}-help` : undefined} className={s.input} />}
    {help && <span id={`${fieldId}-help`} className={s.help}>{help}</span>}
  </div>
}
export function SelectField({ label, className = '', id, children, ...props }: SelectHTMLAttributes<HTMLSelectElement> & { label: string }) {
  const generatedId = useId()
  return <label className={`${s.field} ${className}`} htmlFor={id ?? generatedId}><span className={s.label}>{label}</span><select {...props} id={id ?? generatedId} className={s.input}>{children}</select></label>
}

// Shared dismissal behavior for non-modal popovers. Tab remains native.
export function useDismiss(open: boolean, close: () => void) {
  const root = useRef<HTMLDivElement>(null)
  const closeRef = useRef(close)
  closeRef.current = close
  useEffect(() => {
    if (!open) return
    const pointer = (event: PointerEvent) => { if (!root.current?.contains(event.target as Node)) closeRef.current() }
    const keyboard = (event: KeyboardEvent) => {
      if (event.key === 'Escape') { event.preventDefault(); closeRef.current(); root.current?.querySelector<HTMLButtonElement>('button')?.focus() }
    }
    document.addEventListener('pointerdown', pointer)
    document.addEventListener('keydown', keyboard)
    return () => { document.removeEventListener('pointerdown', pointer); document.removeEventListener('keydown', keyboard) }
  }, [open])
  return root
}

export function MultiSelect({ label, options, value, onChange }: { label: string; options: { value: string; label: string }[]; value: string[]; onChange: (value: string[]) => void }) {
  const [open, setOpen] = useState(false)
  const root = useDismiss(open, () => setOpen(false))
  const id = useId()
  return <div className={s.popoverRoot} ref={root} onBlur={(event) => { if (!event.currentTarget.contains(event.relatedTarget)) setOpen(false) }}>
    <Button appearance="secondary" aria-expanded={open} aria-controls={open ? id : undefined} onClick={() => setOpen(!open)}>{label}{value.length > 0 && <Badge>{value.length}</Badge>}<span aria-hidden="true">⌄</span></Button>
    {open && <div id={id} className={s.popover} role="group" aria-label={label}>
      {options.length === 0 && <p className={s.help}>No options available.</p>}
      {options.map((option) => <label className={s.option} key={option.value}><input type="checkbox" checked={value.includes(option.value)} onChange={(event) => onChange(event.target.checked ? [...value, option.value] : value.filter((item) => item !== option.value))} />{option.label}</label>)}
    </div>}
  </div>
}

export function TopicPicker({ options, value, onChange }: { options: Topic[]; value: (Topic | string)[]; onChange: (value: (Topic | string)[]) => void }) {
  const [query, setQuery] = useState('')
  const listId = useId()
  const name = (topic: Topic | string) => typeof topic === 'string' ? topic : topic.name
  const add = () => {
    const text = query.trim()
    if (!text) return
    if (!value.some((topic) => name(topic).toLowerCase() === text.toLowerCase())) onChange([...value, options.find((topic) => topic.name.toLowerCase() === text.toLowerCase()) ?? text])
    setQuery('')
  }
  return <div>
    <Field label="Topics" value={query} list={listId} onChange={(event) => setQuery(event.target.value)} onKeyDown={(event) => { if (event.key === 'Enter' && !event.nativeEvent.isComposing) { event.preventDefault(); add() } }} help="Select an existing topic or type a new one, then press Enter or Add topic." />
    <datalist id={listId}>{options.filter((topic) => !value.some((item) => name(item) === topic.name)).map((topic) => <option key={topic.id} value={topic.name} />)}</datalist>
    <div className={s.chips}>{value.map((topic, index) => <button type="button" key={name(topic)} className={s.chipRemove} aria-label={`Remove topic ${name(topic)}`} onClick={() => onChange(value.filter((_, i) => index !== i))}>{name(topic)} <span aria-hidden="true">×</span></button>)}<Button onClick={add} disabled={!query.trim()}>Add topic</Button></div>
  </div>
}

export function Modal({ open, onClose, title, children, busy = false }: { open: boolean; onClose: () => void; title: string; children: ReactNode; busy?: boolean }) {
  const panel = useRef<HTMLDivElement>(null)
  const titleId = useId()
  useEffect(() => {
    if (!open) return
    const previous = document.activeElement as HTMLElement | null
    const overflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    const focusable = () => [...panel.current!.querySelectorAll<HTMLElement>('button:not(:disabled), a[href], input:not(:disabled), [tabindex="0"]')]
    const initialFocus = focusable()[0] ?? panel.current
    initialFocus?.focus()
    const key = (event: KeyboardEvent) => {
      if (event.key !== 'Tab') return
      const items = focusable()
      const first = items[0] ?? panel.current!
      const last = items.at(-1) ?? panel.current!
      const outside = !items.includes(document.activeElement as HTMLElement)
      if (event.shiftKey && (document.activeElement === first || outside)) { event.preventDefault(); last.focus() }
      else if (!event.shiftKey && (document.activeElement === last || outside)) { event.preventDefault(); first.focus() }
    }
    document.addEventListener('keydown', key)
    return () => { document.removeEventListener('keydown', key); document.body.style.overflow = overflow; previous?.focus() }
  }, [open])
  if (!open) return null
  return createPortal(<div className={s.modalBackdrop} onClick={(event) => { if (event.target === event.currentTarget && !busy) onClose() }} onKeyDown={(event) => { if (event.key === 'Escape' && !busy) { event.stopPropagation(); onClose() } }}>
    <div ref={panel} className={s.modal} role="dialog" aria-modal="true" aria-labelledby={titleId} aria-busy={busy} tabIndex={-1}><h2 id={titleId} className={s.modalTitle}>{title}</h2>{children}</div>
  </div>, document.body)
}
