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
import com.r0adkll.kimchi.annotations.ContributesToAnnotation
import com.r0adkll.kimchi.util.KimchiException
import com.r0adkll.kimchi.util.ksp.isInterface
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
    return ContributesToAnnotation.from(element).scope
  }

  override fun validate(element: KSClassDeclaration) {
    if (!element.isInterface) {
      throw KimchiException(
        "@ContributesTo can only contribute interface classes to a scope",
        node = element,
      )
    }
  }
}