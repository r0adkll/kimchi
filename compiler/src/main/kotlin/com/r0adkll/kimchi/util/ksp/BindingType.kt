// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.util.ksp

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.r0adkll.kimchi.util.KimchiException
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toTypeName
import kotlin.reflect.KClass

fun KSClassDeclaration.findBindingTypeFor(
  bindingAnnotation: KClass<*>,
): KSDeclaration {
  // 1) Check if there is an explicit type defined by the binding annotation
  val annotation = findAnnotation(bindingAnnotation)
    ?: error("Unable to find annotation, ${bindingAnnotation.simpleName}, on class ${simpleName.asString()}")

  val boundTypeArgument = annotation.argumentAt(BOUND_TYPE_NAME, BOUND_TYPE_POSITIONAL_INDEX)
  val defaultTypeArgument = annotation.defaultArgumentAt(BOUND_TYPE_NAME, BOUND_TYPE_POSITIONAL_INDEX)

  return if (boundTypeArgument != null && boundTypeArgument.value != defaultTypeArgument?.value) {
    (boundTypeArgument.value as KSType).declaration
  } else {
    val superTypeCount = superTypes
      .filterNot { it.toTypeName() == Any::class.asTypeName() }
      .count()
    if (superTypeCount == 0) {
      throw KimchiException(
        "Bound implementation must have a single supertype, " +
          "or specify a 'boundType' if extending more than one supertype.",
        this,
      )
    }
    if (superTypeCount > 1) {
      throw KimchiException(
        "Bound implementation is extending more than one supertype. " +
          "Please specify an explicit 'boundType'.",
        this,
      )
    }

    superTypes.first().findActualType()
  }
}

private const val BOUND_TYPE_NAME = "boundType"
private const val BOUND_TYPE_POSITIONAL_INDEX = 1
