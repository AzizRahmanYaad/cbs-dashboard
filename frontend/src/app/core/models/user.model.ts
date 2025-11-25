export interface User {
  id: number;
  username: string;
  fullName?: string;
  email: string;
  roles: string[];
  enabled?: boolean;
  createdAt?: string;
}
