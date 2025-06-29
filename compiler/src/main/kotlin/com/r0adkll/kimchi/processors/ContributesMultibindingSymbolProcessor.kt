// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.processors

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.r0adkll.kimchi.annotations.ContributesMultibinding
import com.r0adkll.kimchi.annotations.ContributesMultibindingAnnotation
import com.r0adkll.kimchi.util.KimchiException
import com.r0adkll.kimchi.util.ksp.isAnnotation
import com.squareup.kotlinpoet.ClassName
import kotlin.reflect.KClass

internal class ContributesMultibindingSymbolProcessor(
  env: SymbolProcessorEnvironment,
) : HintSymbolProcessor(env) {

  @AutoService(SymbolProcessorProvider::class)
  class Provider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
      return ContributesMultibindingSymbolProcessor(environment)
    }
  }

  override val annotation: KClass<*>
    get() = ContributesMultibinding::class

  override fun getScopes(element: KSClassDeclaration): Set<ClassName> {
    return element.annotations
      .filter { it.isAnnotation(ContributesMultibinding::class) }
      .map { ContributesMultibindingAnnotation.from(it).scope }
      .toSet()
  }

  override fun validate(element: KSClassDeclaration) {
    val annotations = element.annotations
      .filter { it.isAnnotation(ContributesMultibinding::class) }
      .map { ContributesMultibindingAnnotation.from(it) }
      .toList()

    val uniqueAnnotations = annotations
      .map { it.scope to it.boundType }
      .toSet()

    if (annotations.size != uniqueAnnotations.size) {
      throw KimchiException(
        "You cannot use @ContributesMultibinding multiple times for the same scope and same bound type",
        element,
      )
    }
  }
}
