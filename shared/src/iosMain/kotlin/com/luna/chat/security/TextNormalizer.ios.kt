package com.luna.chat.security

import platform.Foundation.NSString
import platform.Foundation.decomposedStringWithCompatibilityMapping

internal actual fun nfkdNormalize(s: String): String =
    (s as NSString).decomposedStringWithCompatibilityMapping
