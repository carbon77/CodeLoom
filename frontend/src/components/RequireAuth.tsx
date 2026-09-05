import { Spinner, Notice, Button } from './ui/Controls'
import styles from './RequireAuth.module.css'
import { useEffect, useState } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { signIn } from '../auth/keycloak';
import { useAuth } from '../auth/useAuth';
export default function RequireAuth() {
  const user = useAuth();
  const location = useLocation();
  const [error, setError] = useState<string | null>(null);
  const [attempt, setAttempt] = useState(0);
  useEffect(() => {
    if (user !== null) {
      return;
    }
    let active = true;
    setError(null);
    void signIn()
      .catch(() => {
        if (active) {
          setError('Unable to reach the sign-in provider. Please try again.');
        }
      });
    return () => {
      active = false;
    };
  }, [user, location.pathname, attempt]);
  if (user) {
    return <Outlet />;
  }
  return (<div className={styles.screen}>
    {user === undefined && <Spinner />}
    {user === null && !error && (<p>Redirecting to sign in…</p>)}
    {user === null && error && (<>
      <Notice tone="error">{error}</Notice>
      <Button appearance="primary" onClick={() => setAttempt((n) => n + 1)}>
        Retry
      </Button>
    </>)}
  </div>);
}
