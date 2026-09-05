/**
 * TDM Insight app screens — rendered as HTML inside CSS device frames.
 * Mirrors the real Kotlin/Compose app: Home, Patient Input, Calculating,
 * Results, and Explanation screens.
 */
import {
  Activity,
  ArrowRight,
  Check,
  ClipboardList,
  FlaskConical,
  History,
  Home,
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

/** Login screen — mirrors the Clerk-powered sign-in screen in TDM Insight. */
export function LoginScreen() {
  return (
    <div className="lp-app" style={{ background: '#f8fafc' }}>
      <StatusBar />
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: '0 20px', gap: 0 }}>
        {/* Logo + wordmark */}
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 8, marginBottom: 28 }}>
          <div style={{ width: 52, height: 52, borderRadius: 14, background: 'linear-gradient(145deg,#1E7AD4,#073A6B)', display: 'flex', alignItems: 'center', justifyContent: 'center', boxShadow: '0 4px 18px rgba(20,100,168,0.35)' }}>
            <FlaskConical size={26} strokeWidth={2} color="#fff" />
          </div>
          <p style={{ margin: 0, fontWeight: 800, fontSize: 16, color: '#0d1a2a', letterSpacing: '-0.01em' }}>TDM Insight</p>
          <p style={{ margin: 0, fontSize: 10, color: '#1464A8', fontWeight: 600, letterSpacing: '0.12em', textTransform: 'uppercase' }}>Sign in to continue</p>
        </div>

        {/* Card */}
        <div style={{ width: '100%', background: '#fff', borderRadius: 16, boxShadow: '0 2px 20px rgba(20,100,168,0.10)', padding: '20px 18px', display: 'flex', flexDirection: 'column', gap: 12 }}>
          {/* Email */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
            <label style={{ fontSize: 10, fontWeight: 600, color: '#374151', letterSpacing: '0.04em', textTransform: 'uppercase' }}>Email address</label>
            <div style={{ height: 36, borderRadius: 8, border: '1.5px solid #e2e8f0', background: '#f8fafc', display: 'flex', alignItems: 'center', paddingLeft: 10, fontSize: 11, color: '#94a3b8' }}>
              student@aiu.edu.my
            </div>
          </div>
          {/* Password */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
            <label style={{ fontSize: 10, fontWeight: 600, color: '#374151', letterSpacing: '0.04em', textTransform: 'uppercase' }}>Password</label>
            <div style={{ height: 36, borderRadius: 8, border: '1.5px solid #e2e8f0', background: '#f8fafc', display: 'flex', alignItems: 'center', paddingLeft: 10, fontSize: 11, color: '#94a3b8' }}>
              ••••••••••
            </div>
          </div>
          {/* Sign in button */}
          <div style={{ height: 38, borderRadius: 10, background: 'linear-gradient(90deg,#1464A8,#1E7AD4)', display: 'flex', alignItems: 'center', justifyContent: 'center', marginTop: 4, gap: 6 }}>
            <span style={{ fontSize: 12, fontWeight: 700, color: '#fff' }}>Continue</span>
            <ArrowRight size={13} strokeWidth={2.5} color="#fff" />
          </div>
          {/* Divider */}
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, margin: '2px 0' }}>
            <div style={{ flex: 1, height: 1, background: '#e2e8f0' }} />
            <span style={{ fontSize: 9, color: '#94a3b8', fontWeight: 500 }}>or</span>
            <div style={{ flex: 1, height: 1, background: '#e2e8f0' }} />
          </div>
          {/* Google SSO (Clerk) */}
          <div style={{ height: 34, borderRadius: 8, border: '1.5px solid #e2e8f0', background: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 7 }}>
            <svg width="13" height="13" viewBox="0 0 24 24"><path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/><path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/><path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l3.66-2.84z"/><path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/></svg>
            <span style={{ fontSize: 11, fontWeight: 600, color: '#374151' }}>Continue with Google</span>
          </div>
        </div>

        {/* Clerk badge */}
        <div style={{ marginTop: 16, display: 'flex', alignItems: 'center', gap: 5 }}>
          <span style={{ fontSize: 9, color: '#94a3b8' }}>Secured by</span>
          <span style={{ fontSize: 9, fontWeight: 700, color: '#6c47ff', letterSpacing: '-0.01em' }}>Clerk</span>
        </div>
      </div>
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


/** History screen — saved calculations, mirrors the app's History tab. */
export function HistoryScreen() {
  const chips = [
    { l: 'ke', v: '0.091', u: 'h⁻¹' },
    { l: 't½', v: '7.6', u: 'h' },
    { l: 'Vd', v: '38', u: 'L' },
    { l: 'CL', v: '3.49', u: 'L/h' },
  ];
  return (
    <div className="lp-app">
      <StatusBar />
      <div className="lp-app__body">
        <p className="lp-app__greet-name">Calculation History</p>
        <p className="lp-app__greet-label" style={{ marginTop: 2 }}>Your saved calculations</p>

        <div style={{ border: '1px solid #E2E2E2', borderRadius: 12, padding: 11, marginTop: 8, background: '#fff' }}>
          <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: 6 }}>
            <div>
              <p style={{ fontSize: 10, fontWeight: 700, color: '#0F0F0F' }}>Vancomycin case</p>
              <p style={{ fontSize: 7, color: '#6B6B6B', marginTop: 2 }}>2026-09-05</p>
            </div>
            <span style={{ background: '#E3F0FF', borderRadius: 50, padding: '3px 8px', fontSize: 6.5, fontWeight: 600, color: '#1464A8', whiteSpace: 'nowrap' }}>
              Pre + Post
            </span>
          </div>

          <div style={{ display: 'flex', alignItems: 'flex-end', justifyContent: 'space-between', marginTop: 9 }}>
            <div>
              <p style={{ fontSize: 6.5, color: '#6B6B6B' }}>AUC₂₄</p>
              <p style={{ fontSize: 13, fontWeight: 700, color: '#1E7A46', fontFamily: 'monospace', lineHeight: 1.1 }}>573 mg·h/L</p>
              <p style={{ fontSize: 6.5, color: '#1E7A46', marginTop: 1 }}>In target</p>
            </div>
            <div style={{ textAlign: 'right' }}>
              <p style={{ fontSize: 6.5, color: '#6B6B6B' }}>Rec. dose</p>
              <p style={{ fontSize: 11, fontWeight: 700, color: '#0F0F0F', fontFamily: 'monospace', lineHeight: 1.1 }}>873 mg</p>
              <p style={{ fontSize: 6.5, color: '#6B6B6B', marginTop: 1 }}>every 12 h</p>
            </div>
          </div>

          <div style={{ display: 'flex', gap: 4, marginTop: 9 }}>
            {chips.map(({ l, v, u }) => (
              <div key={l} style={{ flex: 1, background: '#F4F4F4', borderRadius: 8, padding: '5px 3px', textAlign: 'center' }}>
                <p style={{ fontSize: 6, color: '#6B6B6B' }}>{l}</p>
                <p style={{ fontSize: 8, fontWeight: 700, color: '#0F0F0F', fontFamily: 'monospace', marginTop: 1 }}>{v}</p>
                <p style={{ fontSize: 5.5, color: '#6B6B6B' }}>{u}</p>
              </div>
            ))}
          </div>

          <svg viewBox="0 0 200 54" style={{ width: '100%', height: 42, marginTop: 8, display: 'block' }} aria-hidden>
            <path d="M4 50 L34 8 L196 34" fill="none" stroke="#1464A8" strokeWidth="2" strokeLinejoin="round" strokeLinecap="round" />
            <path d="M4 50 L34 8 L196 34 L196 52 L4 52 Z" fill="#1464A8" opacity="0.07" />
            <line x1="34" y1="8" x2="34" y2="52" stroke="#C9C9C9" strokeWidth="0.8" strokeDasharray="2 2" />
          </svg>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: 5, marginTop: 8, opacity: 0.75 }}>
          <History size={9} strokeWidth={2.2} color="#6B6B6B" />
          <p style={{ fontSize: 7, color: '#6B6B6B' }}>Saved privately to your account</p>
        </div>
      </div>
    </div>
  );
}
