package com.luna.chat.security

import java.text.Normalizer

internal actual fun nfkdNormalize(s: String): String =
    Normalizer.normalize(s, Normalizer.Form.NFKD)
