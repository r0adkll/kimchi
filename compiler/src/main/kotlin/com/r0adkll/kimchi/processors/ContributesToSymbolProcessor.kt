// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.processors

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.r0adkll.kimchi.HINT_CONTRIBUTES_PACKAGE
import com.r0adkll.kimchi.annotations.ContributesTo
import com.r0adkll.kimchi.util.ksp.findAnnotation
import com.r0adkll.kimchi.util.ksp.getScope
import com.r0adkll.kimchi.util.toClassName
import com.squareup.kotlinpoet.ClassName
import kotlin.reflect.KClass

internal class ContributesToSymbolProcessor(
  env: SymbolProcessorEnvironment,
) : HintSymbolProcessor(env, HINT_CONTRIBUTES_PACKAGE) {

  @AutoService(SymbolProcessorProvider::class)
  class Provider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
      return ContributesToSymbolProcessor(environment)
    }
  }

  override val annotation: KClass<*>
    get() = ContributesTo::class

  override fun getScope(element: KSClassDeclaration): ClassName {
    return element.findAnnotation(ContributesTo::class)
      ?.getScope()
      ?.toClassName()
      ?: throw IllegalArgumentException("Unable to find scope for ${element.qualifiedName}")
  }
}
