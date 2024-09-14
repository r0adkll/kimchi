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

class ContributesToAnnotation private constructor(
  val scope: ClassName,
  override val replaces: List<ClassName>,
) : ReplaceableAnnotation {
  companion object {

    /**
     * Find the [ContributesTo] annotation on a given [annotated] element
     * that is scoped to [scope]
     * @param annotated The annotated element to pull from
     * @param scope The scope of the annotation we are looking for
     * @return the [ContributesToAnnotation] for the given scope, or throws [KimchiException]
     */
    fun from(annotated: KSAnnotated, scope: ClassName): ContributesToAnnotation {
      annotated.annotations
        .filter { it.isAnnotation(ContributesTo::class) }
        .forEach { annotation ->
          val contributesToAnnotation = from(annotation)
          if (contributesToAnnotation.scope == scope) {
            return contributesToAnnotation
          }
        }

      throw KimchiException("Unable to find @ContributesTo annotation for $scope", annotated)
    }

    fun from(annotation: KSAnnotation): ContributesToAnnotation {
      val scope = annotation.argumentAt("scope", 0)
        ?.valueAsClassName
        ?: throw KimchiException("Unable to find `scope` on @ContributesMultibinding annotation", annotation.parent)

      val replaces = annotation.argumentAt("replaces", 2)
        ?.valueAsClassNameList
        ?: emptyList()

      return ContributesToAnnotation(scope, replaces)
    }
  }
}
