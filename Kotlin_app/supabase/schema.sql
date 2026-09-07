-- ============================================================
-- TDM Insight — Full Database Schema + Seed Data
-- Paste this into: Supabase Dashboard → SQL Editor → New query → Run
--
-- IMPORTANT: After running, find your real Clerk user_id and replace
-- 'demo_user_001' in the seed data section so the History screen shows
-- these rows when you log in.
--
-- How to find your Clerk user_id:
--   1. Sign in to the app on your device
--   2. Open Android Studio → Logcat → filter by "SupabaseRepo"
--   3. A row will print: "Case saved: MDH-001" — the user_id is the
--      value stored in SharedPreferences under "clerk_user_id"
--   OR: Clerk Dashboard → Users → click your account → copy "User ID"
-- ============================================================

create extension if not exists "pgcrypto";

-- ════════════════════════════════════════════════════════════════════════════
-- TABLE 1 · cases
--
-- One row per completed vancomycin TDM calculation.
-- Written automatically by the app when runCalculation() succeeds.
-- Read by HistoryScreen (20 most recent per user, filtered client-side).
-- ════════════════════════════════════════════════════════════════════════════
drop table if exists cases cascade;

create table cases (
  -- Identity
  id                       uuid         primary key default gen_random_uuid(),
  user_id                  text         not null    default 'anonymous',
  created_at               timestamptz  not null    default now(),

  -- Case label & workflow type
  case_label               text         not null,
  workflow                 text         not null,   -- 'PRE' | 'POST' | 'PRE_POST'

  -- Patient demographics
  weight_kg                float8       not null,   -- kg
  age_years                int          not null,
  is_male                  boolean      not null,
  scr_umol_l               float8       not null,   -- serum creatinine µmol/L

  -- Dosing regimen (as prescribed)
  dose_mg                  float8       not null,
  interval_hours           float8       not null,   -- dosing interval h
  infusion_duration_hours  float8       not null,   -- infusion duration h

  -- Measured concentration samples
  -- PRE / PRE_POST: trough sample; null for POST-only workflow
  pre_conc_mg_l            float8,                  -- measured trough mg/L
  pre_time_h               float8,                  -- hours before next dose the sample was taken
  -- POST / PRE_POST: peak sample; null for PRE-only workflow
  post_conc_mg_l           float8,                  -- measured peak mg/L
  post_time_h              float8,                  -- hours after end of infusion

  -- Derived pharmacokinetic parameters
  ke_per_hour              float8,                  -- elimination rate constant h⁻¹
  half_life_hours          float8,                  -- t½ h
  vd_l                     float8,                  -- volume of distribution L (absolute)
  vd_l_per_kg              float8,                  -- Vd L/kg
  clearance_l_per_hour     float8,                  -- vancomycin clearance L/h
  auc24                    float8,                  -- AUC₂₄ mg·h/L  (target: 400–600)
  recommended_dose_mg      float8,                  -- dose to hit AUC₂₄ = 500 mg·h/L

  -- Projected steady-state concentrations
  c_min                    float8,                  -- projected trough mg/L (null: PRE workflow only returns observed trough)
  c_max                    float8,                  -- projected peak mg/L  (null: PRE-only)

  constraint chk_workflow check (workflow in ('PRE', 'POST', 'PRE_POST'))
);

create index cases_user_created_idx on cases (user_id, created_at desc);

alter table cases enable row level security;
-- Allow the Android app (anon key) to read and insert.
-- Row filtering is done client-side in SupabaseRepository.loadRecentCases().
create policy "anon_read_all" on cases for select using (true);
create policy "anon_insert"   on cases for insert with check (true);


-- ════════════════════════════════════════════════════════════════════════════
-- TABLE 2 · user_profiles
--
-- Pharmacist / clinician profile info linked to the Clerk user_id.
-- The app does not yet write to this table automatically; rows can be
-- inserted manually here or via a future Profile screen in the app.
-- Adding this table now lets a future release just INSERT on first login.
-- ════════════════════════════════════════════════════════════════════════════
drop table if exists user_profiles cascade;

create table user_profiles (
  user_id      text         primary key,            -- Clerk user_id  e.g. "user_2abc..."
  display_name text,
  institution  text,
  department   text,
  role         text         default 'student',       -- 'student' | 'pharmacist' | 'doctor'
  created_at   timestamptz  not null default now(),
  updated_at   timestamptz  not null default now()
);

alter table user_profiles enable row level security;
create policy "anon_read_all"   on user_profiles for select using (true);
create policy "anon_insert"     on user_profiles for insert with check (true);
create policy "anon_update_own" on user_profiles for update using (true);


-- ════════════════════════════════════════════════════════════════════════════
-- SEED DATA · user_profiles
-- Replace 'demo_user_001' with your real Clerk user_id.
-- ════════════════════════════════════════════════════════════════════════════
insert into user_profiles (user_id, display_name, institution, department, role) values
  ('demo_user_001', 'Mowlid Haibe',    'AIU Malaysia',          'Pharmacy Practice', 'student'),
  ('demo_user_002', 'Dr. Ahmad Razif', 'Hospital Selayang',     'Clinical Pharmacy', 'pharmacist'),
  ('demo_user_003', 'Nur Hidayah',     'Hospital Kuala Lumpur', 'Pharmacy',          'pharmacist');


-- ════════════════════════════════════════════════════════════════════════════
-- SEED DATA · cases
--
-- 12 realistic vancomycin TDM cases across all three workflows.
-- AUC₂₄ target: 400–600 mg·h/L  (Rybak 2020 guidelines)
-- All rows use 'demo_user_001' so they appear in History after you
-- replace that string with your real Clerk user_id.
-- ════════════════════════════════════════════════════════════════════════════
insert into cases (
  user_id, created_at,
  case_label, workflow,
  weight_kg, age_years, is_male, scr_umol_l,
  dose_mg, interval_hours, infusion_duration_hours,
  pre_conc_mg_l, pre_time_h,
  post_conc_mg_l, post_time_h,
  ke_per_hour, half_life_hours, vd_l, vd_l_per_kg,
  clearance_l_per_hour, auc24, recommended_dose_mg,
  c_min, c_max
) values

-- ── PRE_POST workflow  (Sawchuk-Zaske two-point regression) ─────────────────

-- MDH-001  AUC₂₄ 487 mg·h/L  ✅ IN TARGET — standard adult male
('demo_user_001', now() - interval  '1 day',
 'MDH-001', 'PRE_POST',
  68.0, 62, true,  98.0,
  1000.0, 12.0, 1.0,
  12.5, 10.5,  26.0, 2.0,
  0.0769, 9.01,  34.8, 0.51,  2.68,  487.2,  978.0,  12.5, 26.0),

-- MDH-002  AUC₂₄ 556 mg·h/L  ✅ IN TARGET — older female, mild CKD
('demo_user_001', now() - interval  '4 days',
 'MDH-002', 'PRE_POST',
  58.0, 71, false, 128.0,
  750.0, 12.0, 1.0,
  11.2, 9.5,   19.6, 2.0,
  0.0601, 11.54, 31.2, 0.54,  1.88,  556.4,  846.0,  11.2, 19.6),

-- MDH-003  AUC₂₄ 312 mg·h/L  ⬇ BELOW TARGET — young fast eliminator
('demo_user_001', now() - interval  '7 days',
 'MDH-003', 'PRE_POST',
  85.0, 38, true,  70.0,
  750.0, 8.0,  1.0,
  4.2,   6.0,  22.8, 2.0,
  0.1436, 4.83, 42.5, 0.50,  6.11,  312.4, 1222.0,   4.2, 22.8),

-- MDH-004  AUC₂₄ 648 mg·h/L  ⬆ ABOVE TARGET — severe renal impairment, elderly female
('demo_user_001', now() - interval '11 days',
 'MDH-004', 'PRE_POST',
  60.0, 74, false, 189.0,
  500.0, 12.0, 1.0,
  14.8, 10.0,  21.2, 2.0,
  0.0540, 12.83, 28.6, 0.48, 1.54,  647.8,  462.0,  14.8, 21.2),

-- MDH-005  AUC₂₄ 503 mg·h/L  ✅ IN TARGET — obese patient, extended interval
('demo_user_001', now() - interval '16 days',
 'MDH-005', 'PRE_POST',
  110.0, 55, true, 105.0,
  1500.0, 12.0, 2.0,
  9.8,   9.5,  32.5, 2.5,
  0.1015, 6.82, 44.2, 0.40,  4.49,  503.4, 1344.0,   9.8, 32.5),

-- ── PRE workflow  (population Vd + CrCl → CL) ───────────────────────────────

-- MDH-006  AUC₂₄ 452 mg·h/L  ✅ IN TARGET — standard male, only trough available
('demo_user_001', now() - interval '20 days',
 'MDH-006', 'PRE',
  72.0, 50, true,  88.0,
  1000.0, 12.0, 1.0,
  10.2, 0.5,  null, null,
  0.0938,  7.39, 50.4, 0.70,  4.73,  452.0, 1125.6,  10.2, null),

-- MDH-007  AUC₂₄ 310 mg·h/L  ⬇ BELOW TARGET — trough 5.2 mg/L, dose too low
('demo_user_001', now() - interval '24 days',
 'MDH-007', 'PRE',
  78.0, 40, true,  72.0,
  1000.0, 12.0, 1.0,
  5.2,  0.5,  null, null,
  0.1105,  6.27, 54.6, 0.70,  6.03,  310.0, 1506.0,   5.2, null),

-- MDH-008  AUC₂₄ 784 mg·h/L  ⬆ ABOVE TARGET — elderly female, CKD stage 4
('demo_user_001', now() - interval '29 days',
 'MDH-008', 'PRE',
  55.0, 78, false, 168.0,
  750.0, 24.0, 1.0,
  18.4, 2.0,  null, null,
  0.0398, 17.41, 38.5, 0.70,  1.53,  784.3,  551.0,  18.4, null),

-- ── POST workflow  (Newton–Raphson single peak fit) ──────────────────────────

-- MDH-009  AUC₂₄ 497 mg·h/L  ✅ IN TARGET — only post-dose sample taken
('demo_user_001', now() - interval '34 days',
 'MDH-009', 'POST',
  65.0, 58, true,  98.0,
  1000.0, 12.0, 1.0,
  null, null,  28.4, 2.0,
  0.0915,  7.57, 45.5, 0.70,  4.16,  497.1, 1040.0,   6.3, 28.4),

-- MDH-010  AUC₂₄ 622 mg·h/L  ⬆ ABOVE TARGET — moderately impaired kidneys
('demo_user_001', now() - interval '40 days',
 'MDH-010', 'POST',
  70.0, 65, true, 130.0,
  1000.0, 12.0, 1.0,
  null, null,  24.1, 2.0,
  0.0688,  10.07, 49.0, 0.70, 3.37,  621.9,  1011.0,  7.6, 24.1),

-- ── Cases for demo_user_002  (second pharmacist) ─────────────────────────────

-- ARZ-001  AUC₂₄ 478 mg·h/L  ✅ IN TARGET
('demo_user_002', now() - interval  '2 days',
 'ARZ-001', 'PRE_POST',
  70.0, 45, true,  80.0,
  1000.0, 8.0,  1.0,
  6.2,  5.5,  31.4, 2.0,
  0.1352,  5.12, 35.0, 0.50,  4.73,  478.2, 1136.0,   6.2, 31.4),

-- ARZ-002  AUC₂₄ 391 mg·h/L  ⬇ BELOW TARGET — young high-CL patient
('demo_user_002', now() - interval  '9 days',
 'ARZ-002', 'PRE',
  90.0, 35, true,  65.0,
  1500.0, 8.0,  1.5,
  8.8,  1.0,  null, null,
  0.1215,  5.70, 63.0, 0.70,  7.66,  391.7, 1531.0,   8.8, null);


-- ════════════════════════════════════════════════════════════════════════════
-- VERIFY — run to confirm all rows were inserted correctly
-- ════════════════════════════════════════════════════════════════════════════
select
  user_id,
  case_label,
  workflow,
  round(auc24::numeric,          1)  as "AUC₂₄ (mg·h/L)",
  round(recommended_dose_mg::numeric) as "Rec dose (mg)",
  round(c_min::numeric,          1)  as "Cmin",
  round(c_max::numeric,          1)  as "Cmax",
  round(half_life_hours::numeric, 1) as "t½ (h)",
  created_at::date                   as "Date"
from  cases
order by user_id, created_at desc;
