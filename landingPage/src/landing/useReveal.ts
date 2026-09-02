import { useEffect, useState } from 'react';

const REVEAL_SELECTOR = '[data-lp-reveal]';

function markVisible(node: Element): void {
  node.setAttribute('data-lp-visible', 'true');
}

/**
 * Scroll-reveal driver for the landing page.
 *
 * The Framer template animates every block as it enters the viewport, so we
 * reproduce that with a single IntersectionObserver that flips
 * `data-lp-visible` on any `[data-lp-reveal]` node. The CSS in landing.css
 * owns the actual animation, which keeps this cheap and lets elements
 * animate once and stay put (matching the template's one-shot entrances).
 *
 * Because the un-revealed state is `opacity: 0`, this is written defensively.
 * Alongside the observer there is a scroll/resize-driven pass that reveals
 * anything currently in view, so content can never be stranded invisible if
 * the observer never delivers — a hidden or not-yet-rendered document defers
 * IntersectionObserver callbacks. The fallback mirrors the observer's own
 * rule rather than revealing everything, so the staggered scroll animations
 * are preserved either way.
 */
export function useReveal(): void {
  useEffect(() => {
    const nodes = Array.from(document.querySelectorAll<HTMLElement>(REVEAL_SELECTOR));
    if (nodes.length === 0) return;

    const reduceMotion = window.matchMedia?.('(prefers-reduced-motion: reduce)')?.matches;
    if (reduceMotion || !('IntersectionObserver' in window)) {
      nodes.forEach(markVisible);
      return;
    }

    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (!entry.isIntersecting) return;
          markVisible(entry.target);
          observer.unobserve(entry.target);
        });
      },
      // Fire slightly before the block is fully on screen, like the template.
      { threshold: 0.12, rootMargin: '0px 0px -8% 0px' },
    );

    // Reveals anything currently within (or above) the viewport without
    // waiting on the observer, so the first screen is never blank.
    const revealInView = () => {
      const limit = window.innerHeight * 0.95;
      nodes.forEach((node) => {
        if (node.hasAttribute('data-lp-visible')) return;
        if (node.getBoundingClientRect().top <= limit) {
          markVisible(node);
          observer.unobserve(node);
        }
      });
    };

    nodes.forEach((node) => observer.observe(node));

    // Run once now, then again after layout settles (web fonts and the
    // device mockups can shift positions on first paint).
    revealInView();
    const raf = window.requestAnimationFrame(revealInView);
    const settle = window.setTimeout(revealInView, 350);

    // Scroll-driven safety net, rAF-throttled. Applies the same in-view rule
    // as the observer, so it both preserves the staggered animations and
    // guarantees nothing stays invisible if the observer never fires.
    let queued = false;
    const onScroll = () => {
      if (queued) return;
      queued = true;
      window.requestAnimationFrame(() => {
        queued = false;
        revealInView();
      });
    };

    const onVisibility = () => {
      if (!document.hidden) revealInView();
    };

    window.addEventListener('scroll', onScroll, { passive: true });
    window.addEventListener('load', revealInView);
    window.addEventListener('resize', revealInView);
    document.addEventListener('visibilitychange', onVisibility);

    return () => {
      window.cancelAnimationFrame(raf);
      window.clearTimeout(settle);
      window.removeEventListener('scroll', onScroll);
      window.removeEventListener('load', revealInView);
      window.removeEventListener('resize', revealInView);
      document.removeEventListener('visibilitychange', onVisibility);
      observer.disconnect();
    };
  }, []);
}

/** Tracks whether the page has scrolled past `offset`, for the sticky nav. */
export function useScrolled(offset = 24): boolean {
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > offset);
    onScroll();
    window.addEventListener('scroll', onScroll, { passive: true });
    return () => window.removeEventListener('scroll', onScroll);
  }, [offset]);

  return scrolled;
}

/**
 * Auto-advancing index used to cycle the app screens inside the hero phone,
 * reproducing the template's rotating device screenshots. Pauses while the
 * tab is hidden so it never runs off-screen.
 */
export function useRotatingIndex(length: number, intervalMs = 3600): number {
  const [index, setIndex] = useState(0);

  useEffect(() => {
    if (length <= 1) return;
    if (window.matchMedia?.('(prefers-reduced-motion: reduce)')?.matches) return;

    const timer = window.setInterval(() => {
      if (document.hidden) return;
      setIndex((i) => (i + 1) % length);
    }, intervalMs);

    return () => window.clearInterval(timer);
  }, [length, intervalMs]);

  return index;
}
