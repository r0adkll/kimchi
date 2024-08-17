// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.util.kotlinpoet

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.ParameterSpec

fun KSFunctionDeclaration.parameterSpecs(): List<ParameterSpec> {
  return parameters.map { it.toParameterSpec() }
}
