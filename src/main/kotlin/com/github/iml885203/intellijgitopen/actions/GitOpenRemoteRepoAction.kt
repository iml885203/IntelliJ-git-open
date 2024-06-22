package com.github.iml885203.intellijgitopen.actions

import com.github.iml885203.intellijgitopen.MyNotifier
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.thisLogger
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.transport.RemoteConfig
import org.eclipse.jgit.transport.URIish
import java.io.File

class GitOpenRemoteRepoAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        try {
            val remoteUrl = getRemoteUrl(project.basePath ?: return)
            thisLogger().info("Remote URL: $remoteUrl")
            BrowserUtil.browse(remoteUrl)
        } catch (ex: RepositoryNotFoundException) {
            MyNotifier.notifyError(project, "The Git repository could not be found at the specified location.")
        }
    }

    private fun getRemoteUrl(projectPath: String): String {
        val git = Git.open(File(projectPath))
        val config = git.repository.config
        val remoteConfig = RemoteConfig(config, "origin")
        val uris: List<URIish> = remoteConfig.urIs
        return uris[0].toString()
    }
}