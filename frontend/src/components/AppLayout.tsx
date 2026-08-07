import { Link, Outlet } from 'react-router-dom'
import { AppBar, Box, Button, Container, Toolbar, Typography } from '@mui/material'
import { isAdmin } from '../auth/roles'
import { useAuth } from '../auth/useAuth'

export default function AppLayout() {
  const user = useAuth()

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
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Button component={Link} to="/problems" color="inherit">
              Problems
            </Button>
            {user && isAdmin(user) && (
              <Button component={Link} to="/admin/problems" color="inherit">
                Admin
              </Button>
            )}
            <Button component={Link} to="/" color="inherit">
              Profile
            </Button>
          </Box>
        </Toolbar>
      </AppBar>
      <Container maxWidth="lg" sx={{ py: 3 }}>
        <Outlet />
      </Container>
    </>
  )
}
