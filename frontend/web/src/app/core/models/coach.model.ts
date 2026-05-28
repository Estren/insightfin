/**
 * Server-sent events emitted by the Coach Agent via POST /api/coach/chat.
 *
 * Wire format (one SSE event per object below):
 *   event: token        — { data: "<text chunk>" }
 *   event: tool_call    — { name: "get_health_score" }
 *   event: citation     — { marker: 1, filename: "regra-50-30-20.md" }
 *   event: error        — { data: "<message>" }
 *   event: done         — {}
 *
 * The UI joins every `token.data` into the assistant bubble, badges the
 * `tool_call` events ("consultando orçamentos..."), and appends citations
 * as numbered chips at the end of the bubble.
 */

export type CoachEvent =
  | { type: 'token'; data: string }
  | { type: 'tool_call'; name: string }
  | { type: 'citation'; marker: number; filename: string }
  | { type: 'error'; data: string }
  | { type: 'done' };

export interface CoachMessage {
  id: string;
  role: 'user' | 'assistant';
  text: string;
  toolCalls: string[];
  citations: { marker: number; filename: string }[];
  isStreaming?: boolean;
  errored?: boolean;
}

export interface CoachSuggestion {
  labelKey: string;
  questionKey: string;
}
