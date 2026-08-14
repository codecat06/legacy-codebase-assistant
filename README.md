# Legacy Codebase Onboarding Assistant

An AI-powered assistant that helps engineers understand large, unfamiliar codebases. It parses a Java/Spring Boot repository into a structural **code graph** (classes, methods, call relationships) and a **semantic vector index**, then combines both to answer natural-language questions and perform deterministic impact analysis — without ever guessing.

> "How does this endpoint go from the request to the database?"
> "If I change `PaymentService`, what breaks?"

This project answers both kinds of questions: the first with LLM-generated, source-cited answers; the second with a fast, deterministic graph traversal — no LLM involved.

---

## Why hybrid retrieval instead of plain RAG

Pure vector search finds text that *sounds* related to your question, but it misses multi-hop relationships: a controller method rarely mentions the repository three layers down that it ultimately depends on. This project combines two retrieval strategies:

1. **Semantic search (Qdrant)** — embeds source code and questions with a local embedding model, finds the methods most relevant in *meaning*.
2. **Graph traversal (PostgreSQL)** — starting from those semantic matches, walks the real `CALLS` relationships a few hops in either direction to pull in the surrounding context (callers and callees) that a text-only search would miss.

The combined context is what gets handed to the LLM — or, for impact analysis, used directly without an LLM at all.

---

## Architecture

```
                     ┌─────────────────────────────┐
   React UI  ───────▶│      Spring Boot API         │
  (Vite, TS,          │                              │
   axios, react-query)│  Ingestion · Retrieval · QA  │
                       └───────┬──────────────────────┘
                               │
              ┌────────────────┼──────────────────┐
              ▼                ▼                    ▼
     Parser Sidecar      PostgreSQL              Qdrant
     (Node.js +           ─────────              ────────
      tree-sitter)        code_node               code chunk
     Java source           code_edge               embeddings
     → AST → JSON          (call graph)
                               │
                               ▼
                            Ollama
                  (qwen2.5-coder — chat,
                   nomic-embed-text — embeddings)
```

**Ingestion pipeline:** clone (JGit) → parse each `.java` file (tree-sitter, via a Node.js sidecar) → build the call graph in Postgres → embed every method and store the vectors in Qdrant.

**Query time:** a question is embedded → Qdrant returns the nearest methods → the graph is expanded a few hops from those seeds → the combined source code is sent to a local Ollama model for a cited, natural-language answer (`/api/ask`), or the expansion result is returned directly as a deterministic impact list (`/api/impact`).

---

## Features

- **Repository ingestion** — point it at any public Git URL; it clones, parses, and indexes the codebase asynchronously with live job status polling.
- **Code graph** — classes and methods as nodes, `CALLS` relationships as edges, stored in PostgreSQL.
- **Semantic code search** — every method is embedded and stored in Qdrant for meaning-based retrieval.
- **Hybrid RAG chat** (`/api/ask`) — ask questions in plain English, get answers grounded in real source code, with cited method references.
- **Impact analysis** (`/api/impact`) — deterministic, LLM-free upstream/downstream traversal: "who calls this" vs. "what does this depend on."
- **Re-ingestion without duplication** — re-indexing an already-known repository reuses its identity and replaces its graph/vectors instead of duplicating them.
- **Web UI** — connect a repo, watch ingestion progress, chat with the codebase, and run impact queries, all from the browser.

---

## Tech stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 4, Spring Data JPA, Spring Security |
| Parsing | Node.js, tree-sitter (WASM), Express |
| Database | PostgreSQL (relational data + code graph) |
| Vector store | Qdrant |
| LLM runtime | Ollama (`qwen2.5-coder` for chat, `nomic-embed-text` for embeddings) |
| Frontend | React, TypeScript, Vite, axios, React Router, TanStack Query, react-markdown |
| Repo cloning | JGit |

Everything runs locally — no external API keys, no cloud LLM calls.

---

## Getting started

### Prerequisites

- Java 21, Maven (wrapper included)
- Node.js 20+
- Docker (for PostgreSQL, Qdrant, Ollama)

### 1. Start the infrastructure

```bash
docker compose up -d
```

Pull the required Ollama models:

```bash
docker exec -it <ollama-container> ollama pull qwen2.5-coder
docker exec -it <ollama-container> ollama pull nomic-embed-text
```

### 2. Start the parser sidecar

```bash
cd parser-service
npm install
npm start   # listens on :3001
```

### 3. Start the backend

```bash
./mvnw spring-boot:run   # listens on :8080
```

### 4. Start the frontend

```bash
cd frontend
npm install
npm run dev   # http://localhost:5173
```

Open the app, connect a repository (e.g. `https://github.com/spring-projects/spring-petclinic.git`), wait for ingestion to finish, then start asking questions or running impact analysis.

---

## API overview

| Endpoint | Method | Purpose |
|---|---|---|
| `/api/repos` | `POST` | Start ingesting a repository |
| `/api/repos/jobs/{jobId}` | `GET` | Poll ingestion status |
| `/api/ask` | `POST` | Ask a natural-language question, get a cited answer |
| `/api/retrieve` | `POST` | Raw hybrid retrieval result (no LLM) |
| `/api/impact` | `GET` | Deterministic upstream/downstream impact analysis |

---

## Known limitations

- Call resolution is best-effort: it relies on field/parameter type names rather than full type inference, so it can't perfectly resolve polymorphism, generics, or ambiguous class names across packages.
- Nested/inner classes are not currently extracted (only top-level classes per file).
- Re-ingestion replaces the whole graph rather than diffing changed files — there's no true incremental indexing yet.
- Currently tuned for Java/Spring Boot codebases (tree-sitter grammar and heuristics are Java-specific).

---

## Project structure

```
codeintel/
  src/main/java/...        # Spring Boot backend
  parser-service/          # Node.js + tree-sitter parsing sidecar
  frontend/                # React + TypeScript UI
  compose.yaml              # Postgres, Qdrant, Ollama
```