package com.r0adkll.kimchi.util.kotlinpoet

import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ksp.toAnnotationSpec
import com.squareup.kotlinpoet.ksp.toTypeName

fun KSValueParameter.toParameterSpec(): ParameterSpec {
  return ParameterSpec.builder(name!!.asString(), type.toTypeName())
    .addAnnotations(
      annotations
        .map { it.toAnnotationSpec() }
        .toList()
    )
    .build()
}
