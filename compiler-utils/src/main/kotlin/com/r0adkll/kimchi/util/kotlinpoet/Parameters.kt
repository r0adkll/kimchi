// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.util.kotlinpoet

import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ksp.toAnnotationSpec
import com.squareup.kotlinpoet.ksp.toTypeName

public fun KSValueParameter.toParameterSpec(
  builder: ParameterSpec.Builder.() -> Unit = {},
): ParameterSpec {
  return ParameterSpec.builder(name!!.asString(), type.toTypeName())
    .addAnnotations(
      annotations
        .map { it.toAnnotationSpec() }
        .toList(),
    )
    .apply(builder)
    .build()
}
