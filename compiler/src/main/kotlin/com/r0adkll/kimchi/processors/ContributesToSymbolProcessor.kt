// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.processors

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.r0adkll.kimchi.annotations.ContributesTo
import com.r0adkll.kimchi.annotations.ContributesToAnnotation
import com.r0adkll.kimchi.util.KimchiException
import com.r0adkll.kimchi.util.ksp.isAnnotation
import com.r0adkll.kimchi.util.ksp.isInterface
import com.squareup.kotlinpoet.ClassName
import kotlin.reflect.KClass

internal class ContributesToSymbolProcessor(
  env: SymbolProcessorEnvironment,
) : HintSymbolProcessor(env) {

  @AutoService(SymbolProcessorProvider::class)
  class Provider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
      return ContributesToSymbolProcessor(environment)
    }
  }

  override val annotation: KClass<*>
    get() = ContributesTo::class

  override fun getScopes(element: KSClassDeclaration): Set<ClassName> {
    return element.annotations
      .filter { it.isAnnotation(ContributesTo::class) }
      .map { ContributesToAnnotation.from(it).scope }
      .toSet()
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
