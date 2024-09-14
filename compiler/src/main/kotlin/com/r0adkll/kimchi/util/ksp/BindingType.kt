// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.util.ksp

import com.r0adkll.kimchi.annotations.AnnotatedElement
import com.r0adkll.kimchi.annotations.BindingAnnotation
import com.r0adkll.kimchi.util.KimchiException
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName

fun <A : BindingAnnotation> AnnotatedElement<A>.findBindingType(): ClassName {
  // 1) Check if there is an explicit type defined by the binding annotation
  return if (annotation.boundType != Unit::class.asClassName()) {
    return annotation.boundType
  } else {
    val superTypeCount = element.superTypes
      .filterNot { it.toTypeName() == Any::class.asTypeName() }
      .count()
    if (superTypeCount == 0) {
      throw KimchiException(
        "Bound implementation must have a single supertype, " +
          "or specify a 'boundType' if extending more than one supertype.",
        element,
      )
    }
    if (superTypeCount > 1) {
      throw KimchiException(
        "Bound implementation is extending more than one supertype. " +
          "Please specify an explicit 'boundType'.",
        element,
      )
    }

    element.superTypes.first().findActualType().toClassName()
  }
}

private const val BOUND_TYPE_NAME = "boundType"
private const val BOUND_TYPE_POSITIONAL_INDEX = 1
