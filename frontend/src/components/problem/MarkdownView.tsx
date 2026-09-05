import ReactMarkdown from 'react-markdown'
import remarkGfm from 'remark-gfm'
import s from './MarkdownView.module.css'

export default function MarkdownView({ children }: { children: string }) {
  return <div className={s.markdown}><ReactMarkdown remarkPlugins={[remarkGfm]} components={{
    a: ({ href, children }) => <a href={href} target="_blank" rel="noreferrer">{children}</a>,
    table: ({ children }) => <div className={s.tableScroll}><table>{children}</table></div>,
  }}>{children}</ReactMarkdown></div>
}
