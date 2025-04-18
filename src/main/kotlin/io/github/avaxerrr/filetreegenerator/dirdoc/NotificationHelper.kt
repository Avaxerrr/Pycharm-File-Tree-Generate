// version 1.0.2

package io.github.avaxerrr.filetreegenerator.dirdoc

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

/**
 * Helper for showing notifications from the plugin
 */
object NotificationHelper {
    private const val GROUP_ID = "File Tree Generator"

    /**
     * Display an information notification
     */
    fun showInfo(project: Project, content: String, title: String = "Directory Structure Generated") {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(GROUP_ID)
            .createNotification(title, content, NotificationType.INFORMATION)
            .notify(project)
    }

    /**
     * Display an error notification
     */
    fun showError(project: Project, content: String, title: String = "Error") {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(GROUP_ID)
            .createNotification(title, content, NotificationType.ERROR)
            .notify(project)
    }
}
