import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { Box, CircularProgress } from '@mui/material'
import { completeSignOut } from '../auth/keycloak'

export default function LogoutPage() {
  const navigate = useNavigate()

  useEffect(() => {
    let active = true
    void completeSignOut().finally(() => {
      if (active) {
        navigate('/', { replace: true })
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
        alignItems: 'center',
        justifyContent: 'center',
      }}
    >
      <CircularProgress />
    </Box>
  )
}
