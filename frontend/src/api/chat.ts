import { api } from './client';
import type { AskRequest, AskResponse } from '../types';

export async function askQuestion(payload: AskRequest): Promise<AskResponse> {
  const { data } = await api.post<AskResponse>('/api/ask', payload);
  return data;
}