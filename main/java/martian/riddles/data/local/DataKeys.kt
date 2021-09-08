package martian.riddles.data.local

// для SharedPreferences
enum class DataKeys(val key: String) {
    NICKNAME("nickname"),
    LEVEL("level"),
    TOKEN("token"),
    PRIZE("prize"),
    COUNT_LAUNCH_APP("count_launch_app"),
    SHOW_PURCHASE_COUNT("show_purchase_count"),
    CURRENT_RIDDLE("current_riddle"),
    NEXT_RIDDLE("next_riddle"),
    EMPTY_RIDDLE("empty_riddle"),
    ERROR_LOAD_RIDDLE("error_load_riddle"),
    COUNT_ATTEMPTS("count_attempts"),
    COUNT_WRONG_ANSWERS("count_wrong_answers"),
    DONE_GAME_ANIM_COMPLETE("done_game_animation_complete")
}