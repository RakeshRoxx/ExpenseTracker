package com.amaterasu.expense_tracker.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

const val RUPEEENSE_PRIVACY_POLICY_URL =
    "https://rakeshroxx.github.io/ExpenseTracker/privacy-policy.html"

@Composable
fun SmsAccessDisclosureDialog(
    onAgree: () -> Unit,
    onNotNow: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onNotNow,
        title = {
            Text(
                text = "Use SMS to find transactions",
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column {
                Text(
                    "Rupeeense reads SMS inbox messages on this device to find bank, card, " +
                        "and UPI transactions and build your spending history. It checks the " +
                        "sender, date, and message text on this device."
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "For messages identified as transactions, Rupeeense stores transaction " +
                        "details and the SMS message ID locally, but not the message text. " +
                        "Long-press a transaction to look up its original message. If you " +
                        "deleted it, the original text will not be available. Rupeeense does " +
                        "not upload your SMS content."
                )
                TextButton(
                    onClick = onOpenPrivacyPolicy,
                    modifier = Modifier.padding(start = 0.dp, top = 8.dp)
                ) {
                    Text("Read the privacy policy")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onAgree) {
                Text("Agree and continue")
            }
        },
        dismissButton = {
            TextButton(onClick = onNotNow) {
                Text("Not now")
            }
        }
    )
}

@Composable
fun SmsImportPermissionCard(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Import bank SMS automatically",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Connect SMS to find new transactions. You can still add transactions manually.",
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onClick,
                modifier = Modifier.padding(top = 10.dp)
            ) {
                Text("Set up SMS import")
            }
        }
    }
}
