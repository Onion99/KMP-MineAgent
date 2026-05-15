package org.onion.agent.ui.navigation

import androidx.compose.runtime.snapshots.SnapshotStateList

class NavActions(private val backstack: SnapshotStateList<Any>) {

    fun navigationTo(destination: Any) {
        backstack.add(destination)
    }

    fun back() {
        if (backstack.size > 1) {
            backstack.removeAt(backstack.size - 1)
        }
    }

    // ------------------------------------------------------------------------
    // 退出当前和进入新页面
    // ------------------------------------------------------------------------
    fun popAndNavigation(destination: Any) {
        backstack.clear()
        backstack.add(destination)
    }
}