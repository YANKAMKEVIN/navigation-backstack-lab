package com.kevin.navigationbackstacklab

/**
 * A tiny, pure navigation model: one back stack per bottom tab plus an optional modal layer.
 * Every user action (push, back, tab switch, modal, deep link) is a function from state to state,
 * so the visualizer always shows exactly what Back will do next.
 */
enum class NavTab(val root: String) { Home("Home"), Inbox("Inbox") }

data class NavState(
    val currentTab: NavTab = NavTab.Home,
    val stacks: Map<NavTab, List<String>> = NavTab.entries.associateWith { listOf(it.root) },
    /** Modal destination drawn above the current stack (sheet, dialog); not part of any tab stack. */
    val modal: String? = null
) {
    val currentStack: List<String> get() = stacks.getValue(currentTab)

    /** What the visualizer shows, bottom to top. */
    val visibleStack: List<String> get() = currentStack + listOfNotNull(modal)

    val top: String get() = visibleStack.last()
}

/** True when Back is handled inside the app; false means the system should close it. */
fun NavState.canGoBack(): Boolean =
    modal != null || currentStack.size > 1 || currentTab != NavTab.Home

/**
 * Back, in priority order: dismiss the modal, pop the current tab, then return to the start tab
 * (Android's rule for bottom navigation). At the root of the start tab nothing changes.
 */
fun NavState.back(): NavState = when {
    modal != null -> copy(modal = null)
    currentStack.size > 1 -> copy(stacks = stacks + (currentTab to currentStack.dropLast(1)))
    currentTab != NavTab.Home -> copy(currentTab = NavTab.Home)
    else -> this
}

fun NavState.push(route: String): NavState =
    copy(stacks = stacks + (currentTab to currentStack + route), modal = null)

/** Switching tabs keeps every tab's history; re-selecting the current tab pops it to its root. */
fun NavState.switchTab(tab: NavTab): NavState =
    if (tab == currentTab) copy(stacks = stacks + (tab to listOf(tab.root)), modal = null)
    else copy(currentTab = tab, modal = null)

fun NavState.openModal(route: String): NavState = copy(modal = route)

/**
 * A deep link opens [path] on [tab] with a synthetic back path: the tab root is always kept
 * underneath, so Back walks up through meaningful screens instead of leaving the app.
 */
fun NavState.deepLink(tab: NavTab, path: List<String>): NavState =
    copy(currentTab = tab, stacks = stacks + (tab to listOf(tab.root) + path), modal = null)

/** The screen a "Open next" tap leads to, for the demo. */
fun nextRoute(top: String): String = when {
    top == "Home" -> "Feed"
    top == "Feed" -> "Article/42"
    top.startsWith("Article/") -> "Article/${(top.substringAfter('/').toIntOrNull() ?: 0) + 1}"
    top == "Inbox" -> "Thread/18"
    top.startsWith("Thread/") -> "Receipt"
    else -> "Detail"
}
