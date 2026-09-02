import { useEffect, useState, type CSSProperties, type ReactNode } from 'react';

type PhoneProps = {
  children: ReactNode;
  /** Frame width in px; the aspect ratio is locked in CSS. */
  width?: number;
  float?: boolean;
  /** Adds a subtle 3D tilt-toward-cursor response on pointer devices. */
  interactive?: boolean;
  className?: string;
  style?: CSSProperties;
  label?: string;
};

/**
 * Pure-CSS iPhone-style frame. The template presents every app screen inside
 * a device like this; building the frame in CSS means the screens inside are
 * live DOM (crisp at any zoom) instead of flat screenshots.
 */
export function PhoneMockup({
  children,
  width = 300,
  float = false,
  interactive = true,
  className,
  style,
  label = 'TDM Insight mobile app screen',
}: PhoneProps) {
  const classes = [
    'lp-phone',
    float ? 'lp-phone--float' : '',
    interactive ? 'lp-phone--interactive' : '',
    className ?? '',
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <div
      className={classes}
      style={{ ...style, ...({ '--lp-phone-w': `${width}px` } as CSSProperties) }}
      role="img"
      aria-label={label}
    >
      <div className="lp-phone__screen">
        <span className="lp-phone__island" />
        {children}
        <span className="lp-phone__home" />
      </div>
    </div>
  );
}

export type PhoneScreen = { key: string; label: string; node: ReactNode };

/**
 * Phone whose screens advance on their own — reproducing the template's
 * rotating device showcase — but which the visitor can also drive directly
 * via the labelled controls underneath. Interacting stops the carousel so it
 * never yanks the screen away mid-read.
 */
export function InteractivePhone({
  screens,
  width = 300,
  float = true,
  intervalMs = 3600,
}: {
  screens: PhoneScreen[];
  width?: number;
  float?: boolean;
  intervalMs?: number;
}) {
  const [current, setCurrent] = useState(0);
  const [auto, setAuto] = useState(true);

  useEffect(() => {
    if (!auto || screens.length <= 1) return;
    if (window.matchMedia?.('(prefers-reduced-motion: reduce)')?.matches) return;

    const timer = window.setInterval(() => {
      if (document.hidden) return;
      setCurrent((i) => (i + 1) % screens.length);
    }, intervalMs);

    return () => window.clearInterval(timer);
  }, [auto, screens.length, intervalMs]);

  const select = (index: number) => {
    setCurrent(index);
    setAuto(false);
  };

  return (
    <div className="lp-device-stage">
      <PhoneMockup width={width} float={float} label={`TDM Insight app — ${screens[current]?.label}`}>
        <div className="lp-phone__stack">
          {screens.map((screen, i) => (
            <div
              key={screen.key}
              className="lp-phone__slide"
              data-lp-current={String(i === current)}
              aria-hidden={i !== current}
            >
              {screen.node}
            </div>
          ))}
        </div>
      </PhoneMockup>

      <div className="lp-device-tabs" role="tablist" aria-label="App screens">
        {screens.map((screen, i) => (
          <button
            key={screen.key}
            type="button"
            role="tab"
            className="lp-device-tab"
            aria-selected={i === current}
            data-lp-active={String(i === current)}
            onClick={() => select(i)}
          >
            {screen.label}
          </button>
        ))}
      </div>
    </div>
  );
}

/**
 * Laptop frame used to showcase the admin dashboard alongside the app —
 * the template's wider "one-stop hub" device treatment.
 */
export function LaptopMockup({ children }: { children: ReactNode }) {
  return (
    <div className="lp-laptop" role="img" aria-label="TDM Insight admin dashboard">
      <div className="lp-laptop__lid">
        <span className="lp-laptop__notch" />
        <div className="lp-laptop__screen">{children}</div>
      </div>
      <div className="lp-laptop__base" />
    </div>
  );
}
