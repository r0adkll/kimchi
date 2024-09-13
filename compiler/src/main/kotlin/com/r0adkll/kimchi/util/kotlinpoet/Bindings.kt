// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.util.kotlinpoet

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.r0adkll.kimchi.annotations.ContributesMultibinding
import com.r0adkll.kimchi.util.buildFun
import com.r0adkll.kimchi.util.buildProperty
import com.r0adkll.kimchi.util.ksp.MapKeyValue
import com.r0adkll.kimchi.util.ksp.findBindingTypeFor
import com.r0adkll.kimchi.util.ksp.findMapKey
import com.r0adkll.kimchi.util.ksp.findQualifier
import com.r0adkll.kimchi.util.ksp.hasAnnotation
import com.r0adkll.kimchi.util.ksp.pairTypeOf
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.toAnnotationSpec
import com.squareup.kotlinpoet.ksp.toClassName
import kotlin.reflect.KClass
import me.tatarka.inject.annotations.Inject
import me.tatarka.inject.annotations.IntoMap
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

fun TypeSpec.Builder.addBinding(
  boundClass: KSClassDeclaration,
  bindingAnnotationClass: KClass<*>,
): TypeSpec.Builder {
  val isMultibinding = bindingAnnotationClass == ContributesMultibinding::class
  val mapKey = if (isMultibinding) {
    boundClass.findMapKey()
  } else {
    null
  }

  val boundType = boundClass.findBindingTypeFor(bindingAnnotationClass)
  val isObject = boundClass.classKind == ClassKind.OBJECT
  val isBindable = !isObject && boundClass.hasAnnotation(Inject::class)

  boundClass.containingFile?.let { addOriginatingKSFile(it) }

  if (mapKey != null) {
    addMappingProvidesFunction(
      boundClass = boundClass,
      boundType = boundType,
      mapKey = mapKey,
      isBindable = isBindable,
    )
  } else if (isBindable) {
    addBindingReceiverProperty(
      boundClass = boundClass,
      boundType = boundType.toClassName(),
      additionalAnnotations = listIf(isMultibinding, IntoSet::class),
    )
  } else {
    addProvidesFunction(
      boundClass = boundClass,
      returnType = boundType.toClassName(),
      additionalAnnotations = listIf(isMultibinding, IntoSet::class),
    )
  }

  return this
}

private fun TypeSpec.Builder.addBindingReceiverProperty(
  boundClass: KSClassDeclaration,
  boundType: ClassName,
  additionalAnnotations: List<KClass<*>> = emptyList(),
) {
  addProperty(
    PropertySpec.buildProperty(
      name = "bind",
      type = boundType,
    ) {
      receiver(boundClass.toClassName())

      getter(
        FunSpec.getterBuilder()
          .addAnnotation(Provides::class)
          .apply {
            // Add any qualifier that the bound class has
            boundClass.findQualifier()?.let { qualifier ->
              addAnnotation(qualifier.toAnnotationSpec())
            }

            // Add additional annotations passed
            additionalAnnotations.forEach {
              addAnnotation(it)
            }
          }
          .addStatement("return this")
          .build(),
      )
    },
  )
}

private fun TypeSpec.Builder.addProvidesFunction(
  boundClass: KSClassDeclaration,
  returnType: TypeName,
  additionalAnnotations: List<KClass<*>> = emptyList(),
) {
  addFunction(
    FunSpec.buildFun("provide${boundClass.simpleName.asString()}") {
      returns(returnType)

      addAnnotation(Provides::class)

      // Add any qualifiers added to the bound class
      boundClass.findQualifier()?.let { qualifier ->
        addAnnotation(qualifier.toAnnotationSpec())
      }

      // Add passed additional annotations
      additionalAnnotations.forEach { annotation ->
        addAnnotation(annotation)
      }

      if (boundClass.classKind == ClassKind.OBJECT) {
        addStatement("return %T", boundClass.toClassName())
      } else {
        addStatement("return %T()", boundClass.toClassName())
      }
    },
  )
}

private fun TypeSpec.Builder.addMappingProvidesFunction(
  boundClass: KSClassDeclaration,
  boundType: KSClassDeclaration,
  mapKey: MapKeyValue,
  isBindable: Boolean,
) {
  addFunction(
    FunSpec.buildFun("provide${boundType.simpleName.asString()}_${mapKey.functionSuffix()}") {
      returns(pairTypeOf(mapKey.type(), boundType.toClassName()))

      addAnnotation(Provides::class)
      addAnnotation(IntoMap::class)

      boundClass.findQualifier()?.let { qualifier ->
        addAnnotation(qualifier.toAnnotationSpec())
      }

      if (isBindable) {
        val (format, value) = mapKey.value()
        addParameter("value", boundClass.toClassName())
        addStatement("return ($format to value)", value)
      } else {
        val (format, value) = mapKey.value()
        val valueTemplate = if (boundClass.classKind == ClassKind.OBJECT) "%T" else "%T()"
        addStatement("return ($format to $valueTemplate)", value, boundClass.toClassName())
      }
    },
  )
}

private fun <T> listIf(predicate: Boolean, vararg items: T): List<T> {
  return if (predicate) {
    listOf(*items)
  } else {
    emptyList()
  }
}
