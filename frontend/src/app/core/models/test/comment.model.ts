export interface Comment {
  id: number;
  content: string;
  createdById: number;
  createdByUsername?: string;
  testCaseId?: number;
  defectId?: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateCommentRequest {
  content: string;
  testCaseId?: number;
  defectId?: number;
}

