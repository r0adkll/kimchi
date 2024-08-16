// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.processors

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.r0adkll.kimchi.HINT_MULTIBINDING_PACKAGE
import com.r0adkll.kimchi.annotations.ContributesMultibinding
import com.r0adkll.kimchi.util.ksp.findAnnotation
import com.r0adkll.kimchi.util.ksp.getScope
import com.r0adkll.kimchi.util.toClassName
import com.squareup.kotlinpoet.ClassName
import kotlin.reflect.KClass

internal class ContributesMultibindingSymbolProcessor(
  env: SymbolProcessorEnvironment,
) : HintSymbolProcessor(env, HINT_MULTIBINDING_PACKAGE) {

  @AutoService(SymbolProcessorProvider::class)
  class Provider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
      return ContributesMultibindingSymbolProcessor(environment)
    }
  }

  override val annotation: KClass<*>
    get() = ContributesMultibinding::class

  override fun getScope(element: KSClassDeclaration): ClassName {
    return element.findAnnotation(ContributesMultibinding::class)
      ?.getScope()
      ?.toClassName()
      ?: throw IllegalArgumentException("Unable to find scope for ${element.qualifiedName}")
  }
}
