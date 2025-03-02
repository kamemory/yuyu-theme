package com.iwagawake.yuyu

import com.intellij.ide.AppLifecycleListener
import com.intellij.openapi.project.DumbAware

class ApplicationLifecycleListener : AppLifecycleListener, DumbAware {
    override fun appFrameCreated(commandLineArgs: MutableList<String>) {
        TheYUYUTheme.instance.init()
    }

    override fun appClosing() {
    }
}