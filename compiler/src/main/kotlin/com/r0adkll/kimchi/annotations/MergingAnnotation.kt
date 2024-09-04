// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.annotations

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.r0adkll.kimchi.util.KimchiException
import com.r0adkll.kimchi.util.ksp.hasAnnotation
import com.squareup.kotlinpoet.ClassName

sealed interface MergingAnnotation {

  /**
   * Get the scope that this annotation is merging elements for
   */
  val scope: ClassName

  /**
   * Check if the [contribution] class is excluded from merging into
   * this component
   */
  fun excludes(contribution: KSClassDeclaration): Boolean

  companion object {
    fun from(annotated: KSAnnotated): MergingAnnotation = when {
      annotated.hasAnnotation(MergeComponent::class) -> MergeComponentAnnotation.from(annotated)
      annotated.hasAnnotation(ContributesSubcomponent::class) -> ContributesSubcomponentAnnotation.from(annotated)
      else -> throw KimchiException("No merging annotation found on element", annotated)
    }
  }
}
