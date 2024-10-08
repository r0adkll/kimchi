// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi

import com.r0adkll.kimchi.util.capitalized
import com.squareup.kotlinpoet.asClassName
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.kotlinFunction
import strikt.api.expectThat
import strikt.assertions.isEqualTo

fun Class<*>.getHint(hintPackage: String): KClass<*>? = contributedProperties(hintPackage)
  ?.filter { it.java == this }
  ?.also { expectThat(it.size).isEqualTo(1) }
  ?.first()

fun Class<*>.getHintScope(hintPackage: String): KClass<*>? =
  contributedProperties(hintPackage)
    ?.also { expectThat(it.size).isEqualTo(2) }
    ?.single { it.java != this }

fun Class<*>.contributedProperties(
  hintPackageName: String,
): List<KClass<*>>? {
  // The capitalize() comes from kotlinc's implicit handling of file names -> class names. It will
  // always, unless otherwise instructed via `@file:JvmName`, capitalize its facade class.
  val className = kotlin.asClassName()
    .canonicalName
    .replace(".", "_")
    .capitalized()
    .plus("Kt")

  val clazz = try {
    classLoader.loadClass("$hintPackageName.$className")
  } catch (e: ClassNotFoundException) {
    return null
  }

  return clazz.declaredFields
    .sortedBy { it.name }
    .map { field -> field.use { it.get(null) } }
    .filterIsInstance<KClass<*>>()
}

fun Class<*>.topLevelFunctions(
  packageName: String,
): List<KFunction<*>>? {
  // The capitalize() comes from kotlinc's implicit handling of file names -> class names. It will
  // always, unless otherwise instructed via `@file:JvmName`, capitalize its facade class.
  val className = kotlin.asClassName()
    .simpleName
    .capitalized()
    .plus("Kt")

  val clazz = try {
    classLoader.loadClass("$packageName.$className")
  } catch (e: ClassNotFoundException) {
    return null
  }

  return clazz.declaredMethods
    .sortedBy { it.name }
    .mapNotNull { method -> method.kotlinFunction }
}
