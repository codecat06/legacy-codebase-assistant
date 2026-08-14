import { useState, type FormEvent } from 'react';
import { useMutation } from '@tanstack/react-query';
import { getImpact } from '../api/impact';

export default function ImpactPage() {
  const [fqn, setFqn] = useState('');
  const [direction, setDirection] = useState<'UPSTREAM' | 'DOWNSTREAM'>('UPSTREAM');
  const [depth, setDepth] = useState(3);

  const impactMutation = useMutation({
    mutationFn: () => getImpact(fqn, direction, depth),
  });

  function handleSubmit(e: FormEvent) {
    e.preventDefault();
    if (!fqn.trim()) return;
    impactMutation.mutate();
  }

  return (
    <div className="page">
      <h1>Impact Analysis</h1>
      <p className="page-subtitle">
        LLM'e sormadan, doğrudan kod grafiği üzerinden deterministik bir cevap. <strong>Upstream</strong>{' '}
        = bunu değiştirirsen kim bozulur. <strong>Downstream</strong> = bu neye bağımlı.
      </p>

      <form onSubmit={handleSubmit} className="card form">
        <label>
          Fully qualified name
          <input
            placeholder="org.springframework.samples.petclinic.owner.Owner.getPet"
            value={fqn}
            onChange={(e) => setFqn(e.target.value)}
            required
          />
        </label>

        <div className="form-row">
          <label>
            Yön
            <select value={direction} onChange={(e) => setDirection(e.target.value as 'UPSTREAM' | 'DOWNSTREAM')}>
              <option value="UPSTREAM">Upstream (kim çağırıyor)</option>
              <option value="DOWNSTREAM">Downstream (neyi çağırıyor)</option>
            </select>
          </label>

          <label>
            Derinlik (hop)
            <input
              type="number"
              min={1}
              max={10}
              value={depth}
              onChange={(e) => setDepth(Number(e.target.value))}
            />
          </label>
        </div>

        <button type="submit" disabled={impactMutation.isPending}>
          {impactMutation.isPending ? 'Analiz ediliyor...' : 'Analiz Et'}
        </button>

        {impactMutation.isError && (
          <p className="error-text">Bu fqn için bir node bulunamadı, ya da bir hata oluştu.</p>
        )}
      </form>

      {impactMutation.data && (
        <div className="card">
          <h2>Sonuç</h2>
          <p className="impact-summary">
            <code>{impactMutation.data.rootFqn}</code> — {impactMutation.data.direction}, derinlik{' '}
            {impactMutation.data.depth}
          </p>

          {impactMutation.data.affectedNodes.length === 0 ? (
            <p className="empty-state">Etkilenen node bulunamadı.</p>
          ) : (
            <ul className="impact-list">
              {impactMutation.data.affectedNodes.map((n) => (
                <li key={n}>
                  <code>{n}</code>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  );
}