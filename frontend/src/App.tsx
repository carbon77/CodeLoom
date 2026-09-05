import { Notice, Spinner } from './components/ui/Controls'
import styles from './App.module.css'
import { lazy, Suspense } from 'react';
import { createBrowserRouter, Navigate, RouterProvider, useRouteError, type LoaderFunctionArgs, } from 'react-router-dom';
import { AppThemeProvider } from './theme/AppTheme';
import RequireAuth from './components/RequireAuth';
import RequireAdmin from './components/RequireAdmin';
import AppLayout from './components/AppLayout';
import CallbackPage from './pages/CallbackPage';
import LogoutPage from './pages/LogoutPage';
import ProfilePage from './pages/ProfilePage';
import ProblemListPage from './pages/ProblemListPage';
import LanguagesPage from './pages/LanguagesPage';
import AdminProblemListPage from './pages/admin/AdminProblemListPage';
import ProblemFormPage from './pages/admin/ProblemFormPage';
import { fetchProblemBySlug, type ProblemDetail } from './api/problems';
import { EditorSettingsProvider } from './editor/EditorSettingsContext';
const ProblemDetailPage = lazy(() => import('./pages/ProblemDetailPage'));
async function problemLoader({ params }: LoaderFunctionArgs): Promise<ProblemDetail> {
  if (!params.problemSlug) {
    throw new Error('Problem slug is missing');
  }
  return fetchProblemBySlug(params.problemSlug);
}
function ProblemDetailError() {
  const error = useRouteError();
  return (<Notice tone="error">
    {error instanceof Error ? error.message : 'Unable to load problem.'}
  </Notice>);
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
          { path: 'languages', element: <LanguagesPage /> },
          { path: 'problems', element: <ProblemListPage /> },
          {
            path: 'problems/:problemSlug',
            id: 'problem',
            loader: problemLoader,
            errorElement: <ProblemDetailError />,
            element: (<Suspense fallback={<div className={styles.loading}>
              <Spinner />
            </div>}>
              <ProblemDetailPage />
            </Suspense>),
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
]);
export default function App() {
  return (<EditorSettingsProvider>
    <AppThemeProvider>
      <RouterProvider router={router} />
    </AppThemeProvider>
  </EditorSettingsProvider>);
}
