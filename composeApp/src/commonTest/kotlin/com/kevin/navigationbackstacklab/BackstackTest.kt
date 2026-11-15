package com.kevin.navigationbackstacklab

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BackstackTest {

    @Test
    fun startsAtHomeRootWhereBackIsNotHandled() {
        val s = NavState()
        assertEquals(listOf("Home"), s.visibleStack)
        assertFalse(s.canGoBack())
        assertEquals(s, s.back())
    }

    @Test
    fun pushThenBackRestoresPreviousScreen() {
        val s = NavState().push("Feed").push("Article/42")
        assertEquals(listOf("Home", "Feed", "Article/42"), s.visibleStack)
        assertEquals(listOf("Home", "Feed"), s.back().visibleStack)
    }

    @Test
    fun backDismissesModalBeforePoppingTheStack() {
        val s = NavState().push("Feed").openModal("ShareSheet")
        assertEquals("ShareSheet", s.top)
        val afterBack = s.back()
        assertNull(afterBack.modal)
        assertEquals(listOf("Home", "Feed"), afterBack.visibleStack)
    }

    @Test
    fun tabsKeepTheirOwnHistory() {
        val s = NavState().push("Feed").switchTab(NavTab.Inbox).push("Thread/18").switchTab(NavTab.Home)
        assertEquals(listOf("Home", "Feed"), s.visibleStack)
        assertEquals(listOf("Inbox", "Thread/18"), s.switchTab(NavTab.Inbox).visibleStack)
    }

    @Test
    fun reselectingCurrentTabPopsToRoot() {
        val s = NavState().push("Feed").push("Article/42").switchTab(NavTab.Home)
        assertEquals(listOf("Home"), s.visibleStack)
    }

    @Test
    fun backAtRootOfSecondaryTabReturnsToStartTab() {
        val s = NavState().push("Feed").switchTab(NavTab.Inbox)
        assertTrue(s.canGoBack())
        val back = s.back()
        assertEquals(NavTab.Home, back.currentTab)
        assertEquals(listOf("Home", "Feed"), back.visibleStack)
    }

    @Test
    fun deepLinkBuildsASyntheticBackPath() {
        val s = NavState().deepLink(NavTab.Inbox, listOf("Thread/18", "Receipt"))
        assertEquals(listOf("Inbox", "Thread/18", "Receipt"), s.visibleStack)
        val walkedBack = s.back().back().back()
        assertEquals(NavState().copy(stacks = s.stacks + (NavTab.Inbox to listOf("Inbox"))), walkedBack)
        assertFalse(walkedBack.canGoBack())
    }

    @Test
    fun nextRouteFollowsTheDemoGraph() {
        assertEquals("Feed", nextRoute("Home"))
        assertEquals("Article/43", nextRoute("Article/42"))
        assertEquals("Receipt", nextRoute("Thread/18"))
    }
}
