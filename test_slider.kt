import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Test() {
    Slider(
        value = 0f,
        onValueChange = {},
        thumb = {
            SliderDefaults.Thumb(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                modifier = androidx.compose.ui.Modifier.size(16.dp)
            )
        }
    )
}
