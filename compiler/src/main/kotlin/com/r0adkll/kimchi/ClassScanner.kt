// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.r0adkll.kimchi.GeneratedProperty.ReferenceProperty
import com.r0adkll.kimchi.GeneratedProperty.ScopeProperty
import com.r0adkll.kimchi.util.ksp.findActualType
import com.r0adkll.kimchi.util.ksp.hasAnnotation
import com.squareup.kotlinpoet.ClassName
import java.lang.IllegalStateException
import kotlin.reflect.KClass

internal const val HINT_PACKAGE = "kimchi.merge.hint"

internal const val REFERENCE_SUFFIX = "_reference"
internal const val SCOPE_SUFFIX = "_scope"

@OptIn(KspExperimental::class)
class ClassScanner(
  private val resolver: Resolver,
  private val logger: KSPLogger,
) {

  private val contributedCache = mutableMapOf<String, List<ReferenceProperty>>()

  fun findContributedClasses(
    annotation: KClass<*>,
    scope: ClassName,
  ): Sequence<KSClassDeclaration> {
    // Check cache for contributed elements with [annotation] for the given [scope]
    val cachedProperties = contributedCache[scope.canonicalName]
    if (cachedProperties != null && cachedProperties.isNotEmpty()) {
      return cachedProperties
        .mapNotNull { it.classDeclaration }
        .filter { it.hasAnnotation(annotation) }
        .asSequence()
    }

    val propertyGroups = resolver.getDeclarationsFromPackage(HINT_PACKAGE)
      .filterIsInstance<KSPropertyDeclaration>()
      .mapNotNull { property ->
        GeneratedProperty.fromDeclaration(property)
      }
      .groupBy { it.baseName }
      .values

    val scopedReferences = propertyGroups
      .flatMap { properties ->
        val reference = properties
          .filterIsInstance<ReferenceProperty>()
          .singleOrNull()
          ?: throw IllegalStateException("Couldn't find the reference for a generated hint: ${properties[0].baseName}.")

        val scopes = properties
          .filterIsInstance<ScopeProperty>()
          .ifEmpty {
            throw IllegalStateException("Couldn't find any scope for a generated hint: ${properties[0].baseName}.")
          }
          .mapNotNull { it.canonicalScopeName }

        scopes.map { scope ->
          scope to reference
        }
      }.groupBy(
        keySelector = { it.first },
        valueTransform = { it.second },
      )

    contributedCache.clear()
    contributedCache.putAll(scopedReferences)

    return scopedReferences[scope.canonicalName]
      ?.mapNotNull { it.classDeclaration }
      ?.filter { it.hasAnnotation(annotation) }
      ?.asSequence()
      ?: emptySequence()
  }
}

private sealed class GeneratedProperty(
  val declaration: KSPropertyDeclaration,
  val baseName: String,
) {

  class ReferenceProperty(
    declaration: KSPropertyDeclaration,
    baseName: String,
  ) : GeneratedProperty(declaration, baseName) {

    val classDeclaration: KSClassDeclaration? by lazy {
      declaration.type.resolve()
        .arguments.first().type
        ?.findActualType()
    }
  }

  class ScopeProperty(
    declaration: KSPropertyDeclaration,
    baseName: String,
  ) : GeneratedProperty(declaration, baseName) {

    val canonicalScopeName: String? by lazy {
      declaration.type.resolve().arguments.first().type
        ?.findActualType()
        ?.qualifiedName
        ?.asString()
    }
  }

  companion object {
    fun fromDeclaration(declaration: KSPropertyDeclaration): GeneratedProperty? {
      val name = declaration.simpleName.asString()

      return when {
        name.endsWith(REFERENCE_SUFFIX) -> ReferenceProperty(
          declaration,
          name.substringBeforeLast(REFERENCE_SUFFIX),
        )
        name.substringBeforeLast("_").endsWith(SCOPE_SUFFIX) -> ScopeProperty(
          declaration,
          name.substringBeforeLast(SCOPE_SUFFIX),
        )
        else -> null
      }
    }
  }
}
