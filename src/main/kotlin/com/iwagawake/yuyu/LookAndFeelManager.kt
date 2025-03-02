package com.iwagawake.yuyu

import com.iwagawake.yuyu.icons.YUYUIcons
import com.iwagawake.yuyu.services.PluginService
import javax.swing.UIManager

object LookAndFeelManager {
    fun installAllComponents() {
        installIcons()
    }

    private fun installIcons() {
        if (PluginService.isIconInstalled()) {
            return
        }

        this.setTreeIcons()
    }

    private fun setTreeIcons() {
        val defaults = UIManager.getLookAndFeelDefaults()
        defaults[YUYUIcons.Tree.COLLAPSED_KEY] = YUYUIcons.Tree.COLLAPSED
        defaults[YUYUIcons.Tree.EXPANDED_KEY] = YUYUIcons.Tree.EXPANDED
    }
}