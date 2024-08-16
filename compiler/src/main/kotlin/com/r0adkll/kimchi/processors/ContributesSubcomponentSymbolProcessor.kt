// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.processors

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.r0adkll.kimchi.HINT_SUBCOMPONENT_PACKAGE
import com.r0adkll.kimchi.annotations.ContributesSubcomponent
import com.r0adkll.kimchi.util.ksp.findAnnotation
import com.r0adkll.kimchi.util.ksp.getParentScope
import com.r0adkll.kimchi.util.toClassName
import com.squareup.kotlinpoet.ClassName
import kotlin.reflect.KClass

internal class ContributesSubcomponentSymbolProcessor(
  env: SymbolProcessorEnvironment,
) : HintSymbolProcessor(env, HINT_SUBCOMPONENT_PACKAGE) {

  @AutoService(SymbolProcessorProvider::class)
  class Provider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
      return ContributesSubcomponentSymbolProcessor(environment)
    }
  }

  override val annotation: KClass<*>
    get() = ContributesSubcomponent::class

  override fun getScope(element: KSClassDeclaration): ClassName {
    return element.findAnnotation(ContributesSubcomponent::class)
      ?.getParentScope()
      ?.toClassName()
      ?: throw IllegalArgumentException("Unable to find parentScope for ${element.simpleName}")
  }
}
