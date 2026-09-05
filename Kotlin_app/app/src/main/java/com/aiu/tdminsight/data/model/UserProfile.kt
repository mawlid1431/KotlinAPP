package com.aiu.tdminsight.data.model

/**
 * The application's record of a signed-in user.
 *
 * [userId] is the Clerk user ID and is the primary key of the Supabase
 * `user_profiles` table. That single fact is what stops repeated logins from
 * creating duplicate rows: an upsert keyed on it can only ever produce one
 * row per Clerk identity.
 *
 * The identity fields come from Clerk and are overwritten on every sync.
 * [institution], [department] and [role] are owned by the app/database and
 * are deliberately NOT part of the sync payload, so a login never wipes
 * details the user or an administrator set.
 */
data class UserProfile(
    val userId: String,
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    // Application-owned, read back from Supabase.
    val institution: String? = null,
    val department: String? = null,
    val role: String? = null,
)
