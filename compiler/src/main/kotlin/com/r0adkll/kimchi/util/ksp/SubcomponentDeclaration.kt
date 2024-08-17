// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.util.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.r0adkll.kimchi.annotations.ContributesSubcomponent
import com.r0adkll.kimchi.util.buildFun
import com.r0adkll.kimchi.util.kotlinpoet.toParameterSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName

/**
 * A custom overlay of [KSClassDeclaration] to provide a unified way of
 * accessing the specific components and assumptions of declarations
 * annotated with [com.r0adkll.kimchi.annotations.ContributesSubcomponent]
 */
class SubcomponentDeclaration(
  private val clazz: KSClassDeclaration,
) : KSClassDeclaration by clazz {

  val subcomponentSimpleName: String
    get() = "Merged${simpleName.asString()}"

  @OptIn(KspExperimental::class)
  val factoryClass: FactoryDeclaration by lazy {
    declarations
      .filterIsInstance<KSClassDeclaration>()
      .filter { it.isAnnotationPresent(ContributesSubcomponent.Factory::class) }
      .map { FactoryDeclaration(this, it) }
      .firstOrNull()
      ?: throw IllegalStateException(
        "@ContributesSubcomponent must define a factory interface annotated with " +
          "@ContributesSubcomponent.Factory",
      )
  }

  fun createFactoryFunctionOverload(
    isGenerateCompanionExtensionsEnabled: Boolean,
  ): FunSpec = with(factoryClass) {
    return FunSpec.buildFun(factoryFunction.simpleName.asString()) {
      addModifiers(KModifier.OVERRIDE)

      returns(factoryFunction.returnType!!.toTypeName())

      val factoryParameters = factoryFunction.parameters.map { it.toParameterSpec() }
      addParameters(factoryParameters)

      // Build the return statement constructing the expected merged subcomponent, including
      // parent.
      val componentCreationFunction = if (isGenerateCompanionExtensionsEnabled) {
        "%L.create"
      } else {
        "%L::class.create"
      }
      addStatement(
        "return $componentCreationFunction(${factoryParameters.joinToString { "%L" }}" +
          "${if (factoryParameters.isNotEmpty()) ", " else ""}this)",
        subcomponentSimpleName,
        *factoryParameters.map { it.name }.toTypedArray(),
      )
    }
  }

  /**
   * A custom overlay of [KSClassDeclaration] to provide a unified way of accessing
   * the specific components and assumptions of declarations annotated with
   * [com.r0adkll.kimchi.annotations.ContributesSubcomponent.Factory]
   */
  class FactoryDeclaration(
    private val subcomponent: SubcomponentDeclaration,
    private val clazz: KSClassDeclaration,
  ) : KSClassDeclaration by clazz {

    init {
      require(isInterface) {
        "@ContributesSubcomponent.Factory annotated declarations must be an interface"
      }
    }

    val factoryFunction: KSFunctionDeclaration by lazy {
      getDeclaredFunctions()
        .singleOrNull()
        ?.also {
          require(it.returnType != null) { "Factory methods are required to return their component" }
          require(it.returnType!!.toTypeName() == subcomponent.toClassName()) {
            "Factory methods are required to return their component"
          }
        }
        ?: throw IllegalStateException(
          "@ContributeSubcomponent.Factory interfaces must only have a " +
            "single function declared",
        )
    }
  }
}
