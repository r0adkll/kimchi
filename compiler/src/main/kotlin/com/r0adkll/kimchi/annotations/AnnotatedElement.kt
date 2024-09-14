// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.annotations

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.r0adkll.kimchi.util.ksp.MapKeyValue
import com.r0adkll.kimchi.util.ksp.findMapKey
import com.r0adkll.kimchi.util.ksp.findQualifier
import com.r0adkll.kimchi.util.ksp.hasAnnotation
import me.tatarka.inject.annotations.Inject

class AnnotatedElement<Annotation>(
  val element: KSClassDeclaration,
  val annotation: Annotation,
) {

  val isObject: Boolean
    get() = element.classKind == ClassKind.OBJECT

  val isInjected: Boolean
    get() = element.hasAnnotation(Inject::class)

  fun qualifier(): KSAnnotation? {
    return element.findQualifier()
  }

  fun mapKey(): MapKeyValue? {
    return element.findMapKey()
  }
}
