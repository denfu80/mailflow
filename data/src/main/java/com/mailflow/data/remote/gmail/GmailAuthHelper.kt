package com.mailflow.data.remote.gmail

import android.app.Activity
import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GmailAuthHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gmailApiClient: GmailApiClient,
    private val gmailClient: GmailClient
) {
    private var googleSignInClient: GoogleSignInClient? = null

    fun getSignInClient(): GoogleSignInClient {
        if (googleSignInClient == null) {
            googleSignInClient = GoogleSignIn.getClient(
                context,
                gmailApiClient.getSignInOptions()
            )
        }
        return googleSignInClient!!
    }

    fun isSignedIn(): Boolean {
        return gmailClient.isAuthenticated()
    }

    fun getAccountEmail(): String? {
        return gmailClient.getSignedInAccount()?.email
    }

    suspend fun signOut() {
        getSignInClient().signOut()
        gmailClient.signOut()
    }

    suspend fun revokeAccess() {
        getSignInClient().revokeAccess()
        gmailClient.signOut()
    }
}
