package ai.ciel.assistant.tools

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * Camera Vision Manager for "Show CIEL What You See":
 * Real-time hardware camera capture using Camera2 API with on-demand frame sampling,
 * torch controls, front/back lens toggle, and immediate lifecycle release.
 */
class CameraVisionManager(private val context: Context) {

    private val tag = "CielCameraVision"
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val mainHandler = Handler(Looper.getMainLooper())

    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null
    private var currentCameraId: String = "0"
    private var isTorchEnabled = false

    @Volatile
    var isCameraActive = false
        private set

    @Volatile
    var latestCameraBitmap: Bitmap? = null
        private set

    fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    fun startCameraSession(useFrontCamera: Boolean = false, onFrameCaptured: ((Bitmap) -> Unit)? = null) {
        if (!hasCameraPermission()) {
            Log.w(tag, "Camera permission not granted")
            return
        }
        if (isCameraActive) return

        try {
            val cameraIds = cameraManager.cameraIdList
            currentCameraId = cameraIds.firstOrNull { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                val facing = chars.get(CameraCharacteristics.LENS_FACING)
                if (useFrontCamera) facing == CameraCharacteristics.LENS_FACING_FRONT else facing == CameraCharacteristics.LENS_FACING_BACK
            } ?: cameraIds.firstOrNull() ?: "0"

            // Moderate resolution (720x480 or 640x480) for low memory on 4 GB RAM phone
            imageReader = ImageReader.newInstance(640, 480, ImageFormat.JPEG, 2).apply {
                setOnImageAvailableListener({ reader ->
                    try {
                        val image = reader.acquireLatestImage()
                        if (image != null) {
                            val buffer = image.planes[0].buffer
                            val bytes = ByteArray(buffer.remaining())
                            buffer.get(bytes)
                            image.close()

                            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            if (bmp != null) {
                                latestCameraBitmap = bmp
                                onFrameCaptured?.invoke(bmp)
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(tag, "Frame capture error: ${e.message}")
                    }
                }, mainHandler)
            }

            cameraManager.openCamera(currentCameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    isCameraActive = true
                    createCaptureSession()
                }

                override fun onDisconnected(camera: CameraDevice) {
                    stopCameraSession()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    Log.e(tag, "Camera open error code: $error")
                    stopCameraSession()
                }
            }, mainHandler)

        } catch (e: SecurityException) {
            Log.e(tag, "SecurityException opening camera: ${e.message}")
        } catch (e: Exception) {
            Log.e(tag, "Error starting camera: ${e.message}")
        }
    }

    private fun createCaptureSession() {
        val device = cameraDevice ?: return
        val reader = imageReader ?: return

        try {
            @Suppress("DEPRECATION")
            device.createCaptureSession(listOf(reader.surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    captureSession = session
                    triggerSingleCapture()
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Log.e(tag, "Capture session configuration failed")
                }
            }, mainHandler)
        } catch (e: Exception) {
            Log.e(tag, "Create capture session error: ${e.message}")
        }
    }

    fun triggerSingleCapture() {
        val session = captureSession ?: return
        val reader = imageReader ?: return
        val device = cameraDevice ?: return

        try {
            val captureBuilder = device.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {
                addTarget(reader.surface)
                set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                if (isTorchEnabled) {
                    set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH)
                }
            }
            session.capture(captureBuilder.build(), null, mainHandler)
        } catch (e: Exception) {
            Log.w(tag, "Single capture trigger error: ${e.message}")
        }
    }

    fun switchCamera() {
        val currentlyFront = try {
            val chars = cameraManager.getCameraCharacteristics(currentCameraId)
            chars.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT
        } catch (e: Exception) { false }

        stopCameraSession()
        startCameraSession(useFrontCamera = !currentlyFront)
    }

    fun toggleTorch(): Boolean {
        isTorchEnabled = !isTorchEnabled
        if (isCameraActive) {
            triggerSingleCapture()
        }
        return isTorchEnabled
    }

    fun stopCameraSession() {
        isCameraActive = false
        try {
            captureSession?.close()
            captureSession = null
            cameraDevice?.close()
            cameraDevice = null
            imageReader?.close()
            imageReader = null
        } catch (e: Exception) {
            Log.w(tag, "Camera release error: ${e.message}")
        }
    }
}
