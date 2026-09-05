import { Spinner } from './ui/Controls'
import styles from './RequireAdmin.module.css'
import { Navigate, Outlet } from 'react-router-dom';
import { isAdmin } from '../auth/roles';
import { useAuth } from '../auth/useAuth';
export default function RequireAdmin() {
  const user = useAuth();
  if (user === undefined) {
    return (<div className={styles.loading}>
      <Spinner />
    </div>);
  }
  if (user === null || !isAdmin(user)) {
    return <Navigate to="/" replace />;
  }
  return <Outlet />;
}
