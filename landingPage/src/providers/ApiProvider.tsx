import type { ReactNode } from 'react';
import { API_URL, hasApi } from '@/lib/apiClient';

export { hasApi, API_URL };

/**
 * The REST client is stateless, so this is a plain pass-through. It exists so
 * main.tsx keeps a single obvious place to wrap the app if the data layer ever
 * needs context again.
 */
export function AppApiProvider({ children }: { children: ReactNode }) {
  return children;
}

export function ApiSetupNotice({ context }: { context: string }) {
  return (
    <div className="mx-auto mt-16 max-w-md rounded-2xl border border-outline-variant/50 bg-surface p-6 text-center shadow-soft">
      <h2 className="font-display text-xl font-semibold text-coffee-dark">API not configured</h2>
      <p className="mt-2 text-sm text-muted">
        {context} needs the TDM Insight API. Set <code className="rounded bg-cream px-1">VITE_API_URL</code>{' '}
        in <code className="rounded bg-cream px-1">.env.local</code> (for example{' '}
        <code className="rounded bg-cream px-1">http://localhost:4000/api</code>).
      </p>
      <p className="mt-2 text-sm text-muted">
        Start the backend with <code className="rounded bg-cream px-1">npm run api</code> from the
        project root.
      </p>
    </div>
  );
}
