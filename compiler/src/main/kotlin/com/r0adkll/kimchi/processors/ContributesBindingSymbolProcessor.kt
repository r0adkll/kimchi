// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.processors

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.r0adkll.kimchi.annotations.ContributesBinding
import com.r0adkll.kimchi.annotations.ContributesBindingAnnotation
import com.r0adkll.kimchi.util.KimchiException
import com.r0adkll.kimchi.util.ksp.hasAnnotation
import com.r0adkll.kimchi.util.ksp.isAnnotation
import com.squareup.kotlinpoet.ClassName
import kotlin.reflect.KClass
import me.tatarka.inject.annotations.Inject

internal class ContributesBindingSymbolProcessor(
  env: SymbolProcessorEnvironment,
) : HintSymbolProcessor(env) {

  @AutoService(SymbolProcessorProvider::class)
  class Provider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
      return ContributesBindingSymbolProcessor(environment)
    }
  }

  override val annotation: KClass<*>
    get() = ContributesBinding::class

  override fun getScopes(element: KSClassDeclaration): Set<ClassName> {
    return element.annotations
      .filter { it.isAnnotation(ContributesBinding::class) }
      .map { ContributesBindingAnnotation.from(it).scope }
      .toSet()
  }

  override fun validate(element: KSClassDeclaration) {
    val hasInjectAnnotation = element.hasAnnotation(Inject::class)
    val hasInjectedParameters = element.primaryConstructor
      ?.parameters
      ?.isNotEmpty() == true

    if (hasInjectedParameters && !hasInjectAnnotation) {
      throw KimchiException(
        "@ContributesBinding annotated classes with injected constructor parameters require an '@Inject' annotation",
        element,
      )
    }

    val annotations = element.annotations
      .filter { it.isAnnotation(ContributesBinding::class) }
      .map { ContributesBindingAnnotation.from(it) }
      .toList()

    val uniqueAnnotations = annotations
      .map { it.scope to it.boundType }
      .toSet()

    if (annotations.size != uniqueAnnotations.size) {
      throw KimchiException(
        "You cannot use @ContributesBinding multiple times for the same scope and same bound type",
        element,
      )
    }
  }
}
