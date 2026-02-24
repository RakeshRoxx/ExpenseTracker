package com.amaterasu.expense_tracker.utils

import java.text.SimpleDateFormat

fun formatTimeStamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault());
    return sdf.format(timestamp);
}