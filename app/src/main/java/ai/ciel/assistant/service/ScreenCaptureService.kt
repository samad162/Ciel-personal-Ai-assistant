package ai.ciel.assistant.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import ai.ciel.assistant.CielApplication
import ai.ciel.assistant.MainActivity
import ai.ciel.assistant.R

/**
 * Foreground Service for Android MediaProjection Screen Sharing (Gemini Live-style).
 * Captures screen frames efficiently with throttled sampling and low memory footprint.
 */
class ScreenCaptureService : Service() {

    companion object {
        const val ACTION_START = "ai.ciel.action.START_SCREEN_CAPTURE"
        const val ACTION_STOP = "ai.ciel.action.STOP_SCREEN_CAPTURE"
        const val EXTRA_RESULT_CODE = "extra_result_code"
        const val EXTRA_RESULT_DATA = "extra_result_data"
        private const val NOTIFICATION_ID = 2028
        private const val TAG = "CielScreenCapture"

        @Volatile
        var latestScreenBitmap: Bitmap? = null
            private set

        @Volatile
        var isSharingActive: Boolean = false
            private set
    }

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, -1)
                val resultData = intent.getParcelableExtra<Intent>(EXTRA_RESULT_DATA)
                if (resultCode != -1 && resultData != null) {
                    startScreenCapture(resultCode, resultData)
                }
            }
            ACTION_STOP -> stopScreenCapture()
        }
        return START_NOT_STICKY
    }

    private fun startScreenCapture(resultCode: Int, resultData: Intent) {
        val notification = buildCaptureNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        try {
            val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjection = projectionManager.getMediaProjection(resultCode, resultData)

            val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealMetrics(metrics)

            // Scaled down resolution to 540x960 to conserve RAM (target: 4GB device)
            val screenWidth = metrics.widthPixels
            val screenHeight = metrics.heightPixels
            val targetWidth = if (screenWidth > 720) 720 else screenWidth
            val targetHeight = (screenHeight * (targetWidth.toFloat() / screenWidth)).toInt()
            val density = metrics.densityDpi

            imageReader = ImageReader.newInstance(targetWidth, targetHeight, PixelFormat.RGBA_8888, 2).apply {
                setOnImageAvailableListener({ reader ->
                    try {
                        val image = reader.acquireLatestImage()
                        if (image != null) {
                            val planes = image.planes
                            val buffer = planes[0].buffer
                            val pixelStride = planes[0].pixelStride
                            val rowStride = planes[0].rowStride
                            val rowPadding = rowStride - pixelStride * targetWidth

                            val bmp = Bitmap.createBitmap(
                                targetWidth + rowPadding / pixelStride,
                                targetHeight,
                                Bitmap.Config.ARGB_8888
                            )
                            bmp.copyPixelsFromBuffer(buffer)
                            image.close()

                            val cropped = if (rowPadding > 0) {
                                Bitmap.createBitmap(bmp, 0, 0, targetWidth, targetHeight)
                            } else {
                                bmp
                            }

                            // Keep single lightweight reference
                            val old = latestScreenBitmap
                            latestScreenBitmap = cropped
                            old?.recycle()
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Image read error: ${e.message}")
                    }
                }, null)
            }

            virtualDisplay = mediaProjection?.createVirtualDisplay(
                "CielLiveScreen",
                targetWidth,
                targetHeight,
                density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader?.surface,
                null,
                null
            )

            isSharingActive = true
            Log.i(TAG, "Live Screen capture initialized successfully ($targetWidth x $targetHeight)")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start MediaProjection: ${e.message}", e)
            stopScreenCapture()
        }
    }

    private fun stopScreenCapture() {
        isSharingActive = false
        try {
            virtualDisplay?.release()
            virtualDisplay = null
            imageReader?.close()
            imageReader = null
            mediaProjection?.stop()
            mediaProjection = null
        } catch (e: Exception) {
            Log.w(TAG, "Teardown error: ${e.message}")
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        Log.i(TAG, "Screen capture ended and resources released.")
    }

    private fun buildCaptureNotification(): Notification {
        val stopIntent = Intent(this, ScreenCaptureService::class.java).apply {
            action = ACTION_STOP
        }
        val pStopIntent = PendingIntent.getService(
            this, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CielApplication.VOICE_CHANNEL_ID)
            .setContentTitle("CIEL Live Screen Sharing")
            .setContentText("Analyzing screen content with Vision AI")
            .setSmallIcon(R.drawable.ic_ciel_sparkle)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(R.drawable.ic_ciel_sparkle, "Stop Sharing", pStopIntent)
            .build()
    }

    override fun onDestroy() {
        stopScreenCapture()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
