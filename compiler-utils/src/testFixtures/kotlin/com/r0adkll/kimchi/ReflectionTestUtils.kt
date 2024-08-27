// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi

import com.tschuchort.compiletesting.JvmCompilationResult
import java.lang.reflect.AccessibleObject
import kotlin.reflect.KClass

public inline fun <T, E : AccessibleObject> E.use(block: (E) -> T): T {
  // Deprecated since Java 9, but many projects still use JDK 8 for compilation.
  @Suppress("DEPRECATION")
  val original = isAccessible

  return try {
    isAccessible = true
    block(this)
  } finally {
    isAccessible = original
  }
}

public fun JvmCompilationResult.kotlinClass(className: String): KClass<*> {
  return classLoader.loadClass(className).kotlin
}
