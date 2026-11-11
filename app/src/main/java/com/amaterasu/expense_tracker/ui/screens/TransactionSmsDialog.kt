package com.amaterasu.expense_tracker.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amaterasu.expense_tracker.data.entity.TransactionEntity
import com.amaterasu.expense_tracker.sms.SmsBodyLookupResult
import utils.Utils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionSmsDialog(
    transaction: TransactionEntity,
    onDismiss: () -> Unit,
    loadOriginalSms: suspend (Long?) -> SmsBodyLookupResult
) {
    var lookupResult by remember(transaction.id) {
        mutableStateOf<SmsBodyLookupResult>(SmsBodyLookupResult.Loading)
    }

    LaunchedEffect(transaction.id, transaction.smsId) {
        lookupResult = loadOriginalSms(transaction.smsId)
    }

    val smsText = when (val result = lookupResult) {
        SmsBodyLookupResult.Loading -> "Loading original text…"
        is SmsBodyLookupResult.Found -> result.body
        SmsBodyLookupResult.NotFound -> "No original text found."
        SmsBodyLookupResult.PermissionDenied ->
            "SMS permission is unavailable, so the original text cannot be opened."
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = "Original SMS",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${transaction.sourceBank} • ${Utils.formatTimeStamp(transaction.smsReceivedTimestamp)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            SelectionContainer {
                Text(
                    text = smsText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
