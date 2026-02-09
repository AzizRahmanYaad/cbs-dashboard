export interface TrainingMaterial {
  id: number;
  programId: number;
  programTitle?: string;
  title: string;
  description?: string;
  materialType?: string; // PDF, Video, Document, Link, Presentation
  filePath?: string;
  fileSize?: number;
  fileName?: string;
  isRequired?: boolean;
  displayOrder?: number;
  uploadedById: number;
  uploadedByUsername?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateTrainingMaterialRequest {
  programId: number;
  title: string;
  description?: string;
  materialType?: string;
  filePath?: string;
  fileSize?: number;
  fileName?: string;
  isRequired?: boolean;
  displayOrder?: number;
}

export enum MaterialType {
  PDF = 'PDF',
  VIDEO = 'Video',
  DOCUMENT = 'Document',
  LINK = 'Link',
  PRESENTATION = 'Presentation'
}
