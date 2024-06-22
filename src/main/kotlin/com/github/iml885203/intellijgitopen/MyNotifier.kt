package com.github.iml885203.intellijgitopen

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

object MyNotifier {
    fun notifyWarn(project: Project, content: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Git Open")
            .createNotification(content, NotificationType.WARNING)
            .notify(project)
    }
}
