// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.util.ksp

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toTypeName

/**
 * Return whether or not this [KSClassDeclaration] has a supertype of type [clazz] anywhere
 * it its supertype hierarchy
 */
public fun KSClassDeclaration.implements(className: ClassName): Boolean {
  return getAllSuperTypes().any {
    it.toTypeName() == className
  }
}

public val KSClassDeclaration.requireQualifiedName: KSName
  get() = qualifiedName!!
