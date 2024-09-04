// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.util.ksp

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import me.tatarka.inject.annotations.Scope

/**
 * Find any [Scope] marked annotation attached to [this] class
 * @receiver an annotated element expected to be marked with a scope
 * @return the scope annotation if found, else null
 */
fun KSAnnotated.findInjectScope(): KSAnnotation? {
  return annotations.find { annotation ->
    annotation.isAnnotationOf(Scope::class)
  }
}
