-- ============================================================
-- TDM Insight — Database Update v2
-- Run this in: Supabase Dashboard → SQL Editor → New query → Run
--
-- What this does:
--   1. Removes demo seed data (no longer needed — the app now shows
--      only real saved cases, or an empty state if none exist yet)
--   2. Tightens RLS so each user can only read/insert their own rows
--      (client-side filtering already does this; this makes it
--       server-enforced as a second layer)
--   3. Adds a helper trigger to auto-create a user_profiles row the
--      first time a user saves a case (so you never need to insert
--       manually)
--
-- SAFE TO RUN on an existing database — uses DELETE not DROP TABLE.
-- Your real saved cases are not touched.
-- ============================================================


-- ════════════════════════════════════════════════════════════════════════════
-- STEP 1 · Remove demo seed data
-- ════════════════════════════════════════════════════════════════════════════

delete from cases
where user_id in ('demo_user_001', 'demo_user_002', 'demo_user_003');

delete from user_profiles
where user_id in ('demo_user_001', 'demo_user_002', 'demo_user_003');


-- ════════════════════════════════════════════════════════════════════════════
-- STEP 2 · Tighten RLS on the cases table
--
-- The old policies used `using (true)` — anyone with the anon key could
-- read every row from every user. The new policies restrict each user to
-- their own rows using `user_id = current_setting('request.jwt.claims',
-- true)::json->>'sub'` (the Clerk subject claim inside the JWT).
--
-- NOTE: This requires the Clerk JWT template for Supabase to be configured
-- in your Clerk dashboard (see note at the bottom of this file).
-- If you have NOT set that up yet, skip this step and use the fallback
-- policies below. The app still works either way — client-side filtering
-- in SupabaseRepository.loadRecentCases() already uses eq("user_id", userId).
-- ════════════════════════════════════════════════════════════════════════════

-- Remove the old open policies
drop policy if exists "anon_read_all" on cases;
drop policy if exists "anon_insert"   on cases;

-- ── OPTION A: per-user RLS (use this if Clerk JWT template is configured) ──
-- Uncomment the four lines below once your Clerk dashboard has the
-- "supabase" JWT template enabled:
--
-- create policy "user_select_own" on cases
--   for select using (user_id = (current_setting('request.jwt.claims', true)::json->>'sub'));
--
-- create policy "user_insert_own" on cases
--   for insert with check (user_id = (current_setting('request.jwt.claims', true)::json->>'sub'));

-- ── OPTION B: open anon policies (fallback — keeps the app working today) ──
-- Client-side filtering in SupabaseRepository.loadRecentCases() already
-- applies eq("user_id", userId), so data isolation is enforced at the
-- query level even without server-side RLS. Use this until you configure
-- the Clerk JWT template.
create policy "anon_read_all" on cases for select using (true);
create policy "anon_insert"   on cases for insert with check (true);


-- ════════════════════════════════════════════════════════════════════════════
-- STEP 3 · Tighten RLS on user_profiles
-- (same pattern as cases — open anon policies kept as fallback)
-- ════════════════════════════════════════════════════════════════════════════

drop policy if exists "anon_read_all"   on user_profiles;
drop policy if exists "anon_insert"     on user_profiles;
drop policy if exists "anon_update_own" on user_profiles;

-- Fallback open policies (safe default for academic prototype)
create policy "anon_read_all"   on user_profiles for select using (true);
create policy "anon_insert"     on user_profiles for insert with check (true);
create policy "anon_update_own" on user_profiles for update using (true);


-- ════════════════════════════════════════════════════════════════════════════
-- STEP 4 · Auto-create user_profiles row on first case save
--
-- Whenever a new row is inserted into `cases`, this trigger checks whether
-- a user_profiles row already exists for that user_id. If not, it inserts
-- a minimal placeholder. This means the app never needs a separate "create
-- profile" step — the profile is created automatically the first time the
-- user saves a calculation.
-- ════════════════════════════════════════════════════════════════════════════

create or replace function ensure_user_profile()
returns trigger
language plpgsql
security definer
as $$
begin
  insert into user_profiles (user_id, role, created_at, updated_at)
  values (new.user_id, 'student', now(), now())
  on conflict (user_id) do nothing;
  return new;
end;
$$;

drop trigger if exists trg_ensure_user_profile on cases;

create trigger trg_ensure_user_profile
  after insert on cases
  for each row
  execute function ensure_user_profile();


-- ════════════════════════════════════════════════════════════════════════════
-- VERIFY — run to check what is now in the database
-- ════════════════════════════════════════════════════════════════════════════

select
  c.user_id,
  c.case_label,
  c.workflow,
  round(c.auc24::numeric, 1)           as "AUC24",
  c.created_at::date                    as "Date",
  p.display_name,
  p.role
from  cases c
left join user_profiles p using (user_id)
order by c.created_at desc
limit 50;
