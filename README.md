# AI Agent Development

A learning project I completed working through a comprehensive AI development course. Built two AI applications from scratch using Java/Spring AI — a conversational chatbot with a custom knowledge base, and an autonomous agent that plans and executes tasks.

---

## What I Built

**AI Love Master** — a multi-turn conversational AI that retrieves answers from a custom knowledge base, calls external tools (map services, web search), and integrates with MCP services. Users can ask questions and the system searches relevant documents before responding rather than relying purely on the model's training data.

**OpenManus Agent** — an autonomous agent based on the ReAct pattern. Give it a goal, it figures out the steps, calls the necessary tools, handles failures, and generates a final report. Built on top of the tool-calling infrastructure from the previous module.

```mermaid
graph TD
    User([User])

    User -->|chat| AiLoveMaster
    User -->|goal| OpenManus

    subgraph AiLoveMaster[AI Love Master]
        Chat[Conversation Manager]
        Memory[Chat Memory]
        Chat <--> Memory
    end

    subgraph OpenManus[OpenManus Agent]
        Planner[ReAct Planner]
        Executor[Tool Executor]
        Planner -->|call tool| Executor
        Executor -->|result| Planner
    end

    AiLoveMaster -->|query| RAG
    AiLoveMaster -->|prompt + context| LLM
    OpenManus -->|prompt + tool results| LLM

    subgraph RAG[Knowledge Base]
        VectorDB[(PGVector)]
        Retriever[Similarity Search]
        VectorDB --> Retriever
    end

    AiLoveMaster --> Tools
    OpenManus --> Tools
    AiLoveMaster -->|standardized calls| MCP[MCP Services]

    subgraph Tools[Tools]
        Search[Web Search]
        Maps[Map API]
        PDF[PDF Generator]
        Scraper[Web Scraper]
    end

    LLM[LLM\nQwen / OpenAI / Ollama]
```

---

## Tech Stack

- Java 21 + Spring Boot 3
- Spring AI
- PostgreSQL + PGvector (vector database)
- Ollama (local model deployment)
- Jsoup (web scraping), iText (PDF generation)
- External APIs: SearchAPI, Pexels, Amap

---

## Getting Started

**Requirements:** Java 21+, PostgreSQL 14+ with pgvector, Maven

```bash
git clone https://github.com/yourusername/ai-agent-development.git
cd ai-agent-development
cp .env.example .env
# Add your API keys to .env
psql -U postgres -d ai_agent < schema.sql
mvn spring-boot:run
```

API docs at http://localhost:8080/doc.html

---

## What I Learned

- How RAG actually works in practice — chunking documents, embedding them, and searching by vector similarity before passing context to the LLM
- Spring AI's abstractions for LLM integration, conversation memory, and structured output
- Tool calling patterns — defining tools the model can request, executing them in the application layer, returning results
- MCP protocol for standardized service integration
- How the ReAct pattern works for autonomous agent reasoning

### RAG pipeline

```mermaid
flowchart LR
    subgraph Ingestion[Document Ingestion - done once]
        Docs[Raw Documents] --> Chunker[Chunker]
        Chunker --> Embedder[Embedding Model]
        Embedder --> VectorDB[(PGVector)]
    end

    subgraph QueryTime[Query Time - every request]
        Q[User Question] --> QEmbed[Embedding Model]
        QEmbed --> Search[Similarity Search]
        VectorDB -.->|stored vectors| Search
        Search --> Chunks[Relevant Chunks]
        Chunks --> Prompt[Augmented Prompt]
        Q --> Prompt
        Prompt --> LLM[LLM]
        LLM --> Answer[Answer]
    end
```

### ReAct agent loop

```mermaid
flowchart TD
    Goal([User Goal]) --> Reason

    Reason[Reason: what do I need next?]
    Reason --> Act[Act: call a tool]
    Act --> Observe[Observe: read the result]
    Observe --> Check{Goal reached?}
    Check -- No --> Reason
    Check -- Yes --> Report([Generate final report])
```

---
