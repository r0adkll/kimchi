// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.util.ksp

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSValueArgument
import kotlin.reflect.KClass

public fun KSAnnotation.argumentAt(name: String, index: Int): KSValueArgument? {
  val isPositional = arguments.any { it.name == null }
  return if (isPositional) {
    arguments.getOrNull(index)
  } else {
    arguments.find { arg -> arg.name?.asString() == name }
  }
}

public fun KSAnnotation.defaultArgumentAt(name: String, index: Int): KSValueArgument? {
  val isPositional = defaultArguments.any { it.name == null }
  return if (isPositional) {
    defaultArguments.getOrNull(index)
  } else {
    defaultArguments.find { arg -> arg.name?.asString() == name }
  }
}

public fun KSAnnotation.isAnnotationOf(clazz: KClass<*>): Boolean {
  return annotationType.findActualType()
    .annotations
    .any { it.isAnnotation(clazz) }
}
