# ai-agent frontend

This is a Vue 3 frontend for `ai-agent`, a two-mode AI chat project by Book. It includes `AI Love Coach` and `Super Agent`.

## Features

- 💬 **AI Love Coach**: an AI relationship guide for dating questions and advice
- 🤖 **Super Agent**: a general-purpose AI assistant for broader questions and practical tasks

## Tech stack

- Vue3
- Vue Router
- Axios
- SSE (Server-Sent Events)

## Development

### Requirements

- Node.js >= 16.0.0
- npm >= 7.0.0

### Install dependencies

```bash
npm install
```

### Start the dev server

```bash
npm run dev
```

### Build the project

```bash
npm run build
```

## Backend endpoints

The frontend expects these backend endpoints:

- `/api/ai/love_app/chat/sse` - AI Love Coach SSE chat endpoint
- `/api/ai/manus/chat` - Super Agent chat endpoint

The backend is expected to run on `http://localhost:8123` by default.

# Vue 3 + Vite

This template should help get you started developing with Vue 3 in Vite. The template uses Vue 3 `<script setup>` SFCs, check out the [script setup docs](https://v3.vuejs.org/api/sfc-script-setup.html#sfc-script-setup) to learn more.

Learn more about IDE Support for Vue in the [Vue Docs Scaling up Guide](https://vuejs.org/guide/scaling-up/tooling.html#ide-support).
