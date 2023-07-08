package edu.gvsu.art.gallery

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
        style = MaterialTheme.typography.h3,
        textAlign = textAlign,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
    )
}
