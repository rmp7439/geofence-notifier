package com.geofencenotifier.ui.recipients
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.geofencenotifier.data.db.AppDao

@Composable
fun RecipientsScreen(dao: AppDao) {
    val recs by dao.getRecipients().collectAsState(initial = emptyList())
    LazyColumn {
        items(recs) { rec -> Text("Recipient: ${rec.name} - ${rec.phoneNumber}") }
    }
}