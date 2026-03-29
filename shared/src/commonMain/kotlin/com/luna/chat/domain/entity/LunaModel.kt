package com.luna.chat.domain.entity

data class LunaModel(
    val id: String,
    val displayName: String,
    val contextLength: Int,
    val category: ModelCategory,
    val provider: String,
)

enum class ModelCategory(val label: String, val icon: String) {
    VISION("vision", "\uD83D\uDC41\uFE0F"),   // 👁️
    CODE("code", "\uD83D\uDCBB"),               // 💻
    REASONING("reasoning", "\uD83E\uDDE0"),      // 🧠
    GENERAL("general", "\uD83D\uDCAC"),          // 💬
}

fun categorizeModel(id: String, inputModalities: List<String>): ModelCategory {
    val lower = id.lowercase()
    val hasImageInput = "image" in inputModalities

    return when {
        hasImageInput || "vision" in lower || "vl" in lower -> ModelCategory.VISION
        "coder" in lower || "code" in lower -> ModelCategory.CODE
        "reasoning" in lower || "think" in lower ||
            lower.contains("-o1") || lower.contains("/o1") ||
            lower.contains("-o3") || lower.contains("/o3") ||
            lower.contains("-r1") || lower.contains("/r1") -> ModelCategory.REASONING
        else -> ModelCategory.GENERAL
    }
}
