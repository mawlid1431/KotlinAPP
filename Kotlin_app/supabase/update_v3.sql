-- ============================================================
-- TDM Insight — Database Update v3
-- Run this in: Supabase Dashboard → SQL Editor → New query → Run
--
-- What this does:
--   1. Adds the Clerk identity columns to user_profiles so the app can
--      store the signed-in user's real email / name / avatar
--   2. Keeps user_id (the Clerk user ID) as the primary key, which is
--      what prevents duplicate rows on repeated logins
--   3. Adds an updated_at trigger so syncs record when they happened
--   4. Adds ON DELETE cleanup support for the "Delete account" flow
--
-- SAFE TO RUN on an existing database:
--   - Uses ADD COLUMN IF NOT EXISTS (no data loss)
--   - Does NOT drop or recreate any table
--   - Existing rows keep their display_name / institution / department / role
-- ============================================================


-- ════════════════════════════════════════════════════════════════════════════
-- STEP 1 · Add Clerk identity columns to user_profiles
--
-- user_id (Clerk user ID, e.g. "user_2abc...") is ALREADY the primary key.
-- That single fact is what guarantees requirement "no duplicate users":
-- an upsert on this table can only ever create one row per Clerk user.
-- ════════════════════════════════════════════════════════════════════════════

alter table user_profiles add column if not exists email      text;
alter table user_profiles add column if not exists first_name text;
alter table user_profiles add column if not exists last_name  text;
alter table user_profiles add column if not exists avatar_url text;

-- Fast lookup by email (e.g. support queries). Not unique: Clerk owns
-- email uniqueness, and making it unique here could reject a legitimate
-- upsert if a user changes their email in Clerk.
create index if not exists user_profiles_email_idx on user_profiles (email);


-- ════════════════════════════════════════════════════════════════════════════
-- STEP 2 · Keep updated_at honest
--
-- The app sends updated_at on every sync, but this trigger guarantees the
-- column is correct even if a row is changed from the SQL editor.
-- ════════════════════════════════════════════════════════════════════════════

create or replace function touch_user_profile_updated_at()
returns trigger
language plpgsql
as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

drop trigger if exists trg_touch_user_profile on user_profiles;

create trigger trg_touch_user_profile
  before update on user_profiles
  for each row
  execute function touch_user_profile_updated_at();


-- ════════════════════════════════════════════════════════════════════════════
-- STEP 3 · RLS for the new columns
--
-- The existing open anon policies already cover select/insert/update on
-- user_profiles, and new columns inherit them. The app additionally needs
-- DELETE (for the "Delete account" flow), which was never granted.
--
-- NOTE ON SCOPE: these policies are permissive because this build talks to
-- PostgREST with the anon key (there is no server-side backend). Every
-- delete the app issues is filtered by `eq("user_id", <signed-in user>)`
-- client-side, so a user can only remove their own rows through the UI.
-- To make that server-enforced, configure the Clerk "supabase" JWT template
-- and swap these for the jwt-scoped policies in update_v2.sql "Option A".
-- ════════════════════════════════════════════════════════════════════════════

drop policy if exists "anon_delete_own" on user_profiles;
create policy "anon_delete_own" on user_profiles for delete using (true);

drop policy if exists "anon_delete_own" on cases;
create policy "anon_delete_own" on cases for delete using (true);


-- ════════════════════════════════════════════════════════════════════════════
-- STEP 4 · Drop the leftover demo profile rows, if they are still present
-- (update_v2.sql already removed these; repeated here so a database that
--  only ever ran schema.sql is also clean)
-- ════════════════════════════════════════════════════════════════════════════

delete from user_profiles
where user_id in ('demo_user_001', 'demo_user_002', 'demo_user_003', 'YOUR_REAL_CLERK_USER_ID');

delete from cases
where user_id in ('demo_user_001', 'demo_user_002', 'demo_user_003', 'YOUR_REAL_CLERK_USER_ID');


-- ════════════════════════════════════════════════════════════════════════════
-- VERIFY
-- ════════════════════════════════════════════════════════════════════════════

select
  user_id,
  email,
  first_name,
  last_name,
  display_name,
  avatar_url is not null as has_avatar,
  role,
  created_at,
  updated_at
from user_profiles
order by updated_at desc;
