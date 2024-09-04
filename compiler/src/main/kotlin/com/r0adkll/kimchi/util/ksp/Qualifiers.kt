// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.util.ksp

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import me.tatarka.inject.annotations.Qualifier

/**
 * Find an annotation that is marked with a [Qualifier] annotation
 */
fun KSClassDeclaration.findQualifier(): KSAnnotation? {
  return annotations.find { annotation -> annotation.isQualifier() }
}

private fun KSAnnotation.isQualifier(): Boolean {
  return annotationType.findActualType()
    .annotations
    .any { it.isAnnotation(Qualifier::class) }
}
