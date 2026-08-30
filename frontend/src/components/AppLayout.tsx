import { Link, Outlet, useRouteLoaderData } from 'react-router-dom'
import { AppBar, Box, Button, Container, FormControl, MenuItem, Select, Toolbar, Typography } from '@mui/material'
import { isAdmin } from '../auth/roles'
import { useAuth } from '../auth/useAuth'
import type { ProblemDetail } from '../api/problems'
import { useI18n, type Language } from '../i18n/I18nContext'

export default function AppLayout() {
  const user = useAuth()
  const problem = useRouteLoaderData('problem') as ProblemDetail | undefined
  const { language, setLanguage, t } = useI18n()

  return (
    <>
      <AppBar position="static">
        <Toolbar>
          <Typography
            variant="h6"
            component={Link}
            to="/"
            sx={{ flexGrow: 1, color: 'inherit', textDecoration: 'none' }}
          >
            CodeLoom
          </Typography>
          {problem && (
            <Typography
              variant="body2"
              sx={{
                color: 'inherit',
                maxWidth: '40vw',
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                whiteSpace: 'nowrap',
                mr: 2,
              }}
            >
              #{problem.id} · {problem.title}
            </Typography>
          )}
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Button component={Link} to="/problems" color="inherit">
              {t('nav.problems')}
            </Button>
            {user && isAdmin(user) && (
              <Button component={Link} to="/admin/problems" color="inherit">
                {t('nav.admin')}
              </Button>
            )}
            <Button component={Link} to="/" color="inherit">
              {t('nav.profile')}
            </Button>
            <FormControl size="small" variant="outlined">
              <Select
                value={language}
                aria-label={t('language.label')}
                onChange={(event) => setLanguage(event.target.value as Language)}
                sx={{ color: 'inherit', minWidth: 74, '& .MuiOutlinedInput-notchedOutline': { borderColor: 'currentColor' } }}
              >
                <MenuItem value="en">EN</MenuItem>
                <MenuItem value="ru">RU</MenuItem>
              </Select>
            </FormControl>
          </Box>
        </Toolbar>
      </AppBar>
      <Container maxWidth="xl" sx={{ py: 3 }}>
        <Outlet />
      </Container>
    </>
  )
}
