// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.util.ksp

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec

/**
 * Represents a parameter to be added to a generated Merged component,
 * including the value property spec if it is not just passed through
 * to the superclass implementation.
 */
class ConstructorParameter(
  val parameterSpec: ParameterSpec,
  val propertySpec: PropertySpec? = null,
)

/**
 * Convenience helper function for adding all the [ConstructorParameter.parameterSpec]
 * to a given function builder.
 */
fun FunSpec.Builder.addParameters(parameters: List<ConstructorParameter>): FunSpec.Builder {
  return addParameters(parameters.map { it.parameterSpec })
}
