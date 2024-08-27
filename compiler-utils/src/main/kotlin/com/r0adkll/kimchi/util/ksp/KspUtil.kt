// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.util.ksp

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import kotlin.reflect.KClass

public fun KSAnnotated.hasAnnotation(kclass: KClass<*>): Boolean {
  return hasAnnotation(kclass.asClassName())
}

public fun KSAnnotated.hasAnnotation(className: ClassName): Boolean {
  return hasAnnotation(className.packageName, className.simpleName)
}

public fun KSAnnotated.hasAnnotation(packageName: String, simpleName: String): Boolean {
  return annotations.any { it.hasName(packageName, simpleName) }
}

public fun KSAnnotated.findAnnotation(kclass: KClass<*>): KSAnnotation? {
  val className = kclass.asClassName()
  return findAnnotation(className.packageName, className.simpleName)
}

public fun KSAnnotated.findAnnotation(packageName: String, simpleName: String): KSAnnotation? {
  return annotations.firstOrNull { it.hasName(packageName, simpleName) }
}

public fun KSAnnotation.isAnnotation(kclass: KClass<*>): Boolean {
  val className = kclass.asClassName()
  return hasName(className.packageName, className.simpleName)
}

private fun KSAnnotation.hasName(packageName: String, simpleName: String): Boolean {
  // we can skip resolving if the short name doesn't match
  if (shortName.asString() != simpleName) return false
  val declaration = annotationType.resolve().declaration
  return declaration.packageName.asString() == packageName
}

public fun KSTypeAlias.findActualType(): KSClassDeclaration {
  val resolvedType = this.type.resolve().declaration
  return if (resolvedType is KSTypeAlias) {
    resolvedType.findActualType()
  } else {
    resolvedType as KSClassDeclaration
  }
}

public fun KSTypeReference.findActualType(): KSClassDeclaration {
  val resolvedType = this.resolve().declaration
  return if (resolvedType is KSTypeAlias) {
    resolvedType.findActualType()
  } else {
    resolvedType as KSClassDeclaration
  }
}

public val KSClassDeclaration.isInterface: Boolean
  get() = this.classKind == ClassKind.INTERFACE
