import { describe, expect, it } from 'vitest';
import { getRunTone } from './run-status';

describe('getRunTone', () => {
  it('maps known run states to a finite presentation tone', () => {
    expect(getRunTone('Succeeded')).toBe('success');
    expect(getRunTone('Awaiting approval')).toBe('pending');
    expect(getRunTone('Failed')).toBe('failed');
    expect(getRunTone('Running')).toBe('active');
    expect(getRunTone('Queued')).toBe('neutral');
  });

  it('does not pass unknown input through as a CSS class', () => {
    expect(getRunTone('user-controlled-class')).toBe('neutral');
    expect(getRunTone('')).toBe('neutral');
  });
});
