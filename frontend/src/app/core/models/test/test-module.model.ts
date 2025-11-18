export interface TestModule {
  id: number;
  name: string;
  description?: string;
  createdById: number;
  createdByUsername?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateTestModuleRequest {
  name: string;
  description?: string;
}

