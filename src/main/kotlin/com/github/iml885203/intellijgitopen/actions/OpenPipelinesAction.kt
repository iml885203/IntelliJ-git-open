package com.github.iml885203.intellijgitopen.actions

import com.github.iml885203.intellijgitopen.GitHelper
import com.github.iml885203.intellijgitopen.MyNotifier
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class OpenPipelinesAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val projectPath = project.basePath ?: return
        if (!GitHelper.isGitRepository(projectPath)) {
            MyNotifier.notifyError(project, "The current project is not a Git repository.")
            return
        }

        val remoteUrl = GitHelper.getRemoteUrl(projectPath)

        val url = when {
            remoteUrl.contains("github.com") -> "$remoteUrl/actions"
            else -> "$remoteUrl/pipelines"
        }

        BrowserUtil.browse(url)
    }
}