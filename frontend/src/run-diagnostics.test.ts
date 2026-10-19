import { describe, expect, it } from 'vitest';
import { formatDuration, formatFailureReason } from './run-diagnostics';

describe('run diagnostics formatting', () => {
  it('does not invent a duration for deterministic mock runs', () => {
    expect(formatDuration(null)).toBe('Not measured (mock mode)');
  });

  it('formats measured durations and rejects invalid values', () => {
    expect(formatDuration(1250)).toBe('1.25 s');
    expect(formatDuration(-1)).toBe('Unavailable');
  });

  it('maps safe failure codes to human-readable text without exposing raw exceptions', () => {
    expect(formatFailureReason('Failed', 'PROVIDER_UNAVAILABLE')).toBe('Model provider unavailable');
    expect(formatFailureReason('Failed', null)).toBe('Run failed; safe details unavailable.');
    expect(formatFailureReason('Succeeded', null)).toBe('No failure recorded');
  });
});
