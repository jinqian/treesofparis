package fr.arbresdeparis.feature.about

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.arbresdeparis.core.designsystem.theme.TreesTheme

/** Asks once whether to send crash reports; shown over the app until the user answers. */
@Composable
fun CrashReportingQuestionRoute(viewModel: CrashReportingQuestionViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    if (state.visible) CrashReportingQuestion(onAnswer = viewModel::onAnswer)
}

/**
 * Both answers are plain text buttons of the same weight: saying no is as easy as saying yes. The dialog only closes
 * with an answer, so nothing is sent by default.
 */
@Composable
fun CrashReportingQuestion(
    onAnswer: (send: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        title = { Text(stringResource(R.string.crash_question_title)) },
        text = { Text(stringResource(R.string.crash_question_text), style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            TextButton(onClick = { onAnswer(true) }) { Text(stringResource(R.string.crash_question_yes)) }
        },
        dismissButton = {
            TextButton(onClick = { onAnswer(false) }) { Text(stringResource(R.string.crash_question_no)) }
        },
        modifier = modifier,
    )
}

@PreviewLightDark
@Composable
private fun CrashReportingQuestionPreview() {
    TreesTheme { CrashReportingQuestion(onAnswer = {}) }
}
