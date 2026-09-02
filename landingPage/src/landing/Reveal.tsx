import type { CSSProperties, ElementType, ReactNode } from 'react';

export type RevealKind = 'rise' | 'fade' | 'zoom' | 'left' | 'right' | 'device';

type RevealProps = {
  children: ReactNode;
  /** Which entrance animation to play. Mirrors the template's block motions. */
  kind?: RevealKind;
  /** Stagger, in ms — the template cascades siblings ~90ms apart. */
  delay?: number;
  as?: ElementType;
  className?: string;
  style?: CSSProperties;
  id?: string;
};

/**
 * Marks a block to animate in on scroll. The actual animation lives in
 * landing.css; `useReveal()` (called once by LandingPage) flips these on.
 */
export function Reveal({
  children,
  kind = 'rise',
  delay = 0,
  as: Tag = 'div',
  className,
  style,
  id,
}: RevealProps) {
  return (
    <Tag
      id={id}
      className={className}
      data-lp-reveal={kind}
      style={{ ...style, ...({ '--lp-delay': `${delay}ms` } as CSSProperties) }}
    >
      {children}
    </Tag>
  );
}
