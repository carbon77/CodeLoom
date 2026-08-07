import { Navigate, Outlet } from 'react-router-dom'
import { Box, CircularProgress } from '@mui/material'
import { isAdmin } from '../auth/roles'
import { useAuth } from '../auth/useAuth'

export default function RequireAdmin() {
  const user = useAuth()

  if (user === undefined) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
        <CircularProgress />
      </Box>
    )
  }

  if (user === null || !isAdmin(user)) {
    return <Navigate to="/" replace />
  }

  return <Outlet />
}
