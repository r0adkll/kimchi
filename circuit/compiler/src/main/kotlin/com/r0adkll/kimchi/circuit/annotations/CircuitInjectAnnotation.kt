// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.circuit.annotations

import com.google.devtools.ksp.symbol.KSAnnotated
import com.r0adkll.kimchi.util.KimchiException
import com.r0adkll.kimchi.util.ksp.argumentAt
import com.r0adkll.kimchi.util.ksp.findAnnotation
import com.r0adkll.kimchi.util.ksp.valueAsClassName
import com.squareup.kotlinpoet.ClassName

class CircuitInjectAnnotation private constructor(
  val screen: ClassName,
  val scope: ClassName,
) {
  companion object {
    fun from(annotated: KSAnnotated): CircuitInjectAnnotation {
      val annotation = annotated.findAnnotation(CircuitInject::class)
        ?: throw KimchiException("Unable to find @CircuitInject annotation", annotated)

      val screen = annotation.argumentAt("screen", 0)
        ?.valueAsClassName
        ?: throw KimchiException("Unable to find `screen` on @ContributesBinding annotation", annotated)

      val scope = annotation.argumentAt("scope", 1)
        ?.valueAsClassName
        ?: throw KimchiException("Unable to find `scope` on @ContributesBinding annotation", annotated)

      return CircuitInjectAnnotation(screen, scope)
    }
  }
}
