export type UserRole = 'USER' | 'ADMIN';

export interface UserProfile {
  id: string;
  name: string;
  email: string;
  role: UserRole;
  createdAt: string;
  avatarUrl?: string;
  hasPassword: boolean;
  linkedWithGoogle: boolean;
}

export interface UpdateUserRequest {
  name: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}
