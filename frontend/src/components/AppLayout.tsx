import { NavLink, Link, Outlet, useRouteLoaderData } from 'react-router-dom'
import { isAdmin } from '../auth/roles'
import { useAuth } from '../auth/useAuth'
import { useAppTheme } from '../theme/AppTheme'
import type { ProblemDetail } from '../api/problems'
import { IconButton } from './ui/Controls'
import { Code, Moon, Sun } from './ui/Icons'
import s from './AppLayout.module.css'

export default function AppLayout() {
  const user = useAuth()
  const problem = useRouteLoaderData('problem') as ProblemDetail | undefined
  const { theme, setTheme } = useAppTheme()
  return <>
    <a className={s.skip} href="#main-content">Skip to content</a>
    <header className={s.header}>
      <Link className={s.brand} to="/"><span className={s.mark}><Code /></span>CodeLoom<span className={s.brandLabel}>WORKSPACE</span></Link>
      <nav className={s.nav} aria-label="Main navigation">
        <NavLink to="/problems">Problems</NavLink>
        <NavLink to="/languages">Languages</NavLink>
        {user && isAdmin(user) && <NavLink to="/admin/problems">Admin</NavLink>}
        <NavLink to="/" end>Profile</NavLink>
      </nav>
      <div className={s.tools}>
        <IconButton aria-label={`Switch to ${theme === 'dark' ? 'light' : 'dark'} theme`} onClick={() => setTheme(theme === 'dark' ? 'light' : 'dark')}>{theme === 'dark' ? <Sun /> : <Moon />}</IconButton>
        <Link to="/" className={s.avatar} aria-label="Your profile">{(user?.profile.name ?? user?.profile.preferred_username ?? 'U').charAt(0).toUpperCase()}</Link>
      </div>
    </header>
    <main id="main-content" tabIndex={-1} className={problem ? s.workspace : s.main}>
      {problem && <div className={s.breadcrumb}><Link to="/problems">Problems</Link><span>/</span><span>#{problem.id} · {problem.title}</span></div>}
      <Outlet />
    </main>
  </>
}
