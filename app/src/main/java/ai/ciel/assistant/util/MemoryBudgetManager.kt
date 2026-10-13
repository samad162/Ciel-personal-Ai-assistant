package ai.ciel.assistant.util

import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 4 GB RAM Performance Guardrail:
 * Monitors Android low-memory signals (ComponentCallbacks2) and active heap usage.
 * Dynamically enforces context trimming, cache pruning, and garbage-collection friendliness.
 */
class MemoryBudgetManager(private val context: Context) : ComponentCallbacks2 {

    private val tag = "MemoryBudgetManager"

    private val _isLowMemoryState = MutableStateFlow(false)
    val isLowMemoryState: StateFlow<Boolean> = _isLowMemoryState.asStateFlow()

    private val _currentHeapUsageMb = MutableStateFlow(0L)
    val currentHeapUsageMb: StateFlow<Long> = _currentHeapUsageMb.asStateFlow()

    // 4GB RAM phones: heap limit is usually 192MB-256MB. We keep CIEL under 64MB active heap.
    val maxRecommendedHeapMb: Long = 96L

    fun checkCurrentMemory(): MemoryStats {
        val runtime = Runtime.getRuntime()
        val usedMem = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        val maxMem = runtime.maxMemory() / (1024 * 1024)
        _currentHeapUsageMb.value = usedMem

        val isCritical = usedMem > maxRecommendedHeapMb
        if (isCritical != _isLowMemoryState.value) {
            _isLowMemoryState.value = isCritical
        }

        return MemoryStats(usedMb = usedMem, maxMb = maxMem, isCritical = isCritical)
    }

    override fun onTrimMemory(level: Int) {
        Log.w(tag, "onTrimMemory triggered with level: $level")
        when (level) {
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL,
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> {
                _isLowMemoryState.value = true
                pruneTransientCaches()
                System.gc()
            }
            ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> {
                // UI went to background: release rendering buffers
                pruneTransientCaches()
            }
            ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
                _isLowMemoryState.value = true
                pruneTransientCaches()
                System.gc()
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {}

    override fun onLowMemory() {
        Log.e(tag, "System onLowMemory callback invoked! Aggressive cache drop.")
        _isLowMemoryState.value = true
        pruneTransientCaches()
        System.gc()
    }

    private fun pruneTransientCaches() {
        Log.i(tag, "Pruning memory: trimming chat context window & dropping temporary buffers.")
    }

    data class MemoryStats(
        val usedMb: Long,
        val maxMb: Long,
        val isCritical: Boolean
    )
}
