import { Spinner, Badge, Notice, Button } from '../components/ui/Controls'
import { Logout as LogoutIcon } from '../components/ui/Icons'
import ui from '../components/ui/ui.module.css'
import styles from './ProfilePage.module.css'
import PageHeading from '../components/ui/PageHeading'
import { useState } from 'react';
import { signOut } from '../auth/keycloak';
import { getRoles } from '../auth/roles';
import { useAuth } from '../auth/useAuth';
export default function ProfilePage() {
  const user = useAuth();
  const [error, setError] = useState<string | null>(null);
  const [loggingOut, setLoggingOut] = useState(false);
  if (!user) {
    return (<div className={styles.loading}>
      <Spinner />
    </div>);
  }
  const { profile } = user;
  const displayName = profile.name || profile.preferred_username || profile.email || 'User';
  const roles = getRoles(user);
  async function handleLogout(): Promise<void> {
    setLoggingOut(true);
    setError(null);
    try {
      await signOut();
    }
    catch {
      setLoggingOut(false);
      setError('Logout failed. Please try again.');
    }
  }
  return (<section className={styles.profile}>
    <PageHeading eyebrow="Your workspace" title="Profile" description="Your CodeLoom identity and account details." />
    <div className={ui.panel}>
      <div className={ui.padding}>
        <div className={[ui.stack, styles.identity].join(" ")}>
          <div className={[ui.avatar, styles.avatar].join(" ")}>
            {displayName.charAt(0).toUpperCase()}
          </div>
          <div>
            <h2>
              {displayName}
            </h2>
            {profile.preferred_username && (<p className={styles.username}>
              @{profile.preferred_username}
            </p>)}
          </div>
        </div>

        {profile.email && (<p className={styles.email}>
          <strong>Email:</strong> {profile.email}
        </p>)}

        {roles.length > 0 && (<div className={styles.permissions}>
          <p className={styles.rolesTitle}>
            <strong>Roles:</strong>
          </p>
          <div className={[ui.stack, styles.roles].join(" ")}>
            {roles.map((role) => (<Badge key={role} tone="primary">{role}</Badge>))}
          </div>
        </div>)}

        {error && <Notice tone="error">{error}</Notice>}
      </div>
    </div>

    <div className={styles.actions}>
      <Button appearance="primary" tone="error" icon={<LogoutIcon />} disabled={loggingOut} onClick={() => void handleLogout()}>
        {loggingOut ? 'Signing out…' : 'Log out'}
      </Button>
    </div>
  </section>);
}
