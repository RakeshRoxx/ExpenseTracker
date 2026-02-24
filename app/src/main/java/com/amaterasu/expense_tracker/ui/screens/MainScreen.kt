    package com.amaterasu.expense_tracker.ui.screens

    import android.widget.Toast
    import androidx.compose.foundation.layout.Column
    import androidx.compose.material3.Button
    import androidx.compose.material3.DividerDefaults
    import androidx.compose.material3.HorizontalDivider
    import androidx.compose.material3.Text
    import androidx.compose.runtime.Composable
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.platform.LocalContext
    import androidx.lifecycle.viewmodel.compose.viewModel
    import com.amaterasu.expense_tracker.viewmodel.TransactionViewModel

    @Composable
    fun MainScreen(modifier: Modifier = Modifier) {
        val viewModel : TransactionViewModel = viewModel()
        Column (modifier = modifier) {
            MonthlySummaryScreen()
            DebugTools(viewModel)
            HorizontalDivider(modifier, DividerDefaults.Thickness, DividerDefaults.color)
            TransactionListScreen()
        }
    }

    @Composable
    fun DebugTools(viewModel: TransactionViewModel) {
        val context = LocalContext.current

        Button(onClick = {
            viewModel.exportSmsForTraining { path ->
                Toast.makeText(context, "SMS exported to:\n$path", Toast.LENGTH_LONG).show()
            }
        }) {
            Text("Export SMS for Training")
        }
    }