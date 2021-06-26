package martian.riddles.data.local

// для SharedPreferences
enum class DataKeys(val key: String) {
    NICKNAME("nickname"),
    LEVEL("level"),
    TOKEN("token"),
    PRIZE("prize"),
    COUNT_LAUNCH_APP("count_launch_app"),
    SHOW_PURCHASE_COUNT("show_purchase_count")
}