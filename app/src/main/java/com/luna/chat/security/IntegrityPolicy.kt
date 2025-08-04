package com.luna.chat.security

/**
 * Defines the runtime integrity enforcement behavior.
 *
 * ALLOW_WITH_WARNING:
 * - Log and show a warning but continue normal operation.
 *
 * LIMITED_MODE:
 * - Enter a restricted feature set. The app should disable risky features.
 *   See SecurityConfig.setLimitedModeEnabled(true). A future improvement could
 *   present a blocking screen until the user acknowledges the risks.
 *
 * BLOCK_STARTUP:
 * - Terminate the process gracefully after logging a warning.
 *   Future improvement: replace kill with a dedicated blocking screen/activity.
 */
enum class IntegrityPolicy {
    ALLOW_WITH_WARNING,
    LIMITED_MODE,
    BLOCK_STARTUP
}