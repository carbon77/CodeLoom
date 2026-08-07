import { lazy, Suspense } from 'react'
import {
  createBrowserRouter,
  Navigate,
  RouterProvider,
  useRouteError,
  type LoaderFunctionArgs,
} from 'react-router-dom'
import { Alert, Box, CircularProgress, CssBaseline, ThemeProvider } from '@mui/material'
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
import { fetchProblemBySlug, type ProblemDetail } from './api/problems'

const ProblemDetailPage = lazy(() => import('./pages/ProblemDetailPage'))

async function problemLoader({ params }: LoaderFunctionArgs): Promise<ProblemDetail> {
  if (!params.problemSlug) {
    throw new Error('Problem slug is missing')
  }
  return fetchProblemBySlug(params.problemSlug)
}

function ProblemDetailError() {
  const error = useRouteError()
  return (
    <Alert severity="error">
      {error instanceof Error ? error.message : 'Unable to load problem.'}
    </Alert>
  )
}

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
            path: 'problems/:problemSlug',
            id: 'problem',
            loader: problemLoader,
            errorElement: <ProblemDetailError />,
            element: (
              <Suspense
                fallback={
                  <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
                    <CircularProgress />
                  </Box>
                }
              >
                <ProblemDetailPage />
              </Suspense>
            ),
          },
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
