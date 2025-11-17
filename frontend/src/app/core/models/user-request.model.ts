export interface CreateUserRequest {
  username: string;
  email: string;
  password: string;
  roles: string[];
  enabled: boolean;
}

export interface UpdateUserRequest {
  email?: string;
  password?: string;
  roles: string[];
  enabled: boolean;
}

