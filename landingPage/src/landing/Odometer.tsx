import type { CSSProperties } from 'react';

/**
 * Rolling-digit counter, cloned from the template's stats strip (its markup
 * renders literal "0 1 2 3 4…" reels per digit and slides each one up to its
 * final value). Each digit is a 0-9 column inside a 1-character-tall window;
 * CSS translates the column by -N digits when the block scrolls into view.
 */
function Digit({ digit, delay }: { digit: number; delay: number }) {
  return (
    <span className="lp-odo" aria-hidden>
      <span
        className="lp-odo__reel"
        style={
          {
            '--lp-digit-shift': `-${digit * 1.05}em`,
            '--lp-digit-delay': `${delay}ms`,
          } as CSSProperties
        }
      >
        {Array.from({ length: 10 }, (_, n) => (
          <span key={n} className="lp-odo__digit">
            {n}
          </span>
        ))}
      </span>
    </span>
  );
}

type OdometerProps = {
  /** Numeric portion, e.g. "12.4" or "40". Digits roll; separators stay put. */
  value: string;
  prefix?: string;
  suffix?: string;
  label: string;
};

export function Odometer({ value, prefix, suffix, label }: OdometerProps) {
  const chars = value.split('');

  return (
    <div className="lp-stat">
      <div className="lp-stat__value">
        {prefix ? <span>{prefix}</span> : null}
        {chars.map((char, i) => {
          const digit = Number.parseInt(char, 10);
          if (Number.isNaN(digit)) {
            // "." and "," are static, exactly as in the template.
            return <span key={`${char}-${i}`}>{char}</span>;
          }
          // Later digits start slightly later, producing the cascading roll.
          return <Digit key={`${char}-${i}`} digit={digit} delay={i * 110} />;
        })}
        {suffix ? <span>{suffix}</span> : null}
        {/* Screen readers get the plain value instead of the reels. */}
        <span className="sr-only">{`${prefix ?? ''}${value}${suffix ?? ''}`}</span>
      </div>
      <p className="lp-stat__label">{label}</p>
    </div>
  );
}
