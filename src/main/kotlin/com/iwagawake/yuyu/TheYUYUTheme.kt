package com.iwagawake.yuyu

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationActivationListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.wm.IdeFrame

@Service
class TheYUYUTheme : Disposable, Logging {
    private val connection = ApplicationManager.getApplication().messageBus.connect()

    init {
        LookAndFeelManager.installAllComponents()

        connection.subscribe(
            ApplicationActivationListener.TOPIC,
            object : ApplicationActivationListener {
                override fun applicationActivated(ideFrame: IdeFrame) {
                }
            }
        )
    }

    override fun dispose() {
        this.connection.dispose()
    }

    fun init() {
    }

    companion object {
        val instance: TheYUYUTheme
            get() = ApplicationManager.getApplication().getService(TheYUYUTheme::class.java)
    }
}