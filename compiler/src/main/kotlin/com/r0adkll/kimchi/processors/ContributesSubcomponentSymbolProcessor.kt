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
import com.r0adkll.kimchi.annotations.ContributesSubcomponentAnnotation
import com.r0adkll.kimchi.util.KimchiException
import com.r0adkll.kimchi.util.ksp.component.SubcomponentDeclaration
import com.r0adkll.kimchi.util.ksp.hasAnnotation
import com.r0adkll.kimchi.util.ksp.isInterface
import com.squareup.kotlinpoet.ClassName
import kotlin.reflect.KClass
import me.tatarka.inject.annotations.Component

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

  override fun getScopes(element: KSClassDeclaration): Set<ClassName> {
    return setOf(
      ContributesSubcomponentAnnotation.from(element).parentScope,
    )
  }

  override fun validate(element: KSClassDeclaration) {
    if (!element.isInterface) {
      throw KimchiException(
        "@ContributesSubcomponent can only be applied to an interface",
        element,
      )
    }

    val subcomponentFactoryHasExplicitParent = SubcomponentDeclaration(element)
      .factoryClass
      .factoryFunction
      .parameters
      .any { it.hasAnnotation(Component::class) }

    if (subcomponentFactoryHasExplicitParent) {
      throw KimchiException(
        "@ContributesSubcomponent.Factory functions cannot explicitly define a @Component parent",
        element,
      )
    }
  }
}
