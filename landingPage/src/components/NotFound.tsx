import type { CSSProperties } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { ArrowLeft, FlaskConical, Search } from 'lucide-react';
import { BRAND_LOGO_URL, BRAND_NAME } from '@/lib/brand';
import { useDocumentTitle } from '@/lib/useDocumentTitle';
import './notFound.css';

const bounceDelay = (index: number): CSSProperties =>
  ({ '--ke-404-delay': `${index * 0.2}s` }) as CSSProperties;

/**
 * The bouncing "404". Per-character vertical oscillation — y: [0, -15, 0]
 * over 2s easeInOut, looping forever, staggered by index * 0.2s. The middle
 * zero uses a flask motif matching the TDM Insight brand.
 *
 * Decorative: hidden from assistive tech, since the heading carries meaning.
 */
export function BouncingDigits() {
  return (
    <p className="ke-404__digits" aria-hidden>
      <span className="ke-404__digit" style={bounceDelay(0)}>
        4
      </span>

      <span className="ke-404__cup" style={bounceDelay(1)}>
        <span className="ke-404__steam">
          <span />
          <span />
          <span />
        </span>
        <span className="ke-404__cup-ring">
          <FlaskConical strokeWidth={2.4} aria-hidden />
        </span>
      </span>

      <span className="ke-404__digit" style={bounceDelay(2)}>
        4
      </span>
    </p>
  );
}

/** Public 404 — any unknown path outside the app. */
export function NotFoundPage() {
  const location = useLocation();
  useDocumentTitle(`Page not found · ${BRAND_NAME}`);

  return (
    <main className="ke-404">
      <span className="ke-404__grid" aria-hidden />

      <div className="ke-404__inner">
        <img src={BRAND_LOGO_URL} alt="" width={44} height={44} style={{ borderRadius: 12 }} />

        <BouncingDigits />

        <span className="ke-404__badge">
          <FlaskConical size={14} strokeWidth={2.4} />
          No calculation found here
        </span>

        <h1 className="ke-404__title">This page doesn&apos;t exist</h1>

        <p className="ke-404__text">
          We couldn&apos;t find <span className="ke-404__path">{location.pathname}</span>. It may
          have moved, or the link might have a typo.
        </p>

        <div className="ke-404__actions">
          <Link to="/" className="ke-404__btn ke-404__btn--primary">
            <ArrowLeft size={17} strokeWidth={2.4} />
            Back to {BRAND_NAME}
          </Link>
          <Link to="/#features" className="ke-404__btn ke-404__btn--ghost">
            <Search size={17} strokeWidth={2.4} />
            Explore the app
          </Link>
        </div>

        <div className="ke-404__links">
          <Link to="/#how" className="ke-404__link">
            How it works
          </Link>
          <Link to="/#results" className="ke-404__link">
            Results
          </Link>
          <Link to="/#about" className="ke-404__link">
            About
          </Link>
          <Link to="/#download" className="ke-404__link">
            Download
          </Link>
        </div>
      </div>
    </main>
  );
}
