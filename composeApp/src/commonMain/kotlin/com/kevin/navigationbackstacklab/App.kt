package com.kevin.navigationbackstacklab

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun App() {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Blue,
            secondary = Green,
            background = Paper,
            surface = Color.White
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Paper
        ) {
            NavigationBackstackLab()
        }
    }
}

private enum class NavScenario {
    Tabs,
    Detail,
    Modal,
    DeepLink,
    Back
}

private data class NavCopy(
    val scenario: NavScenario,
    val tab: String,
    val title: String,
    val body: String,
    val rule: String,
    val accent: Color,
    /** Navigation state loaded when the scenario is picked; the user can then push, pop, etc. */
    val preset: NavState
)

private val Paper = Color(0xFFF5F0E8)
private val Ink = Color(0xFF101615)
private val Muted = Color(0xFF687169)
private val Line = Color(0xFFE1D8CA)
private val Blue = Color(0xFF2864A8)
private val Green = Color(0xFF0B6B57)
private val Coral = Color(0xFFC8523A)
private val Violet = Color(0xFF6B57B8)
private val Gold = Color(0xFFB76F16)

private val scenarios = listOf(
    NavCopy(
        scenario = NavScenario.Tabs,
        tab = "Tabs",
        title = "Keep tab history separate",
        body = "Each primary destination owns its own navigation memory.",
        rule = "Switching tabs should not destroy where the user was.",
        accent = Green,
        preset = NavState().push("Feed").switchTab(NavTab.Inbox).push("Thread/18").switchTab(NavTab.Home)
    ),
    NavCopy(
        scenario = NavScenario.Detail,
        tab = "Detail",
        title = "Push detail with context",
        body = "A detail route should preserve the list context behind it.",
        rule = "Back should return to the decision surface, not restart the task.",
        accent = Blue,
        preset = NavState().push("Feed").push("Article/42")
    ),
    NavCopy(
        scenario = NavScenario.Modal,
        tab = "Modal",
        title = "Treat modal as temporary",
        body = "Sheet and dialog destinations should be dismissible without damaging the stack.",
        rule = "A modal route is an interruption layer, not a new home.",
        accent = Gold,
        preset = NavState().push("Feed").push("Article/42").openModal("ShareSheet")
    ),
    NavCopy(
        scenario = NavScenario.DeepLink,
        tab = "Deep link",
        title = "Rebuild a meaningful path",
        body = "Opening a nested screen from outside should create a back path that makes sense.",
        rule = "Deep links need product context, not only a destination id.",
        accent = Violet,
        preset = NavState().deepLink(NavTab.Inbox, listOf("Thread/18", "Receipt"))
    ),
    NavCopy(
        scenario = NavScenario.Back,
        tab = "Back",
        title = "Make back predictable",
        body = "The next back action should be visible enough for designers and engineers to reason about.",
        rule = "Back behavior is part of UX quality, not just platform plumbing.",
        accent = Coral,
        preset = NavState().push("Feed").push("Article/42")
    )
)

@Composable
private fun NavigationBackstackLab() {
    var selectedIndex by rememberSaveable { mutableStateOf(3) }
    var nav by remember { mutableStateOf(scenarios[3].preset) }
    val selected = scenarios[selectedIndex]

    // System Back (Android back gesture / button) drives the same stack as the on-screen Back.
    // Disabled at the root of the start tab so the system can close the app as usual.
    SystemBackHandler(enabled = nav.canGoBack()) { nav = nav.back() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(top = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Header(Modifier.padding(horizontal = 20.dp)) }
        item {
            ScenarioTabs(
                selectedIndex = selectedIndex,
                onSelected = {
                    selectedIndex = it
                    nav = scenarios[it].preset
                },
                modifier = Modifier.padding(start = 20.dp)
            )
        }
        item {
            BackstackPanel(
                copy = selected,
                nav = nav,
                onNav = { nav = it },
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
        item {
            RuleCard(
                copy = selected,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
        item { Spacer(modifier = Modifier.height(2.dp)) }
    }
}

@Composable
private fun Header(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Navigation Backstack Lab",
            color = Ink,
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.headlineLarge
        )
        Text(
            text = "A Compose/KMP lab for visualizing routes, nested flows, modal layers, deep links and Android Back.",
            color = Muted,
            lineHeight = 23.sp,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun ScenarioTabs(
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        scenarios.forEachIndexed { index, scenario ->
            val selected = selectedIndex == index
            Surface(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .selectable(selected = selected, role = Role.Tab, onClick = { onSelected(index) }),
                shape = RoundedCornerShape(50),
                color = if (selected) scenario.accent else Color.White,
                border = BorderStroke(1.dp, if (selected) scenario.accent else Line)
            ) {
                Text(
                    modifier = Modifier
                        .wrapContentHeight()
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    text = scenario.tab,
                    color = if (selected) Color.White else Ink,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    lineHeight = 18.sp
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
    }
}

@Composable
private fun BackstackPanel(
    copy: NavCopy,
    nav: NavState,
    onNav: (NavState) -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RoutePreview(copy, nav, onNav)
            Text(
                text = copy.title,
                color = Ink,
                fontWeight = FontWeight.Black,
                fontSize = 25.sp,
                lineHeight = 30.sp
            )
            Text(
                text = copy.body,
                color = Muted,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun RoutePreview(copy: NavCopy, nav: NavState, onNav: (NavState) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 345.dp),
        shape = RoundedCornerShape(22.dp),
        color = Paper,
        border = BorderStroke(1.dp, Line)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PhoneTopBar(copy, nav, onBack = { onNav(nav.back()) })
            AnimatedContent(
                targetState = nav.top,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "destination"
            ) { _ ->
                DestinationCanvas(copy, nav)
            }
            NavControls(copy, nav, onNav)
            StackViewer(copy, nav)
        }
    }
}

@Composable
private fun PhoneTopBar(copy: NavCopy, nav: NavState, onBack: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(copy.accent),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = nav.top.take(1),
                color = Color.White,
                fontWeight = FontWeight.Black
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = nav.top,
                color = Ink,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
            Text(
                text = "${nav.visibleStack.size} routes · ${nav.currentTab.name} tab",
                color = Muted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        val canGoBack = nav.canGoBack()
        Surface(
            onClick = onBack,
            enabled = canGoBack,
            modifier = Modifier
                .heightIn(min = 48.dp)
                .alpha(if (canGoBack) 1f else 0.4f),
            shape = RoundedCornerShape(50),
            color = Color.White,
            border = BorderStroke(1.dp, Line)
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 15.dp),
                text = "Back",
                color = Ink,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun DestinationCanvas(copy: NavCopy, nav: NavState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(126.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            modifier = Modifier.weight(0.9f),
            shape = RoundedCornerShape(18.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(if (index == 0) 0.75f else 0.58f)
                            .height(if (index == 0) 12.dp else 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (index == 0) copy.accent else Line)
                    )
                }
            }
        }
        Surface(
            modifier = Modifier.weight(1.1f),
            shape = RoundedCornerShape(18.dp),
            color = if (nav.modal != null) copy.accent.copy(alpha = 0.16f) else Color.White
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(copy.accent.copy(alpha = 0.18f))
                )
                Text(
                    text = destinationLabel(nav),
                    color = Ink,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun StackViewer(copy: NavCopy, nav: NavState) {
    val stack = nav.visibleStack
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Current back stack",
            color = Muted,
            fontWeight = FontWeight.Black,
            fontSize = 13.sp
        )
        stack.forEachIndexed { index, route ->
            StackRow(
                route = route,
                index = index + 1,
                isTop = index == stack.lastIndex,
                isModal = nav.modal != null && index == stack.lastIndex,
                accent = copy.accent
            )
        }
        Text(
            text = if (nav.canGoBack()) "Next Back: ${nav.back().top}" else "Next Back: leaves the app",
            color = copy.accent,
            fontWeight = FontWeight.Black,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun NavControls(copy: NavCopy, nav: NavState, onNav: (NavState) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NavTab.entries.forEach { tab ->
                ControlChip(
                    label = tab.name,
                    selected = nav.currentTab == tab,
                    accent = copy.accent,
                    role = Role.Tab,
                    modifier = Modifier.weight(1f)
                ) { onNav(nav.switchTab(tab)) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ControlChip("Open next", selected = false, accent = copy.accent, modifier = Modifier.weight(1f)) {
                onNav(nav.push(nextRoute(nav.currentStack.last())))
            }
            ControlChip("Share", selected = false, accent = copy.accent, modifier = Modifier.weight(1f)) {
                onNav(nav.openModal("ShareSheet"))
            }
            ControlChip("Deep link", selected = false, accent = copy.accent, modifier = Modifier.weight(1f)) {
                onNav(nav.deepLink(NavTab.Inbox, listOf("Thread/18", "Receipt")))
            }
        }
    }
}

@Composable
private fun ControlChip(
    label: String,
    selected: Boolean,
    accent: Color,
    modifier: Modifier = Modifier,
    role: Role = Role.Button,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) accent else Color.White)
            .clickable(role = role, onClick = onClick)
            .semantics { this.selected = selected },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Ink,
            fontWeight = FontWeight.Black,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun StackRow(route: String, index: Int, isTop: Boolean, isModal: Boolean, accent: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(35.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(Color.White)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(if (isTop) accent else Line),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = index.toString(),
                color = if (isTop) Color.White else Muted,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp
            )
        }
        Text(
            text = route,
            color = Ink,
            fontWeight = FontWeight.Black,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = when {
                isModal -> "modal"
                isTop -> "top"
                else -> "kept"
            },
            color = Muted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun RuleCard(copy: NavCopy, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Ink
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(copy.accent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = "Navigation rule",
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
            }
            Text(
                text = copy.rule,
                color = Color(0xFFECE7DC),
                lineHeight = 22.sp
            )
        }
    }
}

private fun destinationLabel(nav: NavState): String =
    when {
        nav.modal != null -> "Modal layer"
        nav.currentStack.size == 1 -> "Primary tab"
        else -> "Detail destination"
    }
