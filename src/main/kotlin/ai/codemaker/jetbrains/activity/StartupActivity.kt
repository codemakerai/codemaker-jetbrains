/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.jetbrains.activity

import ai.codemaker.jetbrains.image.Icons
import ai.codemaker.jetbrains.settings.AppSettingsConfigurable
import ai.codemaker.jetbrains.settings.AppSettingsState
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class StartupActivity : ProjectActivity {
    companion object {
        private const val NOTIFICATION_TITLE = "CodeMaker AI"
        private const val NOTIFICATION_MESSAGE = "Configure JetBrains plugin."
    }

    override suspend fun execute(project: Project) {
        val settings = AppSettingsState.instance
        if (settings.apiKey.isNullOrBlank()) {
            val notificationGroup = NotificationGroupManager.getInstance()
                    .getNotificationGroup("CodeMaker AI")
            val notification = notificationGroup.createNotification(
                    NOTIFICATION_TITLE, NOTIFICATION_MESSAGE, NotificationType.INFORMATION)
            notification.icon = Icons.Logo
            notification.addAction(object : AnAction("Configure") {
                override fun actionPerformed(e: AnActionEvent) {
                    ShowSettingsUtil.getInstance().showSettingsDialog(project, AppSettingsConfigurable::class.java);
                }
            })
            notification.notify(project)
        }
    }
}