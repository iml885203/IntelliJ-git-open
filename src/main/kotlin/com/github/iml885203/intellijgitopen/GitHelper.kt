package com.github.iml885203.intellijgitopen

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.RemoteConfig
import org.eclipse.jgit.transport.URIish
import java.io.File

object GitHelper {
    fun isGitRepository(projectPath: String): Boolean {
        return File(projectPath, ".git").exists()
    }

    fun getRemoteUrl(projectPath: String): String {
        val git = Git.open(File(projectPath))
        val config = git.repository.config
        val remoteConfig = RemoteConfig(config, "origin")
        val uris: List<URIish> = remoteConfig.urIs
        val remoteUrl = uris[0].toString().replace(".git", "")
        return convertSshToHttps(remoteUrl)
    }

    private fun convertSshToHttps(url: String): String {
        return if (url.startsWith("git@")) {
            url.replace(":", "/").replace("git@", "http://")
        } else {
            url
        }
    }
}