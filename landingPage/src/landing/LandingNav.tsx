import { useEffect, useState } from 'react';
import { ArrowRight, Menu, X } from 'lucide-react';
import { BRAND_LOGO_URL, BRAND_NAME, BRAND_TAGLINE } from '@/lib/brand';
import { useScrolled } from './useReveal';

/**
 * Public navigation only. The admin dashboard is deliberately NOT linked
 * from anywhere on the landing page — staff reach it by navigating to
 * /admin directly, which keeps the login surface unadvertised.
 */
const LINKS = [
  { href: '#features', label: 'Features' },
  { href: '#how', label: 'How it works' },
  { href: '#results', label: 'Results' },
  { href: '#reviews', label: 'Reviews' },
  { href: '#about', label: 'About' },
] as const;

/** Highlights the nav item whose section is currently on screen. */
function useActiveSection(): string {
  const [active, setActive] = useState('');

  useEffect(() => {
    const sections = LINKS.map((l) => document.querySelector(l.href)).filter(
      (el): el is Element => el !== null,
    );
    if (sections.length === 0) return;

    const observer = new IntersectionObserver(
      (entries) => {
        const visible = entries
          .filter((e) => e.isIntersecting)
          .sort((a, b) => b.intersectionRatio - a.intersectionRatio)[0];
        if (visible?.target.id) setActive(`#${visible.target.id}`);
      },
      { rootMargin: '-45% 0px -45% 0px', threshold: [0, 0.25, 0.5] },
    );

    sections.forEach((s) => observer.observe(s));
    return () => observer.disconnect();
  }, []);

  return active;
}

export function LandingNav() {
  const stuck = useScrolled(28);
  const active = useActiveSection();
  const [open, setOpen] = useState(false);

  // Lock background scroll while the mobile drawer is open.
  useEffect(() => {
    if (!open) return;
    const prev = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    return () => {
      document.body.style.overflow = prev;
    };
  }, [open]);

  // Escape closes the drawer.
  useEffect(() => {
    if (!open) return;
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setOpen(false);
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [open]);

  // Close the drawer if the viewport grows back to desktop width.
  useEffect(() => {
    if (!open) return;
    const mq = window.matchMedia('(min-width: 901px)');
    const onChange = () => {
      if (mq.matches) setOpen(false);
    };
    mq.addEventListener('change', onChange);
    return () => mq.removeEventListener('change', onChange);
  }, [open]);

  const brand = (
    <a href="#top" className="lp-brand" aria-label={`${BRAND_NAME} home`} onClick={() => setOpen(false)}>
      <img src={BRAND_LOGO_URL} alt="" className="lp-brand__mark" width={40} height={40} />
      <span>
        <span className="lp-brand__name">{BRAND_NAME}</span>
        <span className="lp-brand__tag">{BRAND_TAGLINE}</span>
      </span>
    </a>
  );

  return (
    <>
      <header className="lp-header" data-lp-stuck={String(stuck)}>
        <nav className="lp-nav" aria-label="Main">
          {brand}

          <div className="lp-nav__links">
            {LINKS.map((link) => (
              <a
                key={link.href}
                href={link.href}
                className="lp-nav__link"
                data-lp-active={String(active === link.href)}
              >
                {link.label}
              </a>
            ))}
          </div>

          <div className="lp-nav__actions">
            <a href="#download" className="lp-btn lp-btn--primary lp-btn--sm lp-nav__cta">
              Get the app
              <ArrowRight size={16} strokeWidth={2.5} className="lp-btn__icon" />
            </a>

            <button
              type="button"
              className="lp-burger"
              onClick={() => setOpen((v) => !v)}
              aria-label={open ? 'Close menu' : 'Open menu'}
              aria-expanded={open}
              aria-controls="lp-mobile-menu"
            >
              {open ? <X size={18} strokeWidth={2.4} /> : <Menu size={18} strokeWidth={2.4} />}
            </button>
          </div>
        </nav>
      </header>

      {open ? (
        <div className="lp-drawer" id="lp-mobile-menu">
          {/* Tapping anywhere outside the panel closes the menu. */}
          <button
            type="button"
            className="lp-drawer__backdrop"
            aria-label="Close menu"
            onClick={() => setOpen(false)}
          />

          <div className="lp-drawer__panel" role="dialog" aria-modal="true" aria-label="Menu">
            <div className="lp-drawer__head">
              {brand}
              <button
                type="button"
                className="lp-burger"
                onClick={() => setOpen(false)}
                aria-label="Close menu"
              >
                <X size={18} strokeWidth={2.4} />
              </button>
            </div>

            <nav className="lp-drawer__links" aria-label="Mobile">
              {LINKS.map((link) => (
                <a
                  key={link.href}
                  href={link.href}
                  className="lp-drawer__link"
                  onClick={() => setOpen(false)}
                >
                  {link.label}
                  <ArrowRight size={17} strokeWidth={2.2} />
                </a>
              ))}
            </nav>

            <a href="#download" className="lp-btn lp-btn--primary lp-drawer__cta" onClick={() => setOpen(false)}>
              Get the app
              <ArrowRight size={17} strokeWidth={2.5} />
            </a>
          </div>
        </div>
      ) : null}
    </>
  );
}
