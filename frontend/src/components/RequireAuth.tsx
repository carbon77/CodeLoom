import { useEffect, useState } from 'react'
import { Outlet, useLocation } from 'react-router-dom'
import { Alert, Box, Button, CircularProgress, Typography } from '@mui/material'
import { signIn } from '../auth/keycloak'
import { useAuth } from '../auth/useAuth'

export default function RequireAuth() {
  const user = useAuth()
  const location = useLocation()
  const [error, setError] = useState<string | null>(null)
  const [attempt, setAttempt] = useState(0)

  useEffect(() => {
    if (user !== null) {
      return
    }

    let active = true
    setError(null)
    void signIn()
      .catch(() => {
        if (active) {
          setError('Unable to reach the sign-in provider. Please try again.')
        }
      })

    return () => {
      active = false
    }
  }, [user, location.pathname, attempt])

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 2,
      }}
    >
      {user === undefined && <CircularProgress />}
      {user === null && !error && (
        <Typography>Redirecting to sign in…</Typography>
      )}
      {user === null && error && (
        <>
          <Alert severity="error">{error}</Alert>
          <Button variant="contained" onClick={() => setAttempt((n) => n + 1)}>
            Retry
          </Button>
        </>
      )}
      {user && <Outlet />}
    </Box>
  )
}
