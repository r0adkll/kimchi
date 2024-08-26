// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.circuit.util

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.r0adkll.kimchi.util.ksp.findActualType
import com.r0adkll.kimchi.util.ksp.findParameterThatImplements
import com.r0adkll.kimchi.util.ksp.findParameterThatIs
import com.r0adkll.kimchi.util.ksp.implements
import com.r0adkll.kimchi.util.toClassName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock

fun CodeBlock.Builder.addUiFactoryCreateStatement(
  element: KSFunctionDeclaration,
  screen: ClassName,
): CodeBlock.Builder = apply {

  // Build creation code block string, taking special account for the names
  // of parameters that we generate as part of the Ui.Factory, ignoring w/e the
  // user has named their parameters
  val callParameters = element.parameters.map { param ->
    when {
      param.implements(screen) -> "screen"
      param.implements(ClassNames.Circuit.UiState) -> "state"
      param.implements(ClassNames.Modifier) -> "modifier"
      else -> param.name?.asString()
    }
  }

  // Since we need to account for stateless screens, i.e. [StaticScreen], we should search for parameters
  // that implement [StaticScreen] rather than assuming positions
  val stateClassParameter = element.findParameterThatImplements(ClassNames.Circuit.UiState)

  // Validate that a modifier exists, taking the assumption that it is the last parameter
  // due to the nature of standard composable function setups
  element.findParameterThatIs(ClassNames.Modifier)
    ?: throw IllegalStateException("@CircuitInject requires your composable function to have a Modifier parameter")

  // If we couldn't find a provided state class name, assume that this is a [StaticScreen] and just pass a root
  // [CircuitUiState] type parameter.
  val stateClassName = stateClassParameter?.type?.findActualType()?.toClassName()
    ?: ClassNames.Circuit.UiState
  addStatement(
    "%M<%T> { state, modifier -> %T(%L) }",
    MemberNames.CircuitUi,
    stateClassName,
    element.toClassName(),
    callParameters.joinToString(),
  )
}
