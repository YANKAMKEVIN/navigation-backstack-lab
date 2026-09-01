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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    val stack: List<String>
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
        stack = listOf("Home", "Feed")
    ),
    NavCopy(
        scenario = NavScenario.Detail,
        tab = "Detail",
        title = "Push detail with context",
        body = "A detail route should preserve the list context behind it.",
        rule = "Back should return to the decision surface, not restart the task.",
        accent = Blue,
        stack = listOf("Home", "Feed", "Article/42")
    ),
    NavCopy(
        scenario = NavScenario.Modal,
        tab = "Modal",
        title = "Treat modal as temporary",
        body = "Sheet and dialog destinations should be dismissible without damaging the stack.",
        rule = "A modal route is an interruption layer, not a new home.",
        accent = Gold,
        stack = listOf("Home", "Feed", "Article/42", "ShareSheet")
    ),
    NavCopy(
        scenario = NavScenario.DeepLink,
        tab = "Deep link",
        title = "Rebuild a meaningful path",
        body = "Opening a nested screen from outside should create a back path that makes sense.",
        rule = "Deep links need product context, not only a destination id.",
        accent = Violet,
        stack = listOf("Home", "Inbox", "Thread/18", "Receipt")
    ),
    NavCopy(
        scenario = NavScenario.Back,
        tab = "Back",
        title = "Make back predictable",
        body = "The next back action should be visible enough for designers and engineers to reason about.",
        rule = "Back behavior is part of UX quality, not just platform plumbing.",
        accent = Coral,
        stack = listOf("Home", "Feed", "Article/42")
    )
)

@Composable
private fun NavigationBackstackLab() {
    var selectedIndex by remember { mutableStateOf(3) }
    val selected = scenarios[selectedIndex]

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
                onSelected = { selectedIndex = it },
                modifier = Modifier.padding(start = 20.dp)
            )
        }
        item {
            AnimatedContent(
                targetState = selected,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "nav-scenario"
            ) { scenario ->
                BackstackPanel(
                    copy = scenario,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
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
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        scenarios.forEachIndexed { index, scenario ->
            val selected = selectedIndex == index
            Surface(
                modifier = Modifier.clickable { onSelected(index) },
                shape = RoundedCornerShape(50),
                color = if (selected) scenario.accent else Color.White,
                border = BorderStroke(1.dp, if (selected) scenario.accent else Line)
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
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
private fun BackstackPanel(copy: NavCopy, modifier: Modifier = Modifier) {
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
            RoutePreview(copy)
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
private fun RoutePreview(copy: NavCopy) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(345.dp),
        shape = RoundedCornerShape(22.dp),
        color = Paper,
        border = BorderStroke(1.dp, Line)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PhoneTopBar(copy)
            DestinationCanvas(copy)
            StackViewer(copy)
        }
    }
}

@Composable
private fun PhoneTopBar(copy: NavCopy) {
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
                text = copy.stack.last().take(1),
                color = Color.White,
                fontWeight = FontWeight.Black
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = copy.stack.last(),
                color = Ink,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
            Text(
                text = "${copy.stack.size} routes in stack",
                color = Muted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Surface(
            shape = RoundedCornerShape(50),
            color = Color.White,
            border = BorderStroke(1.dp, Line)
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                text = "Back",
                color = Ink,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun DestinationCanvas(copy: NavCopy) {
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
            color = if (copy.scenario == NavScenario.Modal) copy.accent.copy(alpha = 0.16f) else Color.White
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
                    text = destinationLabel(copy.scenario),
                    color = Ink,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun StackViewer(copy: NavCopy) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Current back stack",
            color = Muted,
            fontWeight = FontWeight.Black,
            fontSize = 13.sp
        )
        copy.stack.forEachIndexed { index, route ->
            StackRow(
                route = route,
                index = index + 1,
                isTop = index == copy.stack.lastIndex,
                accent = copy.accent
            )
        }
    }
}

@Composable
private fun StackRow(route: String, index: Int, isTop: Boolean, accent: Color) {
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
            text = if (isTop) "top" else "kept",
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

private fun destinationLabel(scenario: NavScenario): String =
    when (scenario) {
        NavScenario.Tabs -> "Primary tab"
        NavScenario.Detail -> "Detail destination"
        NavScenario.Modal -> "Modal layer"
        NavScenario.DeepLink -> "External entry"
        NavScenario.Back -> "Next back target"
    }
