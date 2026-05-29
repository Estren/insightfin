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
  | { type: 'thread'; id: string }
  | { type: 'token'; data: string }
  | { type: 'tool_call'; name: string }
  | { type: 'action_proposal'; action: string; params: Record<string, unknown>; summary: string }
  | { type: 'citation'; marker: number; filename: string }
  | { type: 'error'; data: string }
  | { type: 'done' };

export type CoachActionStatus = 'pending' | 'executing' | 'done' | 'error' | 'cancelled';

/** A write action the agent proposed; executed by core-api only after the user confirms. */
export interface CoachActionProposal {
  action: string;
  params: Record<string, unknown>;
  summary: string;
  status: CoachActionStatus;
  resultMessage?: string;
}

export interface CoachMessage {
  id: string;
  role: 'user' | 'assistant';
  text: string;
  toolCalls: string[];
  citations: { marker: number; filename: string }[];
  isStreaming?: boolean;
  errored?: boolean;
  proposal?: CoachActionProposal;
}

export interface CoachSuggestion {
  labelKey: string;
  questionKey: string;
}

/** A persisted conversation shown in the sidebar. */
export interface CoachThread {
  id: string;
  title: string;
  createdAt: string;
  lastMessageAt: string;
}
