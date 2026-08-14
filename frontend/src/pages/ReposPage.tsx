import { useState, type FormEvent } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { createRepository, getJobStatus } from '../api/repos';
import type { IngestionStatus } from '../types';

const STAGES: IngestionStatus[] = ['PENDING', 'CLONING', 'PARSING', 'GRAPH_BUILDING', 'EMBEDDING', 'DONE'];

function StatusTracker({ status }: { status: IngestionStatus }) {
  if (status === 'FAILED') {
    return <div className="status-pill status-failed">FAILED</div>;
  }

  const currentIndex = STAGES.indexOf(status);

  return (
    <div className="status-tracker">
      {STAGES.map((stage, i) => (
        <div key={stage} className={`status-step ${i <= currentIndex ? 'done' : ''} ${i === currentIndex ? 'active' : ''}`}>
          <span className="status-dot" />
          <span className="status-label">{stage}</span>
        </div>
      ))}
    </div>
  );
}

export default function ReposPage() {
  const [name, setName] = useState('');
  const [remoteUrl, setRemoteUrl] = useState('');
  const [jobId, setJobId] = useState<string | null>(null);

  const createMutation = useMutation({
    mutationFn: createRepository,
    onSuccess: (data) => setJobId(data.jobId),
  });

  const jobQuery = useQuery({
    queryKey: ['job', jobId],
    queryFn: () => getJobStatus(jobId as string),
    enabled: jobId !== null,
    refetchInterval: (query) => {
      const status = query.state.data?.status;
      return status === 'DONE' || status === 'FAILED' ? false : 1500;
    },
  });

  function handleSubmit(e: FormEvent) {
    e.preventDefault();
    if (!name.trim() || !remoteUrl.trim()) return;
    createMutation.mutate({ name, remoteUrl });
  }

  return (
    <div className="page">
      <h1>Repo Bağla</h1>
      <p className="page-subtitle">
        Analiz edilecek bir GitHub reposunun URL'ini gir. Repo klonlanacak, parse edilecek, kod grafiği
        kurulacak ve embedding'ler oluşturulacak.
      </p>

      <form onSubmit={handleSubmit} className="card form">
        <label>
          Repo adı
          <input
            placeholder="spring-petclinic"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
          />
        </label>
        <label>
          GitHub URL
          <input
            placeholder="https://github.com/spring-projects/spring-petclinic.git"
            value={remoteUrl}
            onChange={(e) => setRemoteUrl(e.target.value)}
            required
          />
        </label>
        <button type="submit" disabled={createMutation.isPending}>
          {createMutation.isPending ? 'Başlatılıyor...' : 'Ingest Et'}
        </button>
        {createMutation.isError && (
          <p className="error-text">İstek başarısız oldu. Backend çalışıyor mu kontrol et.</p>
        )}
      </form>

      {jobId && jobQuery.data && (
        <div className="card">
          <h2>İlerleme</h2>
          <StatusTracker status={jobQuery.data.status} />
          {jobQuery.data.status === 'DONE' && (
            <p className="success-text">Ingestion tamamlandı — artık Sohbet ve Impact sekmelerini kullanabilirsin.</p>
          )}
          {jobQuery.data.errorMessage && <p className="error-text">{jobQuery.data.errorMessage}</p>}
          <p className="job-id">Job ID: {jobId}</p>
        </div>
      )}
    </div>
  );
}