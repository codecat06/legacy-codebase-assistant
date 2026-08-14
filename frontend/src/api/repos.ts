import { api } from './client';
import type { CreateRepositoryRequest, IngestionJobResponse } from '../types';

export async function createRepository(payload: CreateRepositoryRequest): Promise<IngestionJobResponse> {
  const { data } = await api.post<IngestionJobResponse>('/api/repos', payload);
  return data;
}

export async function getJobStatus(jobId: string): Promise<IngestionJobResponse> {
  const { data } = await api.get<IngestionJobResponse>(`/api/repos/jobs/${jobId}`);
  return data;
}