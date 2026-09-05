-- ============================================================
-- TDM Insight — Database Update v4
-- Run this in: Supabase Dashboard → SQL Editor → New query → Run
--
-- WHY THIS EXISTS
--   Until now every policy on `cases` and `user_profiles` was
--   `using (true)`. RLS was enabled but permitted everything, so the
--   anon key could read EVERY user's rows. The app looked private
--   because SupabaseRepository filters each query with
--   eq("user_id", userId) — but that is client-side only, and the
--   anon key ships inside the APK where anyone can extract it.
--
--   This script closes that hole: the database itself now decides
--   which rows a caller may see, based on the Clerk user id carried
--   in the request JWT ("sub" claim).
--
-- ⚠ READ THIS BEFORE RUNNING — ORDER MATTERS
--   STEP 1 and STEP 2 are safe to run right now.
--   STEP 3 is the strict lock-down. Do NOT run STEP 3 until the app
--   sends Clerk JWTs to Supabase, or the app will read zero rows and
--   history will look empty for everyone. STEP 3 is commented out for
--   that reason. STEP 4 tells you how to turn it on safely, and
--   STEP 5 is the rollback if anything goes wrong.
--
-- SAFE TO RUN on an existing database:
--   - No table is dropped or recreated
--   - No row is deleted
--   - Steps 1–2 do not change who can read what
-- ============================================================


-- ════════════════════════════════════════════════════════════════════════════
-- STEP 1 · Make user_id trustworthy
--
-- `cases.user_id` currently defaults to 'anonymous'. A row that lands with
-- that default can never belong to anyone, so it would become unreachable
-- the moment per-user policies switch on. Drop the default so a missing
-- user_id fails loudly at insert time instead of silently orphaning data.
-- ════════════════════════════════════════════════════════════════════════════

alter table cases alter column user_id drop default;

-- Index that the per-user policies will lean on for every read.
create index if not exists cases_user_id_idx on cases (user_id);


-- ════════════════════════════════════════════════════════════════════════════
-- STEP 2 · Show what is actually in the table
--
-- Run this and look at the result before going further. Any row listed as
-- 'anonymous' or 'demo_user_001/2/3' is seed or legacy data that no real
-- Clerk user owns — it will disappear from the app once STEP 3 is active.
-- ════════════════════════════════════════════════════════════════════════════

select
  user_id,
  count(*)                       as row_count,
  min(created_at)                as first_row,
  max(created_at)                as latest_row,
  user_id like 'user\_%'         as looks_like_clerk_id
from cases
group by user_id
order by row_count desc;


-- ════════════════════════════════════════════════════════════════════════════
-- STEP 3 · The actual privacy lock  ⚠ COMMENTED OUT ON PURPOSE
--
-- Uncomment and run ONLY after the app authenticates to Supabase with a
-- Clerk JWT. Until then this makes every history screen come back empty.
--
-- How it works: Supabase puts the verified JWT claims in
-- request.jwt.claims. Clerk puts the user id in the "sub" claim. So a row
-- is visible only when its user_id equals the caller's own Clerk id.
-- ════════════════════════════════════════════════════════════════════════════

-- drop policy if exists "anon_read_all"   on cases;
-- drop policy if exists "anon_insert"     on cases;
--
-- create policy "user_select_own" on cases
--   for select using (user_id = (current_setting('request.jwt.claims', true)::json->>'sub'));
--
-- create policy "user_insert_own" on cases
--   for insert with check (user_id = (current_setting('request.jwt.claims', true)::json->>'sub'));
--
-- create policy "user_update_own" on cases
--   for update using (user_id = (current_setting('request.jwt.claims', true)::json->>'sub'));
--
-- create policy "user_delete_own" on cases
--   for delete using (user_id = (current_setting('request.jwt.claims', true)::json->>'sub'));
--
-- drop policy if exists "anon_read_all"   on user_profiles;
-- drop policy if exists "anon_insert"     on user_profiles;
-- drop policy if exists "anon_update_own" on user_profiles;
--
-- create policy "profile_select_own" on user_profiles
--   for select using (user_id = (current_setting('request.jwt.claims', true)::json->>'sub'));
--
-- create policy "profile_insert_own" on user_profiles
--   for insert with check (user_id = (current_setting('request.jwt.claims', true)::json->>'sub'));
--
-- create policy "profile_update_own" on user_profiles
--   for update using (user_id = (current_setting('request.jwt.claims', true)::json->>'sub'));
--
-- create policy "profile_delete_own" on user_profiles
--   for delete using (user_id = (current_setting('request.jwt.claims', true)::json->>'sub'));


-- ════════════════════════════════════════════════════════════════════════════
-- STEP 4 · What has to happen in the app before STEP 3 is safe
--
--   1. Clerk Dashboard → JWT Templates → New template named "supabase",
--      signed with your Supabase JWT secret, with claims:
--        { "role": "authenticated", "sub": "{{user.id}}" }
--      (ClerkAuthManager.refreshSupabaseToken() already requests exactly
--       this template — the plumbing is written, the template is not.)
--
--   2. The Supabase client must send that JWT instead of only the anon
--      key, so request.jwt.claims->>'sub' is populated.
--
--   3. Re-test: sign in as user A, save a case, sign in as user B, and
--      confirm B's history is empty.
--
-- Until all three are done, leave STEP 3 commented and rely on the
-- client-side eq("user_id", userId) filter, understanding that it is a
-- UI-level separation and not a security boundary.
-- ════════════════════════════════════════════════════════════════════════════


-- ════════════════════════════════════════════════════════════════════════════
-- STEP 5 · Rollback — restores the previous open policies
--
-- If STEP 3 was applied too early and the app shows empty history, run
-- this to get back to exactly the v2 behaviour.
-- ════════════════════════════════════════════════════════════════════════════

-- drop policy if exists "user_select_own"    on cases;
-- drop policy if exists "user_insert_own"    on cases;
-- drop policy if exists "user_update_own"    on cases;
-- drop policy if exists "user_delete_own"    on cases;
-- create policy "anon_read_all" on cases for select using (true);
-- create policy "anon_insert"   on cases for insert with check (true);
--
-- drop policy if exists "profile_select_own" on user_profiles;
-- drop policy if exists "profile_insert_own" on user_profiles;
-- drop policy if exists "profile_update_own" on user_profiles;
-- drop policy if exists "profile_delete_own" on user_profiles;
-- create policy "anon_read_all"   on user_profiles for select using (true);
-- create policy "anon_insert"     on user_profiles for insert with check (true);
-- create policy "anon_update_own" on user_profiles for update using (true);


-- ════════════════════════════════════════════════════════════════════════════
-- VERIFY · which policies are live right now
-- ════════════════════════════════════════════════════════════════════════════

select
  tablename,
  policyname,
  cmd,
  qual as using_expression
from pg_policies
where tablename in ('cases', 'user_profiles')
order by tablename, policyname;
