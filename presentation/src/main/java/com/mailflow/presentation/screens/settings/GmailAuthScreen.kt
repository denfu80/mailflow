package com.mailflow.presentation.screens.settings

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.mailflow.data.remote.gmail.GmailApiClient
import com.mailflow.data.remote.gmail.GmailAuthHelper
import com.mailflow.data.remote.gmail.GmailClient
import com.mailflow.presentation.theme.Spacing
import kotlinx.coroutines.launch

@Composable
fun GmailAuthCard(
    authHelper: GmailAuthHelper,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isSignedIn by remember { mutableStateOf(authHelper.isSignedIn()) }
    var accountEmail by remember { mutableStateOf<String?>(authHelper.getAccountEmail()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                isSignedIn = true
                accountEmail = account.email
                errorMessage = null
            } catch (e: ApiException) {
                errorMessage = "Sign-in failed: ${e.message}"
                isSignedIn = false
            }
        } else {
            errorMessage = "Sign-in cancelled"
        }
    }

    LaunchedEffect(Unit) {
        isSignedIn = authHelper.isSignedIn()
        accountEmail = authHelper.getAccountEmail()
    }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            Text(
                text = "Gmail Account",
                style = MaterialTheme.typography.titleMedium
            )

            if (isSignedIn) {
                Text(
                    text = "Signed in as:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = accountEmail ?: "Unknown",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(Spacing.small))

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            authHelper.signOut()
                            isSignedIn = false
                            accountEmail = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign Out")
                }
            } else {
                Text(
                    text = "Sign in to sync your Gmail emails",
                    style = MaterialTheme.typography.bodyMedium
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.small))

                Button(
                    onClick = {
                        val signInIntent = authHelper.getSignInClient().signInIntent
                        signInLauncher.launch(signInIntent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign In with Google")
                }
            }
        }
    }
}
