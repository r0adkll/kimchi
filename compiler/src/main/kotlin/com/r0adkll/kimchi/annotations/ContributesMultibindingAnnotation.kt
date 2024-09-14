// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.annotations

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.r0adkll.kimchi.util.KimchiException
import com.r0adkll.kimchi.util.ksp.argumentAt
import com.r0adkll.kimchi.util.ksp.isAnnotation
import com.r0adkll.kimchi.util.ksp.valueAsClassName
import com.r0adkll.kimchi.util.ksp.valueAsClassNameList
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName

class ContributesMultibindingAnnotation private constructor(
  val scope: ClassName,
  override val boundType: ClassName,
  override val replaces: List<ClassName>,
) : ReplaceableAnnotation, BindingAnnotation {
  companion object {

    /**
     * Find any [ContributesMultibinding] annotations on the given [annotated] element that is scoped to [scope].It is
     * technically possible to have multiple annotations pointed at the same [scope], with different bound types. If
     * these annotations have the same bound type, then it is an error. This use case should be validated in the hint
     * generator. If this returns empty, it is also an error
     * @param annotated The annotated element to pull from
     * @param scope The scope of the annotation we are looking for
     * @return the list of [ContributesMultibindingAnnotation] for the given scope, or throws [KimchiException]
     */
    fun from(annotated: KSAnnotated, scope: ClassName): List<ContributesMultibindingAnnotation> {
      return annotated.annotations
        .filter { it.isAnnotation(ContributesMultibinding::class) }
        .mapNotNull { annotation ->
          val contributesBindingAnnotation = from(annotation)
          if (contributesBindingAnnotation.scope == scope) {
            contributesBindingAnnotation
          } else {
            null
          }
        }
        .toList()
        .ifEmpty {
          throw KimchiException("Unable to find @ContributesMultibinding annotation for $scope", annotated)
        }
    }

    fun from(annotation: KSAnnotation): ContributesMultibindingAnnotation {
      val scope = annotation.argumentAt("scope", 0)
        ?.valueAsClassName
        ?: throw KimchiException("Unable to find `scope` on @ContributesMultibinding annotation", annotation.parent)

      val boundType = annotation.argumentAt("boundType", 1)
        ?.valueAsClassName
        ?: Unit::class.asClassName()

      val replaces = annotation.argumentAt("replaces", 2)
        ?.valueAsClassNameList
        ?: emptyList()

      return ContributesMultibindingAnnotation(scope, boundType, replaces)
    }
  }
}
