export interface Role {
  name: string;
  description: string;
  module?: string;
}

export interface ModuleRole {
  moduleName: string;
  moduleDisplayName: string;
  roles: Role[];
}

