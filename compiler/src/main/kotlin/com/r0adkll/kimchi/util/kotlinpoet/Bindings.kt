// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.util.kotlinpoet

import com.r0adkll.kimchi.annotations.AnnotatedElement
import com.r0adkll.kimchi.annotations.BindingAnnotation
import com.r0adkll.kimchi.annotations.ContributesMultibindingAnnotation
import com.r0adkll.kimchi.util.buildFun
import com.r0adkll.kimchi.util.buildProperty
import com.r0adkll.kimchi.util.ksp.MapKeyValue
import com.r0adkll.kimchi.util.ksp.findBindingType
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
import me.tatarka.inject.annotations.IntoMap
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

fun <A : BindingAnnotation> TypeSpec.Builder.addBinding(
  annotatedElement: AnnotatedElement<A>,
): TypeSpec.Builder {
  val isMultibinding = annotatedElement.annotation is ContributesMultibindingAnnotation
  val mapKey = if (isMultibinding) {
    annotatedElement.mapKey()
  } else {
    null
  }

  val boundType = annotatedElement.findBindingType()
  val isObject = annotatedElement.isObject
  val isBindable = !isObject && annotatedElement.isInjected

  annotatedElement.element.containingFile?.let { addOriginatingKSFile(it) }

  if (mapKey != null) {
    addMappingProvidesFunction(
      boundClass = annotatedElement,
      boundType = boundType,
      mapKey = mapKey,
      isBindable = isBindable,
    )
  } else if (isBindable) {
    addBindingReceiverProperty(
      boundClass = annotatedElement,
      boundType = boundType,
      additionalAnnotations = listIf(isMultibinding, IntoSet::class),
    )
  } else {
    addProvidesFunction(
      boundClass = annotatedElement,
      returnType = boundType,
      additionalAnnotations = listIf(isMultibinding, IntoSet::class),
    )
  }

  return this
}

private fun <A : BindingAnnotation> TypeSpec.Builder.addBindingReceiverProperty(
  boundClass: AnnotatedElement<A>,
  boundType: ClassName,
  additionalAnnotations: List<KClass<*>> = emptyList(),
) {
  addProperty(
    PropertySpec.buildProperty(
      name = "bind${boundType.simpleName}",
      type = boundType,
    ) {
      receiver(boundClass.element.toClassName())

      getter(
        FunSpec.getterBuilder()
          .addAnnotation(Provides::class)
          .apply {
            // Add any qualifier that the bound class has
            boundClass.qualifier()?.let { qualifier ->
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

private fun <A : BindingAnnotation> TypeSpec.Builder.addProvidesFunction(
  boundClass: AnnotatedElement<A>,
  returnType: TypeName,
  additionalAnnotations: List<KClass<*>> = emptyList(),
) {
  addFunction(
    FunSpec.buildFun("provide${boundClass.element.simpleName.asString()}") {
      returns(returnType)

      addAnnotation(Provides::class)

      // Add any qualifiers added to the bound class
      boundClass.qualifier()?.let { qualifier ->
        addAnnotation(qualifier.toAnnotationSpec())
      }

      // Add passed additional annotations
      additionalAnnotations.forEach { annotation ->
        addAnnotation(annotation)
      }

      if (boundClass.isObject) {
        addStatement("return %T", boundClass.element.toClassName())
      } else {
        addStatement("return %T()", boundClass.element.toClassName())
      }
    },
  )
}

private fun <A : BindingAnnotation> TypeSpec.Builder.addMappingProvidesFunction(
  boundClass: AnnotatedElement<A>,
  boundType: ClassName,
  mapKey: MapKeyValue,
  isBindable: Boolean,
) {
  addFunction(
    FunSpec.buildFun("provide${boundType.simpleName}_${mapKey.functionSuffix()}") {
      returns(pairTypeOf(mapKey.type(), boundType))

      addAnnotation(Provides::class)
      addAnnotation(IntoMap::class)

      boundClass.qualifier()?.let { qualifier ->
        addAnnotation(qualifier.toAnnotationSpec())
      }

      if (isBindable) {
        val (format, value) = mapKey.value()
        addParameter("value", boundClass.element.toClassName())
        addStatement("return ($format to value)", value)
      } else {
        val (format, value) = mapKey.value()
        val valueTemplate = if (boundClass.isObject) "%T" else "%T()"
        addStatement("return ($format to $valueTemplate)", value, boundClass.element.toClassName())
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
