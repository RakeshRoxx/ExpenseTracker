package utils

import java.text.SimpleDateFormat

object Utils {

    fun formatTimeStamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault());
        return sdf.format(timestamp);
    }
}