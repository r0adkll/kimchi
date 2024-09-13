// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.util.ksp

import com.google.devtools.ksp.symbol.KSName

/**
 * Convert a [KSName] to a URL-safe string that can be used as a file name
 * @receiver [KSName]
 * @return a url safe name with '.' replaced with '_' characters
 */
public fun KSName.asUrlSafeString(): String = asString().replace(".", "_")
