import { useState } from 'react'
import {
  Alert,
  Avatar,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Container,
  Stack,
  Typography,
} from '@mui/material'
import LogoutIcon from '@mui/icons-material/Logout'
import { signOut } from '../auth/keycloak'
import { useAuth } from '../auth/useAuth'
import type { User } from 'oidc-client-ts'

interface RealmAccess {
  roles?: string[]
}

function getRoles(user: User): string[] {
  const realmAccess = user.profile.realm_access as unknown as RealmAccess | undefined
  return realmAccess?.roles ?? []
}

export default function ProfilePage() {
  const user = useAuth()
  const [error, setError] = useState<string | null>(null)
  const [loggingOut, setLoggingOut] = useState(false)

  if (!user) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center' }}>
        <CircularProgress />
      </Box>
    )
  }

  const { profile } = user
  const displayName = profile.name || profile.preferred_username || profile.email || 'User'
  const roles = getRoles(user)

  async function handleLogout(): Promise<void> {
    setLoggingOut(true)
    setError(null)
    try {
      await signOut()
    } catch {
      setLoggingOut(false)
      setError('Logout failed. Please try again.')
    }
  }

  return (
    <Container maxWidth="sm" sx={{ py: 4 }}>
      <Card variant="outlined">
        <CardContent>
          <Stack direction="row" spacing={3} sx={{ alignItems: 'center', mb: 3 }}>
            <Avatar sx={{ width: 64, height: 64, fontSize: 28 }}>
              {displayName.charAt(0).toUpperCase()}
            </Avatar>
            <Box>
              <Typography variant="h5" component="h1">
                {displayName}
              </Typography>
              {profile.preferred_username && (
                <Typography color="text.secondary">
                  @{profile.preferred_username}
                </Typography>
              )}
            </Box>
          </Stack>

          {profile.email && (
            <Typography variant="body1" sx={{ mb: 2 }}>
              <strong>Email:</strong> {profile.email}
            </Typography>
          )}

          {roles.length > 0 && (
            <Box sx={{ mb: 2 }}>
              <Typography variant="body1" sx={{ mb: 1 }}>
                <strong>Roles:</strong>
              </Typography>
              <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap', gap: 1 }}>
                {roles.map((role) => (
                  <Chip key={role} label={role} color="primary" variant="outlined" />
                ))}
              </Stack>
            </Box>
          )}

          {error && <Alert severity="error">{error}</Alert>}
        </CardContent>
      </Card>

      <Box sx={{ display: 'flex', justifyContent: 'flex-end', mt: 2 }}>
        <Button
          variant="contained"
          color="error"
          startIcon={<LogoutIcon />}
          disabled={loggingOut}
          onClick={() => void handleLogout()}
        >
          {loggingOut ? 'Signing out…' : 'Log out'}
        </Button>
      </Box>
    </Container>
  )
}
