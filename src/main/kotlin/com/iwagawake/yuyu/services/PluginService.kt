package com.iwagawake.yuyu.services

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId

const val YUYU_ICONS_PLUGIN_ID = "com.iwagawake.yuyu.icons"

object PluginService {
    fun isIconInstalled(): Boolean {
        return PluginManagerCore.isPluginInstalled(
            PluginId.getId(YUYU_ICONS_PLUGIN_ID)
        )
    }
}