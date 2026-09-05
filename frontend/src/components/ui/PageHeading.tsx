import type { ReactNode } from 'react'
import s from './PageHeading.module.css'

export default function PageHeading({ eyebrow, title, description, children }: { eyebrow: string; title: string; description: string; children?: ReactNode }) {
  return <header className={s.heading}><div><p className={s.eyebrow}>{eyebrow}</p><h1>{title}</h1><p className={s.description}>{description}</p></div>{children && <div className={s.actions}>{children}</div>}</header>
}
