// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.util.ksp

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.r0adkll.kimchi.util.toClassName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import kotlin.reflect.KClass

/**
 * Search through all of the [KSFunctionDeclaration.parameters] for one whos type implements the
 * passed [className]
 */
public fun KSFunctionDeclaration.findParameterThatImplements(className: ClassName): KSValueParameter? {
  return parameters.find { parameter -> parameter.implements(className) }
}

/**
 * Return whether or not the current [KSValueParameter] implements the passed [className]
 * @receiver a [KSValueParameter] to evaluate
 * @param className the [ClassName] of the type you are looking for
 */
public fun KSValueParameter.implements(className: ClassName): Boolean {
  val classDecl = type.resolve().declaration as? KSClassDeclaration
  if (classDecl != null) {
    // Check if the direct type implements the passed className
    if (classDecl.toClassName() == className) return true
    return classDecl.getAllSuperTypes()
      .any { it.declaration.toClassName() == className }
  }
  return false
}

public fun KSFunctionDeclaration.returnTypeIs(clazz: KClass<*>): Boolean {
  return returnTypeIs(clazz.asClassName())
}

public fun KSFunctionDeclaration.returnTypeIs(className: ClassName): Boolean {
  return returnType
    ?.findActualType()
    ?.getAllSuperTypes()
    ?.any { it.declaration.toClassName() == className } == true
}

public fun KSFunctionDeclaration.directReturnTypeIs(clazz: KClass<*>): Boolean {
  return directReturnTypeIs(clazz.asClassName())
}

public fun KSFunctionDeclaration.directReturnTypeIs(className: ClassName): Boolean {
  return returnType
    ?.findActualType()
    ?.toClassName() == className
}
