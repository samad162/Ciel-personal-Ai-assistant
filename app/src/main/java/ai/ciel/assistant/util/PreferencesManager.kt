package ai.ciel.assistant.util

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("ciel_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ACCENT_COLOR = "accent_color"
        private const val KEY_CHARACTER_STYLE = "character_style"
        private const val KEY_ACTIVE_MODEL = "active_model"
        private const val KEY_MOTION_INTENSITY = "motion_intensity"
        private const val KEY_COMMAND_BAR_ALPHA = "command_bar_alpha"
        private const val KEY_CHAT_DENSITY = "chat_density"
        private const val KEY_WEB_SEARCH = "web_search"
        private const val KEY_CONTINUOUS_VOICE = "continuous_voice"
        private const val KEY_WAKE_WORD = "wake_word_enabled"
        private const val KEY_WAKE_WORD_PHRASE = "wake_word_phrase"
        private const val KEY_VOICE_PITCH = "voice_pitch"
        private const val KEY_SPEECH_RATE = "speech_rate"
        private const val KEY_VOICE_PROFILE = "voice_profile"
        private const val KEY_STREAMING_SPEECH = "streaming_speech"
        private const val KEY_LANGUAGE = "selected_language"
        private const val KEY_USER_NAME = "user_name"
    }

    var accentColor: String
        get() = prefs.getString(KEY_ACCENT_COLOR, "cyan") ?: "cyan"
        set(value) = prefs.edit().putString(KEY_ACCENT_COLOR, value).apply()

    var characterStyle: String
        get() = prefs.getString(KEY_CHARACTER_STYLE, "cinematic") ?: "cinematic"
        set(value) = prefs.edit().putString(KEY_CHARACTER_STYLE, value).apply()

    var activeModel: String
        get() = prefs.getString(KEY_ACTIVE_MODEL, "gpt-4o") ?: "gpt-4o"
        set(value) = prefs.edit().putString(KEY_ACTIVE_MODEL, value).apply()

    var motionIntensity: String
        get() = prefs.getString(KEY_MOTION_INTENSITY, "smooth") ?: "smooth"
        set(value) = prefs.edit().putString(KEY_MOTION_INTENSITY, value).apply()

    var commandBarAlpha: Float
        get() = prefs.getFloat(KEY_COMMAND_BAR_ALPHA, 0.90f)
        set(value) = prefs.edit().putFloat(KEY_COMMAND_BAR_ALPHA, value).apply()

    var chatDensity: String
        get() = prefs.getString(KEY_CHAT_DENSITY, "comfortable") ?: "comfortable"
        set(value) = prefs.edit().putString(KEY_CHAT_DENSITY, value).apply()

    var isWebSearchEnabled: Boolean
        get() = prefs.getBoolean(KEY_WEB_SEARCH, false)
        set(value) = prefs.edit().putBoolean(KEY_WEB_SEARCH, value).apply()

    var isContinuousVoice: Boolean
        get() = prefs.getBoolean(KEY_CONTINUOUS_VOICE, false)
        set(value) = prefs.edit().putBoolean(KEY_CONTINUOUS_VOICE, value).apply()

    var isWakeWordEnabled: Boolean
        get() = prefs.getBoolean(KEY_WAKE_WORD, true)
        set(value) = prefs.edit().putBoolean(KEY_WAKE_WORD, value).apply()

    var wakeWordPhrase: String
        get() = prefs.getString(KEY_WAKE_WORD_PHRASE, "Hey CIEL") ?: "Hey CIEL"
        set(value) = prefs.edit().putString(KEY_WAKE_WORD_PHRASE, value).apply()

    var voicePitch: Float
        get() = prefs.getFloat(KEY_VOICE_PITCH, 1.08f)
        set(value) = prefs.edit().putFloat(KEY_VOICE_PITCH, value).apply()

    var speechRate: Float
        get() = prefs.getFloat(KEY_SPEECH_RATE, 1.00f)
        set(value) = prefs.edit().putFloat(KEY_SPEECH_RATE, value).apply()

    var voiceProfile: String
        get() = prefs.getString(KEY_VOICE_PROFILE, "kaoruko_gentle") ?: "kaoruko_gentle"
        set(value) = prefs.edit().putString(KEY_VOICE_PROFILE, value).apply()

    var isStreamingSpeech: Boolean
        get() = prefs.getBoolean(KEY_STREAMING_SPEECH, true)
        set(value) = prefs.edit().putBoolean(KEY_STREAMING_SPEECH, value).apply()

    var selectedLanguage: String
        get() = prefs.getString(KEY_LANGUAGE, "auto") ?: "auto"
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value).apply()

    var userName: String
        get() = prefs.getString(KEY_USER_NAME, "Samad") ?: "Samad"
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()
}
