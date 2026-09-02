/**
 * TDM Insight app screens — rendered as HTML inside CSS device frames.
 * Mirrors the real Kotlin/Compose app: Home, Patient Input, Calculating,
 * Results, and Explanation screens.
 */
import {
  Activity,
  AlertCircle,
  ArrowRight,
  BarChart2,
  BookOpen,
  Check,
  ChevronRight,
  ClipboardList,
  FlaskConical,
  History,
  Home,
  Info,
  Settings,
  Wifi,
  BatteryFull,
  SignalHigh,
} from 'lucide-react';

function StatusBar({ dark = false }: { dark?: boolean }) {
  return (
    <div className="lp-app__status" style={dark ? { color: '#fff' } : undefined}>
      <span>9:41</span>
      <div className="lp-app__status-icons">
        <SignalHigh size={9} strokeWidth={2.5} />
        <Wifi size={9} strokeWidth={2.5} />
        <BatteryFull size={11} strokeWidth={2.5} />
      </div>
    </div>
  );
}

function TabBar({ active }: { active: 'home' | 'history' | 'settings' }) {
  const tabs = [
    { key: 'home',     label: 'Home',     Icon: Home },
    { key: 'history',  label: 'History',  Icon: History },
    { key: 'settings', label: 'Settings', Icon: Settings },
  ] as const;

  return (
    <div className="lp-app__tabs">
      {tabs.map(({ key, label, Icon }) => (
        <div key={key} className="lp-app__tab" data-lp-active={String(key === active)}>
          <Icon size={13} strokeWidth={key === active ? 2.6 : 2} />
          <span>{label}</span>
          {key === active ? <span className="lp-app__tab-dot" /> : null}
        </div>
      ))}
    </div>
  );
}

/** Home screen — mirrors the TDM Insight Home with CTA card + shortcuts. */
export function HomeScreen() {
  return (
    <div className="lp-app">
      <StatusBar />
      <div className="lp-app__body">
        <div className="lp-app__row">
          <div>
            <p className="lp-app__greet-label">Academic prototype</p>
            <p className="lp-app__greet-name">TDM Insight</p>
          </div>
          <span className="lp-app__spacer" />
          <div className="lp-app__avatar" style={{ background: '#1464A8', color: '#fff', fontSize: 8, fontWeight: 700 }}>TDM</div>
        </div>

        {/* Hero CTA card */}
        <div className="lp-app__promo" style={{ background: '#1464A8', flexDirection: 'column', gap: 6, padding: '10px 12px' }}>
          <span className="lp-app__promo-tag" style={{ background: 'rgba(255,255,255,0.18)', color: '#fff', fontSize: 7 }}>NEW CASE</span>
          <p className="lp-app__promo-title" style={{ fontSize: 13, lineHeight: 1.3 }}>Start a new{'\n'}calculation</p>
          <p className="lp-app__promo-sub" style={{ color: 'rgba(255,255,255,0.8)', fontSize: 7.5 }}>Enter a fictional patient case and derive PK parameters.</p>
          <div className="lp-app__cta" style={{ marginTop: 4, fontSize: 8.5, background: '#fff', color: '#1464A8', borderRadius: 50, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 4, padding: '5px 0' }}>
            <ArrowRight size={9} strokeWidth={2.5} />
            Start new case
          </div>
        </div>

        {/* Quick shortcut tiles */}
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 6 }}>
          {[
            { label: 'Concentration\ncurve', Icon: Activity },
            { label: 'Scan lab\nreport', Icon: ClipboardList },
          ].map(({ label, Icon }) => (
            <div key={label} style={{ background: '#F5F5F5', borderRadius: 10, padding: '8px 10px', display: 'flex', flexDirection: 'column', gap: 6 }}>
              <div style={{ background: '#E3F0FF', borderRadius: 8, width: 26, height: 26, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <Icon size={13} strokeWidth={2} color="#1464A8" />
              </div>
              <p style={{ fontSize: 8, fontWeight: 600, lineHeight: 1.35, color: '#0F0F0F', whiteSpace: 'pre-line' }}>{label}</p>
            </div>
          ))}
        </div>

        <div className="lp-app__section-title">
          <span>RECENT CALCULATIONS</span>
          <span className="lp-app__link">See all</span>
        </div>

        {/* Empty state */}
        <div style={{ border: '1px solid #E2E2E2', borderRadius: 12, padding: '14px', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 6, textAlign: 'center' }}>
          <div style={{ background: '#EEEEEE', borderRadius: '50%', padding: 10 }}>
            <History size={14} strokeWidth={1.8} color="#BCBCBC" />
          </div>
          <p style={{ fontSize: 9, fontWeight: 600, color: '#0F0F0F' }}>No calculations yet</p>
          <p style={{ fontSize: 7.5, color: '#6B6B6B', lineHeight: 1.4 }}>Cases you calculate appear here. Everything stays on this device.</p>
        </div>
      </div>
      <TabBar active="home" />
    </div>
  );
}

/** Patient input screen — Step 1/5, patient details form. */
export function CheckoutScreen() {
  return (
    <div className="lp-app">
      <StatusBar />
      <div className="lp-app__body">
        {/* Top bar */}
        <div className="lp-app__row" style={{ marginBottom: 6 }}>
          <div style={{ background: '#E3F0FF', borderRadius: 50, padding: '2px 8px', fontSize: 7.5, fontWeight: 600, color: '#1464A8' }}>Step 1 of 5</div>
          <span className="lp-app__spacer" />
          <span style={{ fontSize: 7, color: '#6B6B6B' }}>Patient details</span>
        </div>

        {/* Warning banner */}
        <div style={{ background: '#FFF3E0', borderRadius: 8, padding: '5px 8px', fontSize: 7.5, color: '#9A5F00', marginBottom: 8 }}>
          ⚠ Fictional data only — academic prototype
        </div>

        <p style={{ fontSize: 12, fontWeight: 600, color: '#0F0F0F', marginBottom: 8 }}>Enter patient details</p>

        {[
          { label: 'Case ID (fictional)', value: 'CASE-001' },
          { label: 'Weight (kg)', value: '72' },
          { label: 'Age (yr)', value: '58' },
          { label: 'Serum creatinine (µmol/L)', value: '110' },
        ].map(({ label, value }) => (
          <div key={label} style={{ marginBottom: 7 }}>
            <p style={{ fontSize: 7.5, color: '#6B6B6B', marginBottom: 2 }}>{label}</p>
            <div style={{ border: '1px solid #E2E2E2', borderRadius: 8, padding: '4px 8px', fontSize: 9, color: '#0F0F0F', background: '#fff' }}>{value}</div>
          </div>
        ))}

        {/* Live CrCl chip */}
        <div style={{ background: '#F5F5F5', borderRadius: 10, padding: '7px 10px', display: 'flex', alignItems: 'center', gap: 7 }}>
          <div style={{ background: '#E3F0FF', borderRadius: 8, padding: 5 }}>
            <FlaskConical size={11} strokeWidth={2} color="#1464A8" />
          </div>
          <div>
            <p style={{ fontSize: 7, color: '#6B6B6B' }}>Cockcroft–Gault CrCl</p>
            <p style={{ fontSize: 10, fontWeight: 700, color: '#0F0F0F', fontFamily: 'monospace' }}>61.4 mL/min</p>
          </div>
        </div>

        <div className="lp-app__cta" style={{ marginTop: 10, fontSize: 9 }}>Next →</div>
      </div>
    </div>
  );
}

/** Calculating screen — animated progress with PK steps. */
export function TrackingScreen() {
  const steps = ['Validating inputs', 'Fitting PK model', 'Estimating AUC₂₄', 'Generating verdict'];
  return (
    <div className="lp-app" style={{ background: '#F8F8F8' }}>
      <StatusBar />
      <div className="lp-app__body lp-app__body--flush" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 0, padding: '24px 16px' }}>

        {/* Ring with icon */}
        <div style={{ position: 'relative', width: 72, height: 72, marginBottom: 14 }}>
          <svg viewBox="0 0 72 72" style={{ width: '100%', height: '100%', transform: 'rotate(-135deg)' }}>
            <circle cx="36" cy="36" r="28" fill="none" stroke="#EEEEEE" strokeWidth="6" strokeLinecap="round" strokeDasharray="131 45" />
            <circle cx="36" cy="36" r="28" fill="none" stroke="#1464A8" strokeWidth="6" strokeLinecap="round" strokeDasharray="100 76" />
          </svg>
          <div style={{ position: 'absolute', inset: 0, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <FlaskConical size={20} strokeWidth={2} color="#1464A8" />
          </div>
        </div>

        <p style={{ fontSize: 13, fontWeight: 600, color: '#0F0F0F', marginBottom: 4 }}>Calculating…</p>
        <p style={{ fontSize: 8.5, color: '#6B6B6B', marginBottom: 16 }}>Sawchuk–Zaske one-compartment fit</p>

        {steps.map((s, i) => (
          <div key={s} style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 6, alignSelf: 'stretch' }}>
            <div style={{
              width: 14, height: 14, borderRadius: '50%', flexShrink: 0,
              background: i < 3 ? '#1464A8' : '#E3F0FF',
              border: i < 3 ? 'none' : '2px solid #1464A8',
              display: 'flex', alignItems: 'center', justifyContent: 'center'
            }}>
              {i < 3 ? <Check size={7} strokeWidth={3.5} color="#fff" /> : null}
            </div>
            <p style={{ fontSize: 8, fontFamily: 'monospace', color: i < 3 ? '#0F0F0F' : '#6B6B6B' }}>{s}</p>
          </div>
        ))}
      </div>
    </div>
  );
}

/** Results screen — AUC₂₄ ring + PK parameters grid. */
export function RewardsScreen() {
  const pkParams = [
    { label: 'Half-life (t½)', value: '6.2 h' },
    { label: 'Clearance', value: '3.68 L/h' },
    { label: 'Vd', value: '32.8 L' },
    { label: 'AUC₂₄', value: '492 mg·h/L' },
  ];
  return (
    <div className="lp-app">
      <StatusBar />
      <div className="lp-app__body">
        <p className="lp-app__greet-name">Results</p>
        <p className="lp-app__greet-label" style={{ marginTop: 2, fontFamily: 'monospace', fontSize: 8 }}>CASE-001</p>

        {/* AUC ring card */}
        <div style={{ border: '1px solid #E2E2E2', borderRadius: 12, padding: '12px', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 6, marginTop: 6, background: '#fff' }}>
          <p style={{ fontSize: 7, color: '#6B6B6B', fontWeight: 600, letterSpacing: 0.5 }}>ESTIMATED AUC₂₄</p>
          <div style={{ position: 'relative', width: 72, height: 72 }}>
            <svg viewBox="0 0 72 72" style={{ width: '100%', height: '100%', transform: 'rotate(-135deg)' }}>
              <circle cx="36" cy="36" r="28" fill="none" stroke="#EEEEEE" strokeWidth="6" strokeLinecap="round" strokeDasharray="131 45" />
              <circle cx="36" cy="36" r="28" fill="none" stroke="#1464A8" strokeWidth="6" strokeLinecap="round" strokeDasharray="108 68" />
            </svg>
            <div style={{ position: 'absolute', inset: 0, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
              <p style={{ fontSize: 13, fontWeight: 700, color: '#0F0F0F', fontFamily: 'monospace', lineHeight: 1 }}>492</p>
              <p style={{ fontSize: 7, color: '#6B6B6B' }}>mg·h/L</p>
            </div>
          </div>
          {/* In-target chip */}
          <div style={{ background: '#E3F0FF', borderRadius: 50, padding: '3px 10px', display: 'flex', alignItems: 'center', gap: 4 }}>
            <Check size={8} strokeWidth={3} color="#1464A8" />
            <p style={{ fontSize: 7.5, fontWeight: 600, color: '#1464A8' }}>In target · 400–600 mg·h/L</p>
          </div>
          {/* 3-stat row */}
          <div style={{ display: 'flex', gap: 12, borderTop: '1px solid #E2E2E2', paddingTop: 8, width: '100%', justifyContent: 'space-evenly' }}>
            {[{ l: 'Ke', v: '0.1121', u: 'h⁻¹' }, { l: 't½', v: '6.2', u: 'h' }, { l: 'CL', v: '3.68', u: 'L/h' }].map(({ l, v, u }) => (
              <div key={l} style={{ textAlign: 'center' }}>
                <p style={{ fontSize: 10, fontWeight: 700, color: '#0F0F0F', fontFamily: 'monospace' }}>{v}</p>
                <p style={{ fontSize: 6.5, color: '#6B6B6B' }}>{u}</p>
                <p style={{ fontSize: 6.5, color: '#6B6B6B', marginTop: 1 }}>{l}</p>
              </div>
            ))}
          </div>
        </div>

        {/* PK parameter grid */}
        <p style={{ fontSize: 7, color: '#6B6B6B', fontWeight: 600, letterSpacing: 0.5, marginTop: 8 }}>PHARMACOKINETIC PARAMETERS</p>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 5, marginTop: 4 }}>
          {pkParams.map(({ label, value }) => (
            <div key={label} style={{ border: '1px solid #E2E2E2', borderRadius: 10, padding: '7px 10px', background: '#fff' }}>
              <p style={{ fontSize: 7, color: '#6B6B6B' }}>{label}</p>
              <p style={{ fontSize: 10, fontWeight: 700, color: '#0F0F0F', fontFamily: 'monospace', marginTop: 4 }}>{value}</p>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

/** Explanation screen — 4-phase step-by-step formula walkthrough. */
export function SuccessScreen() {
  const phases = [
    { n: 1, title: 'Input values', body: 'CASE-001 · 72 kg · 58 yr · Male\nSCr 110 µmol/L · Dose 1250 mg q12h' },
    { n: 2, title: 'Ke derivation (Sawchuk–Zaske)', body: 'ke = ln(C_peak/C_trough) / Δt\n   = ln(18.2/8.4) / 6 = 0.1121 h⁻¹' },
    { n: 3, title: 'PK parameters', body: 't½ = 6.2 h  ·  Vd = 32.8 L\nCL = 3.68 L/h  ·  AUC₂₄ = 492 mg·h/L' },
    { n: 4, title: 'Final result', body: 'AUC₂₄ = 492 mg·h/L → In target\nRecommend: 1250 mg every 12 h' },
  ];
  return (
    <div className="lp-app">
      <StatusBar />
      <div className="lp-app__body">
        <p className="lp-app__greet-name" style={{ marginBottom: 10 }}>How was this calculated?</p>
        {phases.map(({ n, title, body }) => (
          <div key={n} style={{ marginBottom: 8 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginBottom: 4 }}>
              <div style={{ width: 16, height: 16, borderRadius: '50%', background: '#1464A8', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                <span style={{ fontSize: 7.5, fontWeight: 700, color: '#fff' }}>{n}</span>
              </div>
              <p style={{ fontSize: 7.5, fontWeight: 600, color: '#1464A8', letterSpacing: 0.3, textTransform: 'uppercase' }}>{title}</p>
            </div>
            <div style={{ border: '1px solid #E2E2E2', borderRadius: 10, padding: '7px 10px', background: '#fff' }}>
              <p style={{ fontSize: 7.5, color: '#6B6B6B', fontFamily: 'monospace', whiteSpace: 'pre-line', lineHeight: 1.6 }}>{body}</p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

/** Admin / dashboard screen — shown inside the laptop frame. */
export function AdminDashboardScreen() {
  const nav = ['Dashboard', 'Cases', 'Patients', 'Medications', 'Reports', 'Settings'];
  const cards = [
    { label: "Cases today", value: '24' },
    { label: 'In-target AUC', value: '79%' },
    { label: 'Active patients', value: '148' },
    { label: 'Avg. calc time', value: '2.1s' },
  ];
  const bars = [42, 58, 36, 74, 62, 88, 70, 95, 64, 80, 52, 76];

  return (
    <div className="lp-dash">
      <aside className="lp-dash__side">
        <div style={{ display: 'flex', alignItems: 'center', gap: 5, marginBottom: 10 }}>
          <span style={{ height: 14, width: 14, borderRadius: 5, background: '#1464A8' }} />
          <span style={{ fontSize: 9, fontWeight: 700 }}>TDM Insight</span>
        </div>
        {nav.map((item, i) => (
          <div key={item} className="lp-dash__nav-item" data-lp-active={String(i === 0)}>
            <span className="lp-dash__nav-dot" />
            <span>{item}</span>
          </div>
        ))}
      </aside>

      <main className="lp-dash__main">
        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          <span style={{ fontSize: 12, fontWeight: 700 }}>Dashboard</span>
          <span style={{ padding: '2px 6px', borderRadius: 100, background: '#E3F0FF', color: '#073A6B', fontSize: 7, fontWeight: 600 }}>
            ● Live
          </span>
        </div>

        <div className="lp-dash__cards">
          {cards.map((c) => (
            <div key={c.label} className="lp-dash__card">
              <p style={{ fontSize: 7, color: '#7c8a84', fontWeight: 500 }}>{c.label}</p>
              <p className="lp-dash__card-value">{c.value}</p>
            </div>
          ))}
        </div>

        <div className="lp-dash__panel">
          <p style={{ fontSize: 8.5, fontWeight: 600 }}>Cases this week</p>
          <div className="lp-dash__chart">
            {bars.map((h, i) => (
              <span
                key={i}
                className="lp-dash__bar"
                style={{ height: `${h}%`, animationDelay: `${i * 55}ms`, background: '#1464A8' }}
              />
            ))}
          </div>
        </div>
      </main>
    </div>
  );
}
