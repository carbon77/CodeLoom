import { createBrowserRouter, Navigate, RouterProvider } from 'react-router-dom'
import { CssBaseline, ThemeProvider } from '@mui/material'
import theme from './theme'
import RequireAuth from './components/RequireAuth'
import RequireAdmin from './components/RequireAdmin'
import AppLayout from './components/AppLayout'
import CallbackPage from './pages/CallbackPage'
import LogoutPage from './pages/LogoutPage'
import ProfilePage from './pages/ProfilePage'
import ProblemListPage from './pages/ProblemListPage'
import AdminProblemListPage from './pages/admin/AdminProblemListPage'
import ProblemFormPage from './pages/admin/ProblemFormPage'

const router = createBrowserRouter([
  { path: '/callback', element: <CallbackPage /> },
  { path: '/logout', element: <LogoutPage /> },
  {
    path: '/',
    element: <RequireAuth />,
    children: [
      {
        element: <AppLayout />,
        children: [
          { index: true, element: <ProfilePage /> },
          { path: 'problems', element: <ProblemListPage /> },
          {
            path: 'admin',
            element: <RequireAdmin />,
            children: [
              { index: true, element: <Navigate to="/admin/problems" replace /> },
              { path: 'problems', element: <AdminProblemListPage /> },
              { path: 'problems/new', element: <ProblemFormPage /> },
              { path: 'problems/:problemId/edit', element: <ProblemFormPage /> },
            ],
          },
        ],
      },
    ],
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
