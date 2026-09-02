import {
  ArrowRight,
  ArrowUpRight,
  Activity,
  BookOpen,
  Check,
  ClipboardList,
  Facebook,
  FlaskConical,
  Github,
  Linkedin,
  Lock,
  Play,
  ShieldCheck,
  Star,
  Twitter,
  Users,
  Zap,
} from 'lucide-react';
import { BRAND_LOGO_URL, BRAND_NAME, BRAND_TAGLINE } from '@/lib/brand';
import { LandingNav } from './LandingNav';
import { Odometer } from './Odometer';
import { Reveal } from './Reveal';
import { InteractivePhone, LaptopMockup, PhoneMockup } from './devices';
import {
  AdminDashboardScreen,
  CheckoutScreen,
  HomeScreen,
  RewardsScreen,
  SuccessScreen,
  TrackingScreen,
} from './appScreens';
import { useReveal } from './useReveal';
import { useDocumentTitle } from '@/lib/useDocumentTitle';
import './landing.css';

/* ── Content — all copy describes the real TDM Insight product ─────── */

const HERO_BULLETS = [
  'Enter a fictional patient case in seconds',
  'Derive Ke, Vd, AUC₂₄ and recommended dose',
  'Step-by-step formula walkthrough for every result',
];

const STATS = [
  { value: '3', label: 'Sampling workflows supported' },
  { value: '8', label: 'PK parameters calculated' },
  { value: '4', label: 'Explanation phases per result' },
  { value: '100%', label: 'On-device — no data leaves your phone' },
];

const FEATURES = [
  {
    Icon: FlaskConical,
    title: 'Three sampling workflows',
    body: 'Choose Pre-dose (trough + CrCl), Post-dose (Newton-Raphson fit), or Pre+Post (Sawchuk-Zaske two-point). The app selects the right equations automatically.',
  },
  {
    Icon: Activity,
    title: 'Full PK parameter set',
    body: 'Every result gives you Ke, t½, Vd, Vd/kg, CL, Cmin, Cmax and AUC₂₄ displayed in a fitness-dashboard ring with an in-target verdict.',
  },
  {
    Icon: BookOpen,
    title: 'Step-by-step explanation',
    body: 'Tap "How was this calculated?" for a 4-phase walkthrough: inputs, Ke derivation with real formulas, PK parameters and the final recommendation.',
  },
  {
    Icon: Lock,
    title: 'Completely private',
    body: 'All calculations run on-device. No account required, no data sent to any server. Designed for academic use with fictional patient data only.',
  },
];

const STEPS = [
  {
    step: 'Step 1',
    title: 'Enter patient details',
    body: 'Type a fictional case ID, weight, height, age, sex and serum creatinine. The app computes Cockcroft-Gault CrCl live as you type.',
    points: ['Case ID, weight, height, age', 'Serum creatinine → live CrCl', 'Male / female sex factor'],
    screen: <HomeScreen />,
  },
  {
    step: 'Step 2',
    title: 'Enter dose & samples',
    body: 'Choose Pre, Post or Pre+Post workflow, then add dose, infusion duration, interval and your concentration samples. Validation runs in real time.',
    points: ['Dose, duration and interval (τ)', 'Pre-dose trough or post-dose peak', 'Instant field-level error messages'],
    screen: <CheckoutScreen />,
  },
  {
    step: 'Step 3',
    title: 'Get your results',
    body: 'The engine fits the pharmacokinetic model and returns AUC₂₄, a recommended dose and all PK parameters plus a full formula walkthrough.',
    points: ['AUC₂₄ ring with in-target verdict', 'Recommended dose in mg every τ h', 'Tap to see every formula used'],
    screen: <TrackingScreen />,
  },
];

const WHY = [
  { Icon: Zap, title: 'Instant results', body: 'The PK engine runs in milliseconds — results appear before you put your phone down.' },
  { Icon: ShieldCheck, title: 'Validated equations', body: 'Cockcroft-Gault, Sawchuk-Zaske and Newton-Raphson — standard clinical TDM methods.' },
  { Icon: BookOpen, title: 'Built for learning', body: 'Every formula is shown with the actual numbers substituted, so you see the maths, not just the answer.' },
  { Icon: Users, title: 'Team-friendly', body: 'Share a screenshot of the Explanation screen with supervisors or peers for peer-review.' },
];

const REVIEWS = [
  { title: 'Finally understand TDM', body: 'The explanation screen broke down every step. I actually understand Sawchuk-Zaske now instead of just plugging numbers in.', name: 'Amirah Zakaria', role: 'Pharmacy student, UiTM', tint: '#1464A8' },
  { title: 'Great for case practice', body: 'I use it with fictional patients from past exam papers. It checks my manual maths instantly.', name: 'Daniel Ng', role: 'Clinical pharmacist, KL', tint: '#073A6B' },
  { title: 'The AUC ring is clever', body: 'Seeing the ring go red when the AUC is out of range makes the target band click instantly.', name: 'Nurul Farhana', role: 'PharmD student, UM', tint: '#2a5fa8' },
  { title: 'Three workflows in one app', body: 'I never need separate tools for trough-only vs pre+post anymore. It all works in one place.', name: 'Wei Jun Lim', role: 'Hospital pharmacist, Penang', tint: '#194f8a' },
  { title: 'Step-by-step is the killer feature', body: 'My students use it after seminars to see exactly which formula produced which number. Zero ambiguity.', name: 'Dr Farah Idris', role: 'Lecturer, AIU', tint: '#3a78c9' },
  { title: 'Offline and private', body: 'No login, no cloud — it just works. Perfect for the ward where I cannot use patient data in an app anyway.', name: 'Arif Zulkifli', role: 'Pharmacist, Alor Setar', tint: '#0e5496' },
];

const INSTITUTIONS = [
  { name: 'AIU Pharmacy', detail: 'Academic prototype · Semester 5' },
  { name: 'Android 7+', detail: 'Offline-first · no account needed' },
  { name: 'Open source', detail: 'Kotlin · Jetpack Compose · Material 3' },
];

const MARQUEE_WORDS = ['Calculate', 'Monitor', 'Dose', 'Vancomycin', 'AUC₂₄', 'PK Parameters', 'Sawchuk–Zaske'];

const TEAM = [
  { name: 'Mowlid Haibe',       role: 'Student Developer', photo: '/team/mowlid.jpg' },
  { name: 'Abdinaazir Mustafe', role: 'Student Developer', photo: '/team/abdinaazir.jpg' },
  { name: 'Elham Ahmedngus',    role: 'Student Developer', photo: '/team/elham.jpg' },
];

/* ── Silhouette avatar ──────────────────────────────────────────────── */
function SilhouetteSvg() {
  return (
    <svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg" aria-hidden="true" style={{ width: '100%', height: '100%' }}>
      {/* Head */}
      <circle cx="50" cy="34" r="20" fill="#111827" />
      {/* Shoulders / body */}
      <path d="M10 100 Q10 68 50 68 Q90 68 90 100 Z" fill="#111827" />
    </svg>
  );
}

/* ── Small building blocks ──────────────────────────────────────────── */

function DownloadBadges() {
  return (
    <>
      <a href="#download" className="lp-store">
        <Play size={18} strokeWidth={2} fill="currentColor" />
        <span>
          <span className="lp-store__kicker">Download for Android</span>
          <span className="lp-store__name">Get APK</span>
        </span>
      </a>
      <a href="#download" className="lp-store">
        <Github size={18} strokeWidth={2} />
        <span>
          <span className="lp-store__kicker">Source code</span>
          <span className="lp-store__name">GitHub</span>
        </span>
      </a>
    </>
  );
}

function SectionHead({
  eyebrow,
  title,
  lead,
  align = 'center',
}: {
  eyebrow: string;
  title: string;
  lead?: string;
  align?: 'center' | 'left';
}) {
  return (
    <div className={`lp-head${align === 'left' ? ' lp-head--left' : ''}`}>
      <Reveal kind="fade" className="lp-head__eyebrow">
        <span className="lp-eyebrow lp-eyebrow--plain">{eyebrow}</span>
      </Reveal>
      <Reveal kind="rise" delay={90}>
        <h2 className="lp-h2">{title}</h2>
      </Reveal>
      {lead ? (
        <Reveal kind="rise" delay={180} className="lp-head__lead">
          <p className="lp-lead">{lead}</p>
        </Reveal>
      ) : null}
    </div>
  );
}

/* ── Page ───────────────────────────────────────────────────────────── */

export function LandingPage() {
  useReveal();
  useDocumentTitle(`${BRAND_NAME} — Vancomycin TDM calculator for pharmacy students`);

  return (
    <div className="lp-root" id="top">
      <a href="#main" className="lp-skip">
        Skip to content
      </a>

      <LandingNav />

      <main id="main">
        {/* ── Hero ─────────────────────────────────────────────── */}
        <section className="lp-hero">
          <div className="lp-hero__bg" />
          <div className="lp-hero__grid-overlay" />
          <span className="lp-blob lp-blob--1" />
          <span className="lp-blob lp-blob--2" />

          <div className="lp-hero__inner">
            <div>
              <Reveal kind="fade">
                <span className="lp-eyebrow">
                  <span className="lp-eyebrow-dot" />
                  Academic prototype &middot; fictional data only
                </span>
              </Reveal>

              <Reveal kind="rise" delay={110}>
                <h1 className="lp-h1" style={{ marginTop: '1.5rem' }}>
                  Vancomycin TDM,{' '}
                  <span className="lp-shine">calculated step by step</span>
                </h1>
              </Reveal>

              <Reveal kind="rise" delay={200}>
                <div className="lp-hero__bullets">
                  {HERO_BULLETS.map((text) => (
                    <p key={text} className="lp-hero__bullet">
                      <span className="lp-hero__tick">
                        <Check size={13} strokeWidth={3} />
                      </span>
                      {text}
                    </p>
                  ))}
                </div>
              </Reveal>

              <Reveal kind="rise" delay={290}>
                <div className="lp-hero__actions">
                  <DownloadBadges />
                </div>
              </Reveal>

              <Reveal kind="fade" delay={400}>
                <div className="lp-hero__proof">
                  <div className="lp-avatars">
                    {[
                      { i: 'AZ', c: '#1464A8' },
                      { i: 'DN', c: '#073A6B' },
                      { i: 'NF', c: '#2a5fa8' },
                      { i: 'WJ', c: '#194f8a' },
                    ].map((a) => (
                      <span key={a.i} style={{ background: a.c }}>
                        {a.i}
                      </span>
                    ))}
                  </div>
                  <div>
                    <div style={{ display: 'flex', gap: 2, color: '#d9a521' }}>
                      {Array.from({ length: 5 }, (_, i) => (
                        <Star key={i} size={13} strokeWidth={0} fill="currentColor" />
                      ))}
                    </div>
                    <p className="lp-small" style={{ marginTop: 2 }}>
                      Used by pharmacy students & clinical pharmacists
                    </p>
                  </div>
                </div>
              </Reveal>
            </div>

            {/* Interactive phone — TDM Insight screens */}
            <Reveal kind="device" className="lp-hero__devices">
              <div className="lp-chip lp-chip--tl">
                <FlaskConical size={17} strokeWidth={2.2} color="#1464A8" />
                <span>
                  <span className="lp-chip__label">Workflow</span>
                  <span className="lp-chip__value">Pre + Post</span>
                </span>
              </div>

              <div className="lp-chip lp-chip--bl">
                <Activity size={17} strokeWidth={2.2} color="#1464A8" />
                <span>
                  <span className="lp-chip__label">AUC₂₄</span>
                  <span className="lp-chip__value">492 mg·h/L</span>
                </span>
              </div>

              <div className="lp-chip lp-chip--br">
                <Check size={17} strokeWidth={2.2} color="#1464A8" />
                <span>
                  <span className="lp-chip__label">Target</span>
                  <span className="lp-chip__value">In range</span>
                </span>
              </div>

              <InteractivePhone
                width={310}
                screens={[
                  { key: 'home', label: 'Home', node: <HomeScreen /> },
                  { key: 'input', label: 'Patient inputs', node: <CheckoutScreen /> },
                  { key: 'calc', label: 'Calculating', node: <TrackingScreen /> },
                  { key: 'results', label: 'Results', node: <RewardsScreen /> },
                  { key: 'explain', label: 'Explanation', node: <SuccessScreen /> },
                ]}
              />
            </Reveal>
          </div>
        </section>

        {/* ── Logo marquee divider ─────────────────────────────── */}
        <div className="lp-divider-marquee" aria-hidden>
          <div className="lp-divider-marquee__track">
            {[0, 1].map((copy) => (
              <span key={copy} className="lp-divider-marquee__item">
                {Array.from({ length: 8 }).map((_, i) => (
                  <span key={i} style={{ display: 'inline-flex', alignItems: 'center', gap: '1.5rem' }}>
                    <img src="/favicon.svg" alt="" width={28} height={28} style={{ borderRadius: 8, flexShrink: 0 }} />
                    <span style={{ fontSize: '0.85rem', fontWeight: 700, letterSpacing: '0.06em', color: '#1464A8', textTransform: 'uppercase' }}>TDM Insight</span>
                  </span>
                ))}
              </span>
            ))}
          </div>
        </div>

        {/* ── Stats (odometer) ─────────────────────────────────── */}
        <section className="lp-section lp-section--cream">
          <div className="lp-shell">
            <Reveal kind="rise">
              <h3 className="lp-h3" style={{ textAlign: 'center', maxWidth: 640, margin: '0 auto' }}>
                A complete vancomycin TDM toolkit, running in one app
              </h3>
            </Reveal>
            <Reveal kind="fade" delay={120}>
              <div className="lp-stats">
                {STATS.map((s) => (
                  <Odometer key={s.label} value={s.value} label={s.label} />
                ))}
              </div>
            </Reveal>
          </div>
        </section>

        {/* ── Features ─────────────────────────────────────────── */}
        <section className="lp-section lp-section--white" id="features">
          <div className="lp-shell">
            <SectionHead
              eyebrow="What the app does"
              title={`Everything ${BRAND_NAME} calculates`}
              lead="From patient details to pharmacokinetic parameters — all three sampling workflows, validated equations and a full step-by-step explanation in one place."
            />

            <div className="lp-grid-4">
              {FEATURES.map(({ Icon, title, body }, i) => (
                <Reveal key={title} kind="rise" delay={i * 110}>
                  <article className="lp-card lp-card--hover" style={{ height: '100%' }}>
                    <span className="lp-card__icon">
                      <Icon size={24} strokeWidth={2.1} />
                    </span>
                    <h3 className="lp-h4" style={{ marginTop: '1.35rem' }}>
                      {title}
                    </h3>
                    <p className="lp-body" style={{ marginTop: '0.6rem', fontSize: '0.9375rem' }}>
                      {body}
                    </p>
                    <span className="lp-card__sweep" />
                  </article>
                </Reveal>
              ))}
            </div>
          </div>
        </section>

        {/* ── How it works ─────────────────────────────────────── */}
        <section className="lp-section lp-section--cream" id="how">
          <div className="lp-shell">
            <SectionHead
              eyebrow="How it works"
              title="Three steps to your PK results"
              lead="The full workflow, exactly as it runs in the app today."
            />

            <div className="lp-steps">
              {STEPS.map((s, i) => (
                <div key={s.step} className="lp-step">
                  <div className={`lp-split${i % 2 === 1 ? ' lp-split--reverse' : ''}`}>
                    <Reveal kind={i % 2 === 1 ? 'right' : 'left'}>
                      <div className="lp-step--sticky">
                        <span className="lp-step__num">{s.step}</span>
                        <h3 className="lp-h3" style={{ marginTop: '0.9rem' }}>
                          {s.title}
                        </h3>
                        <p className="lp-lead" style={{ marginTop: '0.9rem' }}>
                          {s.body}
                        </p>
                        <div className="lp-step__list">
                          {s.points.map((p) => (
                            <p key={p} className="lp-step__item">
                              <span className="lp-hero__tick">
                                <Check size={12} strokeWidth={3} />
                              </span>
                              {p}
                            </p>
                          ))}
                        </div>
                        {i === 0 ? (
                          <a
                            href="#download"
                            className="lp-btn lp-btn--dark"
                            style={{ marginTop: '2rem' }}
                          >
                            Download the app
                            <ArrowRight size={17} strokeWidth={2.5} className="lp-btn__icon" />
                          </a>
                        ) : null}
                      </div>
                    </Reveal>

                    <Reveal
                      kind="device"
                      delay={140}
                      style={{ display: 'flex', justifyContent: 'center' }}
                    >
                      <PhoneMockup width={286} float>
                        {s.screen}
                      </PhoneMockup>
                    </Reveal>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* ── Platform: app + dashboard ────────────────────────── */}
        <section className="lp-section lp-section--white" id="platform">
          <div className="lp-shell">
            <SectionHead
              eyebrow="One connected platform"
              title="The app students use, the dashboard educators track"
              lead="Every calculation made in the app can be reviewed on the admin dashboard — case history, PK trends, workflow usage and result distributions, all in sync."
            />

            <Reveal kind="zoom" delay={120} className="lp-cluster">
              <LaptopMockup>
                <AdminDashboardScreen />
              </LaptopMockup>
              <div className="lp-cluster__phone">
                <PhoneMockup width={190} float>
                  <RewardsScreen />
                </PhoneMockup>
              </div>
            </Reveal>

            <div className="lp-grid-4" style={{ marginTop: '4rem' }}>
              {[
                { Icon: ClipboardList, title: 'Case history', body: 'Every calculation saved locally — review past inputs and results at any time.' },
                { Icon: Activity, title: 'AUC trends', body: 'Track how AUC₂₄ estimates change as dosing regimens are adjusted.' },
                { Icon: FlaskConical, title: 'Workflow analytics', body: 'See which sampling strategies are used most and where errors occur.' },
                { Icon: Users, title: 'Student insight', body: 'Understand which steps students find hardest via explanation-screen usage.' },
              ].map(({ Icon, title, body }, i) => (
                <Reveal key={title} kind="rise" delay={i * 100}>
                  <article className="lp-card lp-card--cream lp-card--hover" style={{ height: '100%' }}>
                    <span className="lp-card__icon">
                      <Icon size={22} strokeWidth={2.1} />
                    </span>
                    <h3 className="lp-h4" style={{ marginTop: '1.2rem', fontSize: '1.1875rem' }}>
                      {title}
                    </h3>
                    <p className="lp-body" style={{ marginTop: '0.5rem', fontSize: '0.9375rem' }}>
                      {body}
                    </p>
                  </article>
                </Reveal>
              ))}
            </div>
          </div>
        </section>

        {/* ── Results split ────────────────────────────────────── */}
        <section className="lp-section lp-section--cream" id="results">
          <div className="lp-shell">
            <div className="lp-split">
              <Reveal kind="left">
                <div>
                  <span className="lp-eyebrow lp-eyebrow--plain">Results</span>
                  <h2 className="lp-h2" style={{ marginTop: '1.1rem' }}>
                    Every number, explained
                  </h2>
                  <p className="lp-lead" style={{ marginTop: '1.1rem' }}>
                    Results include an AUC₂₄ ring with an in-target verdict, a recommended dose,
                    and a full PK parameter grid. Tap the explanation button to see every
                    formula with your actual numbers substituted in.
                  </p>

                  <div className="lp-step__list" style={{ marginTop: '1.75rem' }}>
                    {[
                      'AUC₂₄ ring — target 400–600 mg·h/L',
                      'Ke, t½, Vd, CL — full PK grid',
                      'Recommended dose in mg every τ h',
                      'Step-by-step formula walkthrough',
                    ].map((p) => (
                      <p key={p} className="lp-step__item">
                        <span className="lp-hero__tick">
                          <Check size={12} strokeWidth={2.6} />
                        </span>
                        {p}
                      </p>
                    ))}
                  </div>

                  <a href="#download" className="lp-btn lp-btn--primary" style={{ marginTop: '2rem' }}>
                    Try it now
                    <ArrowRight size={17} strokeWidth={2.5} className="lp-btn__icon" />
                  </a>
                </div>
              </Reveal>

              <Reveal kind="device" delay={140} style={{ display: 'flex', justifyContent: 'center' }}>
                <PhoneMockup width={290} float>
                  <RewardsScreen />
                </PhoneMockup>
              </Reveal>
            </div>
          </div>
        </section>

        {/* ── Why choose ──────────────────────────────────────── */}
        <section className="lp-section lp-section--white">
          <div className="lp-shell">
            <SectionHead
              eyebrow={`Why ${BRAND_NAME}`}
              title="Why pharmacists keep using it"
              lead={BRAND_TAGLINE}
            />
            <div className="lp-grid-4">
              {WHY.map(({ Icon, title, body }, i) => (
                <Reveal key={title} kind="zoom" delay={i * 100}>
                  <article className="lp-card lp-card--hover" style={{ height: '100%', textAlign: 'center' }}>
                    <span className="lp-card__icon" style={{ margin: '0 auto' }}>
                      <Icon size={23} strokeWidth={2.1} />
                    </span>
                    <h3 className="lp-h4" style={{ marginTop: '1.25rem', fontSize: '1.1875rem' }}>
                      {title}
                    </h3>
                    <p className="lp-body" style={{ marginTop: '0.5rem', fontSize: '0.9375rem' }}>
                      {body}
                    </p>
                  </article>
                </Reveal>
              ))}
            </div>
          </div>
        </section>

        {/* ── Reviews marquee ─────────────────────────────────── */}
        <section className="lp-section lp-section--cream" id="reviews">
          <div className="lp-shell">
            <SectionHead
              eyebrow="Reviews"
              title="Hear from pharmacy students & clinicians"
              lead="Real feedback from people who practise TDM calculations every week."
            />
          </div>

          <Reveal kind="fade" delay={140}>
            <div className="lp-marquee">
              {[false, true].map((reverse) => (
                <div
                  key={String(reverse)}
                  className={`lp-marquee__track${reverse ? ' lp-marquee__track--reverse' : ''}`}
                  aria-hidden={reverse || undefined}
                >
                  {[0, 1].map((copy) => (
                    <div key={copy} style={{ display: 'flex', gap: '1.25rem' }} aria-hidden={copy === 1}>
                      {(reverse ? [...REVIEWS].reverse() : REVIEWS).map((r) => (
                        <article key={`${copy}-${r.name}`} className="lp-quote">
                          <div className="lp-quote__stars">
                            {Array.from({ length: 5 }, (_, i) => (
                              <Star key={i} size={14} strokeWidth={0} fill="currentColor" />
                            ))}
                          </div>
                          <h3 className="lp-quote__title">{r.title}</h3>
                          <p className="lp-body" style={{ fontSize: '0.9375rem' }}>
                            &ldquo;{r.body}&rdquo;
                          </p>
                          <div className="lp-quote__who">
                            <span className="lp-quote__pic" style={{ background: r.tint }}>
                              {r.name
                                .split(' ')
                                .map((w) => w[0])
                                .slice(0, 2)
                                .join('')}
                            </span>
                            <span>
                              <span style={{ display: 'block', fontWeight: 700, fontSize: '0.9375rem' }}>
                                {r.name}
                              </span>
                              <span className="lp-small">{r.role}</span>
                            </span>
                          </div>
                        </article>
                      ))}
                    </div>
                  ))}
                </div>
              ))}
            </div>
          </Reveal>
        </section>

        {/* ── About / institutions ─────────────────────────────── */}
        <section className="lp-section lp-section--white" id="about">
          <div className="lp-shell">
            <SectionHead
              eyebrow="About"
              title="Built for learning, not clinical use"
              lead="TDM Insight is an academic prototype for pharmacy students practising vancomycin dose calculations with fictional patient data. It runs entirely on-device."
            />
            <div className="lp-grid-2" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))' }}>
              {INSTITUTIONS.map((b, i) => (
                <Reveal key={b.name} kind="rise" delay={i * 110}>
                  <article className="lp-card lp-card--hover" style={{ height: '100%' }}>
                    <span className="lp-card__icon">
                      <FlaskConical size={22} strokeWidth={2.2} />
                    </span>
                    <h3 className="lp-h4" style={{ marginTop: '1.2rem', fontSize: '1.25rem' }}>
                      {b.name}
                    </h3>
                    <p className="lp-body" style={{ marginTop: '0.4rem', fontSize: '0.9375rem' }}>
                      {b.detail}
                    </p>
                    <p
                      className="lp-small"
                      style={{ marginTop: '1.1rem', display: 'flex', alignItems: 'center', gap: '0.35rem', color: '#1464A8', fontWeight: 600 }}
                    >
                      Learn more
                      <ArrowUpRight size={14} strokeWidth={2.5} />
                    </p>
                  </article>
                </Reveal>
              ))}
            </div>
          </div>
        </section>

        {/* ── Team ─────────────────────────────────────────────── */}
        <section className="lp-section lp-section--team" id="team">
          <div className="lp-shell">
            <SectionHead
              eyebrow="Our Team"
              title="The people behind TDM Insight"
              lead="Developed as an academic project at Al-Madinah International University (AIU)."
            />

            {/* Supervisor — centred, full row */}
            <Reveal kind="rise" delay={0}>
              <div className="lp-team-supervisor-row">
                <div className="lp-glass-card lp-glass-card--lg">
                  <div className="lp-glass-card__photo">
                    <img src="/team/supervisor.jpg" alt="Ts. Mohd Zulkifli Mohd Zaki" className="lp-glass-card__img" />
                  </div>
                  <div className="lp-glass-card__overlay">
                    <div className="lp-glass-card__badge">Supervisor</div>
                    <p className="lp-glass-card__name">Ts. Mohd Zulkifli<br />Mohd Zaki</p>
                    <p className="lp-glass-card__role">Lecturer · Faculty of CS &amp; IT</p>
                  </div>
                </div>
              </div>
            </Reveal>

            {/* 3 students in a row */}
            <div className="lp-team-grid">
              {TEAM.map((member, i) => (
                <Reveal key={member.name} kind="rise" delay={(i + 1) * 110}>
                  <div className="lp-glass-card">
                    <div className="lp-glass-card__photo">
                      {member.photo
                        ? <img src={member.photo} alt={member.name} className="lp-glass-card__img" />
                        : <SilhouetteSvg />}
                    </div>
                    <div className="lp-glass-card__overlay">
                      <p className="lp-glass-card__name">{member.name}</p>
                      <p className="lp-glass-card__role">{member.role}</p>
                    </div>
                  </div>
                </Reveal>
              ))}
            </div>
          </div>
        </section>

        {/* ── Download CTA ────────────────────────────────────── */}
        <section className="lp-section lp-section--white" id="download" style={{ paddingTop: 0 }}>
          <div className="lp-shell">
            <Reveal kind="zoom">
              <div className="lp-cta">
                <div className="lp-cta__grid" />
                <span className="lp-eyebrow lp-eyebrow--plain" style={{ background: 'rgba(255,255,255,0.14)', color: '#fff' }}>
                  Free to download
                </span>
                <h2 className="lp-h2" style={{ marginTop: '1.25rem', maxWidth: 680, marginInline: 'auto' }}>
                  Get {BRAND_NAME} and run your first calculation
                </h2>
                <p
                  className="lp-lead"
                  style={{ color: 'rgba(255,255,255,0.78)', marginTop: '1.1rem', maxWidth: 560, marginInline: 'auto' }}
                >
                  Available on Android. No sign-in required. All calculations stay on your device
                  and use fictional patient data only.
                </p>
                <div
                  style={{
                    display: 'flex',
                    flexWrap: 'wrap',
                    gap: '0.75rem',
                    justifyContent: 'center',
                    marginTop: '2.25rem',
                  }}
                >
                  <a href="#download" className="lp-btn lp-btn--light">
                    <Play size={17} strokeWidth={2} fill="currentColor" />
                    Download APK
                  </a>
                  <a href="#download" className="lp-btn lp-btn--light">
                    <Github size={17} strokeWidth={2} />
                    View on GitHub
                  </a>
                </div>
              </div>
            </Reveal>
          </div>
        </section>
      </main>

      {/* ── Footer ────────────────────────────────────────────── */}
      <footer className="lp-footer">
        <div className="lp-shell">
          <div className="lp-footer__grid">
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
                <img src={BRAND_LOGO_URL} alt="" className="lp-brand__mark" width={40} height={40} />
                <span>
                  <span className="lp-brand__name" style={{ color: '#fff' }}>
                    {BRAND_NAME}
                  </span>
                  <span className="lp-brand__tag">{BRAND_TAGLINE}</span>
                </span>
              </div>
              <p style={{ marginTop: '1.25rem', maxWidth: 300, lineHeight: 1.75, fontSize: '0.9375rem' }}>
                An academic prototype for vancomycin therapeutic drug monitoring. All calculations
                run on-device using fictional patient data. Built with Kotlin &amp; Jetpack Compose.
              </p>
              <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1.5rem' }}>
                {[Github, Twitter, Linkedin, Facebook].map((Icon, i) => (
                  <a key={i} href="#top" className="lp-footer__social" aria-label="Social link">
                    <Icon size={16} strokeWidth={2.2} />
                  </a>
                ))}
              </div>
            </div>

            <div>
              <p className="lp-footer__title">App</p>
              <a href="#features" className="lp-footer__link">Features</a>
              <a href="#how" className="lp-footer__link">How it works</a>
              <a href="#results" className="lp-footer__link">Results</a>
              <a href="#download" className="lp-footer__link">Download</a>
            </div>

            <div>
              <p className="lp-footer__title">About</p>
              {INSTITUTIONS.map((b) => (
                <a key={b.name} href="#about" className="lp-footer__link">
                  {b.name}
                </a>
              ))}
            </div>

            <div>
              <p className="lp-footer__title">More</p>
              <a href="#reviews" className="lp-footer__link">Reviews</a>
              <a href="#platform" className="lp-footer__link">Platform</a>
              <a href="#download" className="lp-footer__link">Download</a>
            </div>
          </div>

          <p className="lp-footer__watermark" aria-hidden>
            {BRAND_NAME}
          </p>

          <div className="lp-footer__bar">
            <span>
              &copy; {new Date().getFullYear()} {BRAND_NAME}. Academic prototype &mdash; not for clinical use.
            </span>
            <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem' }}>
              <FlaskConical size={15} strokeWidth={2.3} color="#1464A8" />
              {BRAND_TAGLINE}
            </span>
          </div>
        </div>
      </footer>
    </div>
  );
}
