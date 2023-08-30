@file:OptIn(ExperimentalMaterial3Api::class)

package igrek.todotree.ui.edititem

import android.view.View
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.LocalMinimumTouchTargetEnforcement
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
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
    CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
        Column {
            OutlinedTextField(
                value = controller.state.content.value,
                onValueChange = { controller.state.content.value = it },
                label = null,
                singleLine = false,
                modifier = Modifier.padding(vertical = 1.dp).fillMaxWidth(),
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                FlatButton(
                    modifier = Modifier.weight(0.5f),
                    iconVector = Icons.Filled.Done,
                    text = "Save",
                    onClick = {},
                )

                FlatButton(
                    modifier = Modifier.weight(0.25f),
                    iconVector = Icons.Filled.Done,
                    text = "& add",
                    onClick = {},
                )

                FlatButton(
                    modifier = Modifier.weight(0.25f),
                    iconVector = Icons.Filled.Done,
                    text = "& enter",
                    onClick = {},
                )
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                MyFlatIconButton(
                    drawableResId = R.drawable.navigate_previous,
                    onClick = {},
                )
                MyFlatIconButton(
                    drawableResId = R.drawable.arrow_left,
                    onClick = {},
                )
                MyFlatIconButton(
                    drawableResId = R.drawable.select_all,
                    onClick = {},
                )
                MyFlatIconButton(
                    drawableResId = R.drawable.arrow_right,
                    onClick = {},
                )
                MyFlatIconButton(
                    drawableResId = R.drawable.navigate_next,
                    onClick = {},
                )
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                MyFlatIconButton(
                    modifier = Modifier,
                    drawableResId = R.drawable.copy,
                    onClick = {},
                )
                MyFlatIconButton(
                    drawableResId = R.drawable.paste,
                    onClick = {},
                )
                MyFlatIconButton(
                    drawableResId = R.drawable.backspace,
                    onClick = {},
                )
                MyFlatIconButton(
                    drawableResId = R.drawable.backspace_reverse,
                    onClick = {},
                )
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                FlatButton(
                    modifier = Modifier.weight(2f),
                    text = "HH:mm",
                    onClick = {},
                )
                FlatButton(
                    modifier = Modifier.weight(2f),
                    text = "dd.MM.yy",
                    onClick = {},
                )
                FlatButton(
                    modifier = Modifier.weight(1f),
                    text = "-",
                    onClick = {},
                )
                FlatButton(
                    modifier = Modifier.weight(1f),
                    text = ":",
                    onClick = {},
                )
                FlatButton(
                    modifier = Modifier.weight(1.5f),
                    text = "123",
                    onClick = {},
                )
            }

            Row {
                FlatButton(
                    modifier = Modifier.fillMaxWidth(),
                    iconVector = Icons.Filled.Close,
                    text = "Cancel",
                    onClick = {},
                )
            }
        }
    }
}

@Composable
private fun FlatButton(
    text: String,
    modifier: Modifier = Modifier,
    iconVector: ImageVector? = null,
    onClick: () -> Unit,
) {
    Button(
        onClick = {
            mainScope.launch {
                onClick()
            }
        },
        modifier = modifier.padding(vertical = 2.dp).heightIn(min = 40.dp),
        contentPadding = PaddingValues(horizontal = 1.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = md_theme_dark_surfaceVariant,
            contentColor = Color.White
        ),
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

@Composable
private fun (RowScope).MyFlatIconButton(
    @DrawableRes drawableResId: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick = {
            mainScope.launch {
                onClick()
            }
        },
        modifier = modifier.weight(1f).padding(vertical = 2.dp).heightIn(min = 40.dp),
        contentPadding = PaddingValues(horizontal = 1.dp, vertical = 0.dp),
        shape = RoundedCornerShape(20),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = md_theme_dark_surfaceVariant,
            contentColor = Color.White
        ),
    ) {
        Icon(
            painterResource(id = drawableResId),
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = Color.White,
        )
    }
}
