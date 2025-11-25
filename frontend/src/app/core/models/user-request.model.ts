export interface CreateUserRequest {
  username: string;
  fullName?: string;
  email: string;
  password: string;
  roles: string[];
  enabled: boolean;
}

export interface UpdateUserRequest {
  fullName?: string;
  email?: string;
  password?: string;
  roles: string[];
  enabled: boolean;
}

