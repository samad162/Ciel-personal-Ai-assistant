package ai.ciel.assistant

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import ai.ciel.assistant.util.MemoryBudgetManager

class CielApplication : Application() {

    companion object {
        const val VOICE_CHANNEL_ID = "ciel_voice_channel"
        const val OVERLAY_CHANNEL_ID = "ciel_overlay_channel"
        lateinit var instance: CielApplication
            private set
    }

    lateinit var memoryBudgetManager: MemoryBudgetManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 4GB RAM Optimization: Initialize strict memory budget manager
        memoryBudgetManager = MemoryBudgetManager(this)
        registerComponentCallbacks(memoryBudgetManager)

        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val voiceChannel = NotificationChannel(
                VOICE_CHANNEL_ID,
                "CIEL Voice Assistant Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Continuous voice listening and background wake-word detection"
                setShowBadge(false)
            }

            val overlayChannel = NotificationChannel(
                OVERLAY_CHANNEL_ID,
                "CIEL Floating Assistant Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "System-wide floating assistant controls"
                setShowBadge(false)
            }

            notificationManager.createNotificationChannel(voiceChannel)
            notificationManager.createNotificationChannel(overlayChannel)
        }
    }
}
