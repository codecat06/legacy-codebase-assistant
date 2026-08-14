import { useState, type FormEvent } from 'react';
import { useMutation } from '@tanstack/react-query';
import ReactMarkdown from 'react-markdown';
import { askQuestion } from '../api/chat';
import type { AskResponse } from '../types';

interface ChatEntry {
  question: string;
  response: AskResponse;
}

export default function ChatPage() {
  const [question, setQuestion] = useState('');
  const [history, setHistory] = useState<ChatEntry[]>([]);

  const askMutation = useMutation({
    mutationFn: askQuestion,
    onSuccess: (data, variables) => {
      setHistory((h) => [...h, { question: variables.question, response: data }]);
      setQuestion('');
    },
  });

  function handleSubmit(e: FormEvent) {
    e.preventDefault();
    if (!question.trim() || askMutation.isPending) return;
    askMutation.mutate({ question, topK: 5, hops: 2 });
  }

  return (
    <div className="page page-chat">
      <h1>Kod Tabanına Soru Sor</h1>
      <p className="page-subtitle">
        Cevaplar, semantik arama (Qdrant) ve kod grafiği (Postgres) ile bulunan gerçek kaynak koduna
        dayanarak Ollama üzerinden üretiliyor.
      </p>

      <div className="chat-history">
        {history.length === 0 && !askMutation.isPending && (
          <div className="empty-state">Henüz soru sormadın. Örnek: "how is a pet updated for an owner"</div>
        )}

        {history.map((entry, i) => (
          <div key={i} className="chat-entry">
            <div className="chat-question">{entry.question}</div>
            <div className="chat-answer">
              <ReactMarkdown>{entry.response.answer}</ReactMarkdown>
            </div>
            <details className="chat-sources">
              <summary>{entry.response.sources.length} kaynak kod parçası</summary>
              <ul>
                {entry.response.sources.map((s) => (
                  <li key={s}>
                    <code>{s}</code>
                  </li>
                ))}
              </ul>
            </details>
          </div>
        ))}

        {askMutation.isPending && (
          <div className="chat-entry">
            <div className="chat-question">{askMutation.variables?.question}</div>
            <div className="chat-loading">Düşünüyor... (ilk soruda model yüklenirken biraz sürebilir)</div>
          </div>
        )}

        {askMutation.isError && <p className="error-text">Cevap alınamadı. Backend/Ollama çalışıyor mu kontrol et.</p>}
      </div>

      <form onSubmit={handleSubmit} className="chat-form">
        <input
          placeholder="Soru sor..."
          value={question}
          onChange={(e) => setQuestion(e.target.value)}
        />
        <button type="submit" disabled={askMutation.isPending}>
          Sor
        </button>
      </form>
    </div>
  );
}