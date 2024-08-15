// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.generators

import com.google.devtools.ksp.symbol.KSDeclaration
import com.r0adkll.kimchi.MergeContext
import com.squareup.kotlinpoet.FileSpec
import kotlin.reflect.KClass

interface Generator {
  val annotation: KClass<*>

  fun generate(
    context: MergeContext,
    element: KSDeclaration,
  ): GeneratedSpec

  fun generate(context: MergeContext): List<GeneratedSpec>
}

infix fun FileSpec.isAggregating(isAggregating: Boolean): GeneratedSpec = GeneratedSpec(this, isAggregating)

data class GeneratedSpec(
  val fileSpec: FileSpec,
  val isAggregating: Boolean,
)
