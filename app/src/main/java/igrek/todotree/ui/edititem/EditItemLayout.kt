package igrek.todotree.ui.edititem

import android.view.View
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import igrek.todotree.R
import igrek.todotree.compose.AppTheme
import igrek.todotree.compose.md_theme_dark_surfaceVariant
import igrek.todotree.compose.md_theme_light_primaryContainer
import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.domain.treeitem.RootTreeItem
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.appFactory
import igrek.todotree.service.tree.TreeManager
import igrek.todotree.ui.GUI
import igrek.todotree.util.mainScope
import kotlinx.coroutines.launch

class EditItemLayout {
    private val treeManager: TreeManager by LazyExtractor(appFactory.treeManager)
    private val gui: GUI by LazyExtractor(appFactory.gui)

    private var currentItem: AbstractTreeItem? = null
    private var parent: AbstractTreeItem = RootTreeItem()

    val state = EditItemState()

    fun setCurrentItem(currentItem: AbstractTreeItem?, parent: AbstractTreeItem) {
        this.currentItem = currentItem
        this.parent = parent
    }

    fun showLayout(layout: View) {
        updateState()

        val thisLayout = this
        layout.findViewById<ComposeView>(R.id.compose_view_edit).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                AppTheme {
                    MainComponent(thisLayout)
                }
            }
        }
    }

    private fun updateState() {

    }

}


class EditItemState {
    val content: MutableState<String> = mutableStateOf("")
}


@Composable
private fun MainComponent(controller: EditItemLayout) {
    Column {
        OutlinedTextField(
            value = controller.state.content.value,
            onValueChange = { controller.state.content.value = it },
            label = null,
            singleLine = false,
            modifier = Modifier.padding(vertical = 1.dp).fillMaxWidth(),
        )

        Row (Modifier.padding(vertical = 0.dp)) {
            FlatButton(
                modifier = Modifier.weight(0.5f),
                text = "Save",
                iconVector = Icons.Filled.Done,
                onClick = {},
            )

            Spacer(Modifier.size(2.dp))

            FlatButton(
                modifier = Modifier.weight(0.25f),
                text = "\uD83D\uDCBE & add",
                onClick = {},
            )

            Spacer(Modifier.size(2.dp))

            FlatButton(
                modifier = Modifier.weight(0.25f),
                text = "\uD83D\uDCBE & enter",
                onClick = {},
            )
        }

        Row {
            IconButton(
                modifier = Modifier
                    .size(36.dp)
                    .padding(4.dp)
                    .align(Alignment.CenterVertically),
                onClick = {
                    mainScope.launch {

                    }
                }
            ) {
                Icon(
                    painterResource(id = R.drawable.arrow_forward),
                    contentDescription = null,
                    tint = Color.White,
                )
            }
        }
    }
}

@Composable
private fun FlatButton(
    modifier: Modifier,
    text: String,
    iconVector: ImageVector? = null,
    onClick: () -> Unit,
) {
    Button(
        onClick = {
            mainScope.launch {
                onClick()
            }
        },
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = md_theme_dark_surfaceVariant, contentColor = Color.White),
    ) {
        if (iconVector != null) {
            Icon(
                iconVector,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
                tint = md_theme_light_primaryContainer,
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        }
        Text(text)
    }
}
