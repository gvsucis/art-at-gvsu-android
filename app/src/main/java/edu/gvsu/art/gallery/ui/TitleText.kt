package edu.gvsu.art.gallery.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

@Composable
fun TitleText(
    modifier: Modifier = Modifier,
    text: String,
    textAlign: TextAlign = TextAlign.Start,
) {
    Text(
        text,
        style = MaterialTheme.typography.headlineLarge,
        textAlign = textAlign,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
    )
}
