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

class ContributesSubcomponentAnnotation private constructor(
  override val scope: ClassName,
  val parentScope: ClassName,
  private val excludes: List<ClassName>,
  override val replaces: List<ClassName>,
) : MergingAnnotation, ReplaceableAnnotation {

  override fun excludes(contribution: KSClassDeclaration): Boolean {
    return excludes.contains(contribution.toClassName())
  }

  companion object {
    fun from(annotated: KSAnnotated): ContributesSubcomponentAnnotation {
      val annotation = annotated.findAnnotation(ContributesSubcomponent::class)
        ?: throw KimchiException("Unable to find @ContributesSubcomponent annotation", annotated)

      val scope = annotation.argumentAt("scope", 0)
        ?.valueAsClassName
        ?: throw KimchiException("Unable to find `scope` on @ContributesSubcomponent annotation", annotated)

      val parentScope = annotation.argumentAt("parentScope", 1)
        ?.valueAsClassName
        ?: throw KimchiException("Unable to find `parentScope` on @ContributesSubcomponent annotation", annotated)

      val excludes = annotation.argumentAt("excludes", 2)
        ?.valueAsClassNameList
        ?: emptyList()

      val replaces = annotation.argumentAt("replaces", 3)
        ?.valueAsClassNameList
        ?: emptyList()

      return ContributesSubcomponentAnnotation(scope, parentScope, excludes, replaces)
    }
  }
}
