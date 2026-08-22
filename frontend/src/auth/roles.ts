import type { User } from "oidc-client-ts";

type UserRoles = string[];

export function getRoles(user: User): UserRoles {
  const realmAccess = user.profile.realm_access as { roles?: UserRoles } | undefined;
  const roles = realmAccess?.roles ?? (user.profile.roles as UserRoles | undefined) ?? [];
  return roles.filter((role) => role.startsWith("ROLE_"));
}

export function isAdmin(user: User): boolean {
  return getRoles(user).includes("ROLE_ADMIN");
}
