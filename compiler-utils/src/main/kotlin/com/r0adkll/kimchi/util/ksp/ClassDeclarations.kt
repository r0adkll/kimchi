package com.r0adkll.kimchi.util.ksp

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName

/**
 * Return whether or not this [KSClassDeclaration] has a supertype of type [clazz] anywhere
 * it its supertype hierarchy
 */
fun KSClassDeclaration.implements(className: ClassName): Boolean {
  return getAllSuperTypes().any {
    it.toClassName() == className
  }
}
