package com.luna.chat.hrr

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Fact(
    @SerialName("key") val key: String,
    @SerialName("value") val value: String,
    @SerialName("hits") val hits: Int = 0,
    @SerialName("lastSession") val lastSession: String = "",
)

@Serializable
data class NuggetData(
    @SerialName("version") val version: Int = 1,
    @SerialName("name") val name: String,
    @SerialName("D") val dimension: Int = HrrDefaults.DIMENSION,
    @SerialName("banks") val banks: Int = HrrDefaults.BANKS,
    @SerialName("facts") val facts: List<Fact> = emptyList(),
)

object HrrDefaults {
    const val DIMENSION = 1024
    const val BANKS = 2
    const val TEMPERATURE = 0.9
    const val ORTH_ITERATIONS = 1
    const val PROMOTION_THRESHOLD = 3
    const val BANK_SEED_STRIDE = 31337
    const val VOCAB_SEED_OFFSET = 77777
    const val SENTENCE_KEY_OFFSET = 7919
    const val FUZZY_MATCH_THRESHOLD = 0.55
}
