package com.iwagawake.yuyu.icons

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object YUYUIcons {
    object Tree {
        const val COLLAPSED_KEY = "Tree.collapsedIcon"
        const val EXPANDED_KEY = "Tree.expandedIcon"

        val COLLAPSED = load("icons/tree/collapsed.svg")
        val EXPANDED = load("icons/tree/expanded.svg")
    }

    private fun load(path: String): Icon {
        return IconLoader.getIcon(path, YUYUIcons::class.java)
    }
}