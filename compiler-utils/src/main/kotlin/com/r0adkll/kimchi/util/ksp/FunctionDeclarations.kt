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
fun KSFunctionDeclaration.findParameterThatImplements(className: ClassName): KSValueParameter? {
  return parameters.find { parameter ->
    val classDecl = parameter.type.resolve().declaration as? KSClassDeclaration
    if (classDecl != null) {
      return@find classDecl.getAllSuperTypes()
        .any { it.declaration.toClassName() == className }
    }
    false
  }
}

fun KSFunctionDeclaration.findParameterThatIs(className: ClassName): KSValueParameter? {
  return parameters.find { parameter ->
    parameter.type.resolve().toClassName() == className
  }
}

fun KSFunctionDeclaration.returnTypeIs(clazz: KClass<*>): Boolean {
  return returnTypeIs(clazz.asClassName())
}

fun KSFunctionDeclaration.returnTypeIs(className: ClassName): Boolean {
  return returnType
    ?.findActualType()
    ?.getAllSuperTypes()
    ?.any { it.declaration.toClassName() == className } == true
}

fun KSFunctionDeclaration.directReturnTypeIs(clazz: KClass<*>): Boolean {
  return directReturnTypeIs(clazz.asClassName())
}

fun KSFunctionDeclaration.directReturnTypeIs(className: ClassName): Boolean {
  return returnType
    ?.findActualType()
    ?.toClassName() == className
}
