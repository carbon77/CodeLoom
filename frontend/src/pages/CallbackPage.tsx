import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Alert, Box, Button, CircularProgress } from '@mui/material'
import { handleSignInRedirect, signIn } from '../auth/keycloak'

export default function CallbackPage() {
  const navigate = useNavigate()
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let active = true
    handleSignInRedirect()
      .then((returnTo) => {
        if (active) {
          navigate(returnTo || '/', { replace: true })
        }
      })
      .catch(() => {
        if (active) {
          setError('Sign-in failed. Please try again.')
        }
      })

    return () => {
      active = false
    }
  }, [navigate])

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
      {!error && <CircularProgress />}
      {error && (
        <>
          <Alert severity="error">{error}</Alert>
          <Button variant="contained" onClick={() => void signIn()}>
            Retry
          </Button>
        </>
      )}
    </Box>
  )
}
