# Insurance Policy Intelligence System

> A **framework-free** RAG (Retrieval-Augmented Generation) backend that ingests insurance policy PDFs and answers natural-language questions about them — grounded in the actual document, with source citations and a built-in "I don't know" guardrail.

This is a **learning project**, not a production insurance product. The goal was to move past "call an LLM and print the response" and understand what it actually takes to build an AI system **end-to-end** — ingestion, retrieval, generation, prompt design, guardrails, and observability.

> **Status:** The backend (ingestion + RAG API) is functional. An Angular frontend is in progress and not yet complete — this README focuses on the backend and its APIs.

## Why build it from scratch?

One deliberate design choice drives this whole repo: **I built the RAG pipeline by hand with a bare SDK, no orchestration framework.**

It would have been faster to reach for LangChain or LangGraph. But frameworks hide the very things I wanted to understand — how retrieval stitches into a prompt, how context gets assembled, where the guardrails actually live. So I wired every piece myself first.

The bet: when I *do* adopt those frameworks later, I'll understand the abstractions they provide — because I'll know exactly what they're abstracting away.

> Note: `langchain4j` appears as a dependency, but it is used **only** to run the local embedding model in-process. All orchestration — retrieval, prompt assembly, confidence gating, tracing — is custom Java.

## What it does

1. **Ingest** an insurance policy PDF → extract text → chunk it → embed each chunk → persist the vectors.
2. **Ask** a natural-language question about a policy.
3. **Retrieve** the most relevant excerpts via vector similarity search.
4. **Generate** a grounded answer that cites the excerpts it used — or declines to answer if the policy doesn't clearly cover it.

## How it works

### Ingestion pipeline
- The uploaded PDF is stored on disk under `uploads/{company}/{policy}/{uuid}.pdf`; metadata is saved to the database.
- **Apache PDFBox** extracts and normalizes the text.
- The text is split into **500-word ordered chunks**.
- Each chunk is embedded locally (**AllMiniLM-L6-v2**, 384 dimensions, ONNX) and persisted to the `document_embeddings` table (pgvector).

### Query pipeline (RAG)
1. The user's question is embedded locally using the same model.
2. pgvector finds the **top-5** nearest chunks by cosine distance, filtered to the requested document.
3. **Confidence gate:** if the nearest chunk's distance exceeds the threshold (`0.5`), the API returns a safe fallback — *"I could not confidently determine this from the policy."* — instead of hallucinating.
4. The retrieved excerpts are assembled into context using externalized prompt templates.
5. The LLM is called, and the response is returned **along with the source chunks** it was based on.

```
Upload PDF ──► PDFBox extract ──► chunk (500 words) ──► embed (MiniLM/ONNX) ──► pgvector
                                                                                    │
Question ──► embed ──► top-5 cosine search ──► confidence gate ──► prompt ──► LLM ──┘──► Answer + sources
```

## Key concepts explored

| Concept | How it's implemented |
|---|---|
| **Hand-built RAG** | Every step wired manually — no LangChain/LangGraph orchestration |
| **Local embeddings** | AllMiniLM-L6-v2 via ONNX, in-process — no embedding API, no per-call cost |
| **Prompt engineering** | System + user prompt templates externalized to `prompts.properties` (treated as config, not hardcoded strings) |
| **Hallucination guardrail** | Cosine-distance confidence threshold with a graceful fallback response |
| **Source attribution** | Every answer returns the exact chunks (id, distance, order, text) behind it |
| **Observability** | End-to-end LangSmith tracing: retrieval latency, LLM latency, prompt, response, per-chunk metadata |
| **Pluggable LLM providers** | `LlmProvider` interface + `LlmFactory` — swapping models is a config change, not a rewrite |
| **Persistent vector store** | pgvector column (`VECTOR`, 384-dim) with native cosine-distance search (`<=>`) |

## Tech stack

**Backend**
- Java 25
- Spring Boot 4.0.5 — Web, WebFlux (reactive `WebClient` for LLM calls), Data JPA
- Apache PDFBox 3.0.1 — PDF text extraction
- LangChain4j 0.35.0 — local embedding model (AllMiniLM-L6-v2)
- Hibernate Vector 7.2.7 — pgvector integration

**Data**
- PostgreSQL + pgvector — persistent vector store (`insurance_vectors` DB)

**AI / LLM**
- OpenRouter — LLM provider (`meta-llama/llama-3.1-8b-instruct`)
- AllMiniLM-L6-v2 — 384-dim embeddings, run locally via ONNX
- LangSmith — tracing & observability

**Frontend** *(in progress)*
- Angular 17 · Angular Material · RxJS · TypeScript

## Prompt design

Prompts live in [`src/main/resources/prompts.properties`](src/main/resources/prompts.properties), kept out of the code so they can be iterated and versioned independently.

- **System prompt** — constrains the model to answer *only* from the provided excerpts, cite excerpt numbers, flag insufficient or conflicting information, and avoid speculation.
- **User template** — a parameterized template that injects the question and the retrieved context.

## API reference

Base URL: `http://localhost:8080`

### `POST /documents/upload`
Uploads a policy PDF, then extracts, chunks, embeds, and persists it. This is the ingestion entry point.

**Request** — `multipart/form-data`

| Field | Type | Description |
|---|---|---|
| `companyName` | string | Insurer name (used in the storage path) |
| `policyName` | string | Policy name (used in the storage path) |
| `documentType` | string | Type/category of the document |
| `file` | file | The policy PDF (max 10 MB) |

```bash
curl -X POST http://localhost:8080/documents/upload \
  -F "companyName=STARHEALTH" \
  -F "policyName=StarComprehensiveInsurancePolicy" \
  -F "documentType=policy" \
  -F "file=@policy.pdf"
```

---

### `POST /answer/vector-search`
The primary RAG endpoint. Embeds the question, retrieves the top-5 relevant chunks, applies the confidence gate, and returns a grounded answer **with its source excerpts**.

**Request** — `application/json`

| Field | Type | Description |
|---|---|---|
| `question` | string | The natural-language question |
| `documentId` | number | The document to search within |

```bash
curl -X POST http://localhost:8080/answer/vector-search \
  -H "Content-Type: application/json" \
  -d '{"question": "What is the waiting period for pre-existing diseases?", "documentId": 1}'
```

The response contains the generated answer plus a `sources` array (chunk id, cosine distance, chunk order, and chunk text) so every answer is traceable back to the document.

---

### `POST /answer/generate`
An alternate grounded-answer generation endpoint. Same request shape as `/answer/vector-search` (`question`, `documentId`).

---

### `GET /answer/health`
Service health check. Returns the status of the answer service.

```bash
curl http://localhost:8080/answer/health
```

---

### `GET /answer/providers`
Lists the available LLM providers discovered via the pluggable provider abstraction, and indicates which one is active.

```bash
curl http://localhost:8080/answer/providers
```

## Getting started

### Prerequisites
- Java 25
- Maven
- PostgreSQL with the [pgvector](https://github.com/pgvector/pgvector) extension
- An [OpenRouter](https://openrouter.ai/) API key
- A [LangSmith](https://smith.langchain.com/) API key (optional — for tracing)

### 1. Database

```sql
CREATE DATABASE insurance_vectors;
\c insurance_vectors
CREATE EXTENSION IF NOT EXISTS vector;
```

### 2. Configuration

The repo ships with placeholders, not real keys. Provide your secrets (e.g. via environment variables) and update the datasource credentials in [`application.properties`](src/main/resources/application.properties) if yours differ.

Key configurable settings:

| Property | Default | Purpose |
|---|---|---|
| `llm.provider` | `openrouter` | Active LLM provider |
| `openrouter.model` | `meta-llama/llama-3.1-8b-instruct` | Model used for generation |
| `openrouter.temperature` | `0.3` | Sampling temperature |
| `openrouter.max-tokens` | `1000` | Max output tokens |
| `vector.search.confidence.threshold` | `0.5` | Max cosine distance before the "can't determine" fallback |
| `langsmith_tracing_v2` | `true` | Toggle LangSmith tracing |

### 3. Run

```bash
mvn spring-boot:run
```

The API starts on `http://localhost:8080`.

## Roadmap

This is an evolving learning project. Next steps:

- Complete the **Angular frontend** and wire it to the live APIs.
- Expand the pluggable provider abstraction with more LLM backends.
- **Next phase:** re-implement the pipeline with a framework (LangChain / LangGraph) and compare the abstractions against this hand-built version.

## License

This project is shared for learning and reference purposes.
