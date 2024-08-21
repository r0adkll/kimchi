// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.annotations

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.r0adkll.kimchi.util.KimchiException
import com.r0adkll.kimchi.util.ksp.argumentAt
import com.r0adkll.kimchi.util.ksp.findAnnotation
import com.r0adkll.kimchi.util.ksp.valueAsClassName
import com.r0adkll.kimchi.util.ksp.valueAsClassNameList
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName

class MergeComponentAnnotation private constructor(
  override val scope: ClassName,
  private val excludes: List<ClassName>,
) : MergingAnnotation {

  override fun excludes(contribution: KSClassDeclaration): Boolean {
    return excludes.contains(contribution.toClassName())
  }

  companion object {
    fun from(annotated: KSAnnotated): MergeComponentAnnotation {
      val annotation = annotated.findAnnotation(MergeComponent::class)
        ?: throw KimchiException("Unable to find @MergeComponent annotation", annotated)

      val scope = annotation.argumentAt("scope", 0)
        ?.valueAsClassName
        ?: throw KimchiException("Unable to find `scope` on @MergeComponent annotation", annotated)

      val excludes = annotation.argumentAt("excludes", 1)
        ?.valueAsClassNameList
        ?: emptyList()

      return MergeComponentAnnotation(scope, excludes)
    }
  }
}
