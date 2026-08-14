export type IngestionStatus =
  | 'PENDING'
  | 'CLONING'
  | 'PARSING'
  | 'GRAPH_BUILDING'
  | 'EMBEDDING'
  | 'DONE'
  | 'FAILED';

export interface CreateRepositoryRequest {
  name: string;
  remoteUrl: string;
}

export interface IngestionJobResponse {
  jobId: string;
  repositoryId: string;
  status: IngestionStatus;
  errorMessage: string | null;
}

export interface RetrievedNode {
  fqn: string;
  filePath: string;
  sourceText: string | null;
}

export interface AskRequest {
  question: string;
  topK: number;
  hops: number;
}

export interface AskResponse {
  answer: string;
  sources: string[];
}

export type ImpactDirection = 'UPSTREAM' | 'DOWNSTREAM' | 'BOTH';

export interface ImpactAnalysisResponse {
  rootFqn: string;
  direction: string;
  depth: number;
  affectedNodes: string[];
}