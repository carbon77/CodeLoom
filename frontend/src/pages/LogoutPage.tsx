import { Spinner } from '../components/ui/Controls'
import styles from './LogoutPage.module.css'
import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { completeSignOut } from '../auth/keycloak';
export default function LogoutPage() {
  const navigate = useNavigate();
  useEffect(() => {
    let active = true;
    void completeSignOut().finally(() => {
      if (active) {
        navigate('/', { replace: true });
      }
    });
    return () => {
      active = false;
    };
  }, [navigate]);
  return (<div className={styles.screen}>
    <Spinner />
  </div>);
}
