package com.stripe.android.uicore.elements

import androidx.annotation.RestrictTo
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.stripe.android.core.model.CountryUtils
import com.stripe.android.uicore.R
import com.stripe.android.uicore.stripeColors
import androidx.compose.material.TextField as ComposeTextField

@Preview
@Composable
private fun DropDownPreview() {
    Dropdown(
        controller = DropdownFieldController(
            CountryConfig(tinyMode = true)
        ),
        enabled = true
    )
}

/**
 * This composable will handle the display of dropdown items
 * in a lazy column.
 *
 * Here are some relevant manual tests:
 *   - Short list of dropdown items
 *   - long list of dropdown items
 *   - Varying width of dropdown item
 *   - Display setting very large
 *   - Whole row is clickable, not just text
 *   - Scrolls to the selected item in the list
 */
@Composable
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun Dropdown(
    controller: DropdownFieldController,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val label by controller.label.collectAsState(null)
    val selectedIndex by controller.selectedIndex.collectAsState(0)
    val items = controller.displayItems
    val shouldDisableDropdownWithSingleItem =
        items.count() == 1 && controller.disableDropdownWithSingleElement

    val shouldEnable = enabled && !shouldDisableDropdownWithSingleItem

    var expanded by remember { mutableStateOf(false) }
    val selectedItemLabel = controller.getSelectedItemLabel(selectedIndex)
    val interactionSource = remember { MutableInteractionSource() }
    val currentTextColor = if (shouldEnable) {
        MaterialTheme.stripeColors.onComponent
    } else {
        TextFieldDefaults
            .textFieldColors()
            .indicatorColor(enabled = false, isError = false, interactionSource = interactionSource)
            .value
    }

    val inputModeManager = LocalInputModeManager.current

    Box(
        modifier = modifier
            .wrapContentSize(Alignment.TopStart)
            .background(MaterialTheme.stripeColors.component)
    ) {
        // Click handling happens on the box, so that it is a single accessible item
        Box(
            modifier = Modifier
                .focusProperties {
                    canFocus = inputModeManager.inputMode != InputMode.Touch
                }
                .clickable(
                    enabled = shouldEnable,
                    onClickLabel = stringResource(R.string.stripe_change)
                ) {
                    expanded = true
                }
        ) {
            if (controller.tinyMode) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        selectedItemLabel,
                        color = currentTextColor
                    )
                    if (!shouldDisableDropdownWithSingleItem) {
                        Icon(
                            painter = painterResource(id = R.drawable.stripe_ic_chevron_down),
                            contentDescription = null,
                            modifier = Modifier.height(24.dp),
                            tint = MaterialTheme.stripeColors.placeholderText
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(
                            start = 16.dp,
                            top = 4.dp,
                            bottom = 8.dp
                        )
                    ) {
                        label?.let {
                            FormLabel(stringResource(it), enabled = shouldEnable)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(.9f),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                selectedItemLabel,
                                color = currentTextColor
                            )
                        }
                    }
                    if (!shouldDisableDropdownWithSingleItem) {
                        Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                            Icon(
                                painter = painterResource(id = R.drawable.stripe_ic_chevron_down),
                                contentDescription = null,
                                modifier = Modifier.height(24.dp),
                                tint = currentTextColor
                            )
                        }
                    }
                }
            }
        }

        if (expanded) {
            DropdownDialog(
                items = items,
                selectedIndex = selectedIndex,
                showSearch = controller.showSearch,
                currentTextColor = currentTextColor,
                onDismissRequest = { expanded = false },
                onItemSelected = { item ->
                    expanded = false
                    val index = items.indexOf(item)
                    controller.onValueChange(index)
                },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
internal fun DropdownDialog(
    items: List<String>,
    selectedIndex: Int,
    showSearch: Boolean,
    currentTextColor: Color,
    onDismissRequest: () -> Unit,
    onItemSelected: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }

    val filteredItems = remember(query) {
        items.filter { item ->
            val normalized = CountryUtils.normalize(item)
            normalized.contains(query, ignoreCase = true)
        }
    }

    val selectedItem = remember(selectedIndex) {
        items[selectedIndex]
    }

    val hasResults = remember(filteredItems) {
        filteredItems.isNotEmpty()
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(),
    ) {
        Surface(
            elevation = 8.dp,
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colors.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .requiredSizeIn(maxHeight = DropdownMaxHeight),
            ) {
                if (showSearch) {
                    DropdownSearchBar(
                        value = query,
                        onValueChange = { query = it },
                    )
                }

                Crossfade(
                    targetState = hasResults,
                    label = "SearchResultsCrossfade",
                ) { showList ->
                    if (showList) {
                        LazyColumn(modifier = Modifier.fillMaxHeight()) {
                            items(
                                items = filteredItems,
                                key = { it },
                            ) { item ->
                                DropdownMenuItem(
                                    displayValue = item,
                                    isSelected = item == selectedItem,
                                    currentTextColor = currentTextColor,
                                    onClick = { onItemSelected(item) },
                                    modifier = Modifier.animateItemPlacement(),
                                )
                            }
                        }
                    } else {
                        NoResultsPlaceholder()
                    }
                }
            }
        }
    }
}

@Composable
private fun DropdownSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    var didRequestFocus by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!didRequestFocus) {
            focusRequester.requestFocus()
            didRequestFocus = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.surface),
    ) {
        ComposeTextField(
            value = value,
            onValueChange = onValueChange,
            colors = textFieldColors(),
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.height(24.dp),
                    tint = MaterialTheme.colors.onSurface,
                )
            },
            trailingIcon = {
                if (value.isNotEmpty()) {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = null,
                            modifier = Modifier.height(24.dp),
                            tint = MaterialTheme.colors.onSurface,
                        )
                    }
                }
            },
            placeholder = {
                Placeholder(text = stringResource(R.string.stripe_address_search_content_description))
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Search,
            ),
            modifier = Modifier
                .focusRequester(focusRequester)
                .fillMaxWidth(),
        )

        Divider()
    }
}

@Composable
private fun DropdownMenuItem(
    displayValue: String,
    isSelected: Boolean,
    currentTextColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .requiredHeightIn(min = DropdownMenuItemDefaultMinHeight)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = displayValue,
            color = if (isSelected) {
                MaterialTheme.colors.primary
            } else {
                currentTextColor
            },
            fontWeight = if (isSelected) {
                FontWeight.Bold
            } else {
                FontWeight.Normal
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                modifier = Modifier.height(24.dp),
                tint = MaterialTheme.colors.primary
            )
        }
    }
}

@Composable
private fun NoResultsPlaceholder(
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        Text(
            text = "No results", // TODO
            color = MaterialTheme.stripeColors.placeholderText,
            style = MaterialTheme.typography.body1,
        )
    }
}

// Size defaults.
internal val DropdownMenuItemDefaultMinHeight = 48.dp
internal val DropdownMaxHeight = DropdownMenuItemDefaultMinHeight * 8.7f
