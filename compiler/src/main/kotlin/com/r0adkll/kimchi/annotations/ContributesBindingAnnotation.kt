// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.annotations

import com.google.devtools.ksp.symbol.KSAnnotated
import com.r0adkll.kimchi.util.KimchiException
import com.r0adkll.kimchi.util.ksp.argumentAt
import com.r0adkll.kimchi.util.ksp.findAnnotation
import com.r0adkll.kimchi.util.ksp.valueAsClassName
import com.r0adkll.kimchi.util.ksp.valueAsClassNameList
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName

class ContributesBindingAnnotation private constructor(
  val scope: ClassName,
  val boundType: ClassName,
  override val replaces: List<ClassName>,
  val rank: Int,
) : ReplaceableAnnotation {
  companion object {
    fun from(annotated: KSAnnotated): ContributesBindingAnnotation {
      val annotation = annotated.findAnnotation(ContributesBinding::class)
        ?: throw KimchiException("Unable to find @ContributesBinding annotation", annotated)

      val scope = annotation.argumentAt("scope", 0)
        ?.valueAsClassName
        ?: throw KimchiException("Unable to find `scope` on @ContributesBinding annotation", annotated)

      val boundType = annotation.argumentAt("boundType", 1)
        ?.valueAsClassName
        ?: Unit::class.asClassName()

      val replaces = annotation.argumentAt("replaces", 2)
        ?.valueAsClassNameList
        ?: emptyList()

      val rank = annotation.argumentAt("rank", 3)
        ?.value
        ?.let { it as? Int }
        ?: ContributesBinding.RANK_NORMAL

      return ContributesBindingAnnotation(scope, boundType, replaces, rank)
    }
  }
}
