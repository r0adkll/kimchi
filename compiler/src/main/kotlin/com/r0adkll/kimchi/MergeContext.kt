// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSDeclaration
import kotlin.reflect.KClass

data class MergeContext(
  val logger: KSPLogger,
  val resolver: Resolver,
  val classScanner: ClassScanner,
  val contributionCache: GeneratedContributionCache,
  val defer: (element: KSDeclaration, annotation: KClass<*>) -> Unit,
)
