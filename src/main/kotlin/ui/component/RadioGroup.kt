package ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun RadioGroup(
  title: String,
  options: List<String>,
  selectedIndex: Int,
  onSelectedChanged: (index: Int) -> Unit,
  isHorizontal: Boolean = true,
) {
  Text(title)
  if (isHorizontal) {
    Row(
      Modifier.selectableGroup(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      RadioGroupContent(options, selectedIndex, onSelectedChanged)
    }
  } else {
    Column(
      Modifier.selectableGroup(),
      horizontalAlignment = Alignment.Start
    ) {
      RadioGroupContent(options, selectedIndex, onSelectedChanged)
    }
  }
}

@Composable
private fun RadioGroupContent(
  options: List<String>,
  selectedIndex: Int,
  onSelectedChanged: (index: Int) -> Unit,
) {
  options.forEachIndexed { index, s ->
    Row(verticalAlignment = Alignment.CenterVertically) {
      RadioButton(
        selected = index == selectedIndex,
        onClick = {
          onSelectedChanged(index)
        }
      )
      Text(s)
    }
  }
}
