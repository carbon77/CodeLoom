import { Spinner, Notice, Button } from '../components/ui/Controls'
import styles from './CallbackPage.module.css'
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { handleSignInRedirect, signIn } from '../auth/keycloak';
export default function CallbackPage() {
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);
  useEffect(() => {
    let active = true;
    handleSignInRedirect()
      .then((returnTo) => {
        if (active) {
          navigate(returnTo || '/', { replace: true });
        }
      })
      .catch(() => {
        if (active) {
          setError('Sign-in failed. Please try again.');
        }
      });
    return () => {
      active = false;
    };
  }, [navigate]);
  return (<div className={styles.screen}>
    {!error && <Spinner />}
    {error && (<>
      <Notice tone="error">{error}</Notice>
      <Button appearance="primary" onClick={() => void signIn()}>
        Retry
      </Button>
    </>)}
  </div>);
}
