// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.util.ksp

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.r0adkll.kimchi.annotations.MapKey
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName

/**
 * Find the map key value included in [com.r0adkll.kimchi.annotations.ContributesMultibinding]
 * usage to use as the key when generating the bindings on the graph
 */
fun KSClassDeclaration.findMapKey(): Any? {
  val mapKeyAnnotation = annotations.find { annotation ->
    annotation.isAnnotationOf(MapKey::class)
  } ?: return null

  val mapKeyArgument = mapKeyAnnotation.arguments
    .firstOrNull()
    ?: error("MapKey's must define a single argument to use as the value of the key.")

  return mapKeyArgument.value
    ?: error("MapKey is not provided with a single value that we can find")
}

fun pairTypeOf(vararg typeNames: TypeName): ParameterizedTypeName {
  return Pair::class.asTypeName()
    .parameterizedBy(*typeNames)
}
