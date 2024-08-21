// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.processors

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.r0adkll.kimchi.HINT_BINDING_PACKAGE
import com.r0adkll.kimchi.annotations.ContributesBinding
import com.r0adkll.kimchi.annotations.ContributesBindingAnnotation
import com.squareup.kotlinpoet.ClassName
import kotlin.reflect.KClass

internal class ContributesBindingSymbolProcessor(
  env: SymbolProcessorEnvironment,
) : HintSymbolProcessor(env, HINT_BINDING_PACKAGE) {

  @AutoService(SymbolProcessorProvider::class)
  class Provider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
      return ContributesBindingSymbolProcessor(environment)
    }
  }

  override val annotation: KClass<*>
    get() = ContributesBinding::class

  override fun getScope(element: KSClassDeclaration): ClassName {
    return ContributesBindingAnnotation.from(element).scope
  }
}
