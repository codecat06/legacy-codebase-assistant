import { api } from './client';
import type { ImpactAnalysisResponse } from '../types';

export async function getImpact(fqn: string, direction: string, depth: number): Promise<ImpactAnalysisResponse> {
  const { data } = await api.get<ImpactAnalysisResponse>('/api/impact', {
    params: { fqn, direction, depth },
  });
  return data;
}