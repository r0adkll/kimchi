// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.util.ksp

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueArgument
import com.r0adkll.kimchi.annotations.MapKey
import com.r0adkll.kimchi.util.KimchiException
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import kotlin.reflect.KClass

/**
 * Find the map key value included in [com.r0adkll.kimchi.annotations.ContributesMultibinding]
 * usage to use as the key when generating the bindings on the graph
 */
fun KSClassDeclaration.findMapKey(): MapKeyValue? {
  val mapKeyAnnotation = annotations.find { annotation ->
    annotation.isAnnotationOf(MapKey::class)
  } ?: return null

  val mapKeyArgument = mapKeyAnnotation.arguments
    .firstOrNull()
    ?: error("MapKey's must define a single argument to use as the value of the key.")

  return MapKeyValue(mapKeyArgument)
}

fun pairTypeOf(vararg typeNames: TypeName): ParameterizedTypeName {
  return Pair::class.asTypeName()
    .parameterizedBy(*typeNames)
}

data class MapKeyValue(private val valueArgument: KSValueArgument) {

  fun functionSuffix(): String {
    val value = valueArgument.value ?: throw KimchiException(
      "Unable to determine the MapKey value: ${valueArgument.name?.asString()} = ${valueArgument.value}",
    )

    return when (value) {
      is Byte -> "${value}b"
      is Short -> "${value}s"

      is Int,
      is Long,
      is Char,
      is String,
      is Boolean,
      -> value.toString()

      is Float,
      is Double,
      -> value.toString().replace(".", "_")

      is KSType -> value.declaration.simpleName.asString()
      is KSClassDeclaration -> value.safeRequiredQualifiedName
      else -> throw KimchiException(
        "Unable to determine the MapKey name: ${valueArgument.name?.asString()} = ${valueArgument.value}",
      )
    }
  }

  fun type(): TypeName {
    val value = valueArgument.value ?: throw KimchiException(
      "Unable to determine the MapKey type: ${valueArgument.name?.asString()} = ${valueArgument.value}",
    )
    return when (value) {
      is Byte -> Byte::class.asTypeName()
      is Short -> Short::class.asTypeName()
      is Int -> Int::class.asTypeName()
      is Long -> Long::class.asTypeName()
      is Float -> Float::class.asTypeName()
      is Double -> Double::class.asTypeName()
      is Char -> Char::class.asTypeName()
      is String -> String::class.asTypeName()
      is Boolean -> Boolean::class.asTypeName()

      is KSType -> KClass::class.asTypeName().parameterizedBy(STAR)
      is KSClassDeclaration -> when (value.classKind) {
        ClassKind.ENUM_CLASS -> value.toClassName()
        ClassKind.ENUM_ENTRY -> (value.parent as? KSClassDeclaration)?.toClassName()
          ?: throw KimchiException(
            "Unsupported map key detected",
            value,
          )
        else -> throw KimchiException(
          "Unsupported map key detected",
          value,
        )
      }
      else -> throw KimchiException(
        "Unable to determine the map key type",
        valueArgument,
      )
    }
  }

  fun value(): Pair<String, Any> {
    val value = valueArgument.value ?: throw KimchiException(
      "Unable to determine the MapKey value: ${valueArgument.name?.asString()} = ${valueArgument.value}",
    )

    val format = when (value) {
      is Byte,
      is Short,
      is Int,
      is Long,
      is Float,
      is Double,
      is Boolean,
      -> "%L"

      is Char -> "'%L'"
      is String -> "%S"

      is KSType -> "%T::class"
      is KSClassDeclaration -> "%L"
      else -> throw KimchiException(
        "Unable to determine the MapKey value: ${valueArgument.name?.asString()} = ${valueArgument.value}",
      )
    }

    val argValue = when (value) {
      is Byte -> "$value.toByte()"
      is Short -> "$value.toShort()"
      is Float -> "$value.toFloat()"

      is Int,
      is Long,
      is Double,
      is Boolean,
      is Char,
      is String,
      -> value

      is KSType -> value.toTypeName()
      is KSClassDeclaration -> value
      else -> throw KimchiException(
        "Unable to determine the MapKey value: ${valueArgument.name?.asString()} = ${valueArgument.value}",
      )
    }

    return format to argValue
  }
}
