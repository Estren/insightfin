/**
 * Server-sent events emitted by the Coach Agent via POST /api/coach/chat.
 *
 * Wire format (one SSE event per object below):
 *   event: token         — { data: "<text chunk>" }
 *   event: tool_call     — { name: "get_health_score" }
 *   event: tool_executed — { name, args, result }
 *   event: chart_payload — { kind: "line"|"donut", title, data }
 *   event: citation      — { marker: 1, filename: "regra-50-30-20.md" }
 *   event: error         — { data: "<message>" }
 *   event: done          — {}
 *
 * The UI joins every `token.data` into the assistant bubble, badges the
 * `tool_call` events ("consultando orçamentos..."), accumulates
 * `tool_executed` into an expandable reasoning trail under the message,
 * renders `chart_payload` inline inside the assistant bubble, and appends
 * citations as numbered chips.
 */

export type CoachEvent =
  | { type: 'thread'; id: string }
  | { type: 'token'; data: string }
  | { type: 'tool_call'; name: string }
  | { type: 'tool_executed'; name: string; args: Record<string, unknown>; result: unknown }
  | { type: 'action_proposal'; action: string; params: Record<string, unknown>; summary: string }
  | { type: 'chart_payload'; kind: CoachChartKind; title: string; data: CoachChartData }
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

export type CoachChartKind = 'line' | 'donut';

/** Wire shape (matches the AI service's `present_*_chart` tool descriptors). */
export type CoachChartData =
  | { categories: string[]; series: { name: string; data: number[] }[] }
  | { labels: string[]; series: number[] };

export interface CoachChart {
  kind: CoachChartKind;
  title: string;
  data: CoachChartData;
}

export interface CoachToolExecution {
  name: string;
  args: Record<string, unknown>;
  result: unknown;
}

export interface CoachMessage {
  id: string;
  role: 'user' | 'assistant';
  text: string;
  toolCalls: string[];
  citations: { marker: number; filename: string }[];
  /** Charts the agent decided to show alongside its text. Not persisted across thread hydration (v1). */
  charts?: CoachChart[];
  /** Tool calls + their args + their results — surfaced as an expandable reasoning trail. */
  toolExecutions?: CoachToolExecution[];
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
