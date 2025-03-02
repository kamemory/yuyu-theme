package com.iwagawake.yuyu

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class AfterStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
    }
}