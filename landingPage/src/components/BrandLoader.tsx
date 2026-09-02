import type { CSSProperties } from 'react';
import { BRAND_LOGO_URL, BRAND_NAME, BRAND_TAGLINE } from '@/lib/brand';
import './brandLoader.css';

/**
 * Brand loading mark: the logo tile held steady inside a single hairline
 * sage arc that sweeps around it. Decorative — hidden from assistive tech,
 * since `LoadingScreen` carries the live status text.
 */
export function BrandLoader({ size = 96, className }: { size?: number; className?: string }) {
  return (
    <div
      className={`ke-loader${className ? ` ${className}` : ''}`}
      style={{ '--ke-size': `${size}px` } as CSSProperties}
      aria-hidden
    >
      <svg className="ke-loader__arc" viewBox="0 0 100 100">
        <circle className="ke-loader__arc-track" cx="50" cy="50" r="45" />
        <circle className="ke-loader__arc-head" cx="50" cy="50" r="45" />
      </svg>

      <span className="ke-loader__mark">
        <img src={BRAND_LOGO_URL} alt="" width={64} height={64} decoding="async" />
      </span>
    </div>
  );
}

/**
 * Loading screen for route transitions and session checks.
 * `full` fills the viewport (app boot, auth gate); otherwise it fills the
 * content area inside the existing chrome.
 */
export function LoadingScreen({
  title,
  hint,
  full = false,
  size,
}: {
  /** Status line. Defaults to the brand wordmark treatment when omitted. */
  title?: string;
  hint?: string;
  full?: boolean;
  size?: number;
}) {
  return (
    <div className={`ke-loading${full ? ' ke-loading--full' : ''}`} role="status" aria-live="polite">
      <BrandLoader size={size ?? (full ? 104 : 84)} />

      {full ? (
        <div>
          <p className="ke-loading__wordmark">{BRAND_NAME}</p>
          <p className="ke-loading__tagline">{BRAND_TAGLINE}</p>
        </div>
      ) : null}

      <span className="ke-loading__bar" aria-hidden />

      {title ? (
        <div>
          <p className="ke-loading__status">{title}</p>
          {hint ? (
            <p className="ke-loading__status" style={{ opacity: 0.72, fontSize: '0.8125rem' }}>
              {hint}
            </p>
          ) : null}
        </div>
      ) : null}
    </div>
  );
}
