import { createBrowserRouter, Navigate, RouterProvider } from 'react-router-dom'
import { CssBaseline, ThemeProvider } from '@mui/material'
import theme from './theme'
import RequireAuth from './components/RequireAuth'
import CallbackPage from './pages/CallbackPage'
import LogoutPage from './pages/LogoutPage'
import ProfilePage from './pages/ProfilePage'

const router = createBrowserRouter([
  { path: '/callback', element: <CallbackPage /> },
  { path: '/logout', element: <LogoutPage /> },
  {
    path: '/',
    element: <RequireAuth />,
    children: [{ index: true, element: <ProfilePage /> }],
  },
  { path: '*', element: <Navigate to="/" replace /> },
])

export default function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <RouterProvider router={router} />
    </ThemeProvider>
  )
}
