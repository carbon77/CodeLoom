// oxlint-disable react/only-export-components -- The icon factory creates the exported React components.
import type { SVGProps } from 'react'

function icon(path: string) {
  return function Icon(props: SVGProps<SVGSVGElement>) {
    return <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true" {...props}><path d={path} /></svg>
  }
}
export const Add = icon('M12 5v14M5 12h14')
export const Delete = icon('M3 6h18M9 6V3h6v3M5 6l1 15h12l1-15M10 10v7M14 10v7')
export const Edit = icon('m16 3 5 5-12 12-6 1 1-6L16 3ZM13 6l5 5')
export const Publish = icon('M12 16V3m-5 5 5-5 5 5M4 15v6h16v-6')
export const Unpublished = icon('M12 3v13m-5-5 5 5 5-5M4 17v4h16v-4')
export const Logout = icon('M9 4H4v16h5M9 12h12m-5-5 5 5-5 5')
export const Send = icon('m3 3 18 9-18 9 4-9-4-9ZM7 12h14')
export const ArrowBack = icon('M20 12H4m6-6-6 6 6 6')
export const Close = icon('m6 6 12 12M18 6 6 18')
export const Refresh = icon('M20 7v5h-5M4 17v-5h5M5 7a8 8 0 0 1 13-2l2 7M4 12l2 7a8 8 0 0 0 13-2')
export const Check = icon('m5 12 4 4L19 6')
export const CheckCircle = icon('M22 11a10 10 0 1 1-6-8M8 11l4 4L22 4')
export const Cancel = icon('M22 12a10 10 0 1 1-20 0 10 10 0 0 1 20 0ZM8 8l8 8M16 8l-8 8')
export const ContentCopy = icon('M9 9h12v12H9V9ZM5 15H3V3h12v2')
export const Settings = icon('M4 7h16M4 17h16M8 4v6M16 14v6')
export const Sun = icon('M16 12a4 4 0 1 1-8 0 4 4 0 0 1 8 0ZM12 2v2M12 20v2M2 12h2M20 12h2M5 5l1 1M18 18l1 1M5 19l1-1M18 6l1-1')
export const Moon = icon('M21 13A9 9 0 0 1 11 3a9 9 0 1 0 10 10Z')
export const Code = icon('m8 6-6 6 6 6M16 6l6 6-6 6M14 3l-4 18')
