export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  /** Set by /auth/google: true when the sign-in created a new account. */
  isNewUser?: boolean;
}

export type { UserProfile as UserResponse } from './user.model';
