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
import com.r0adkll.kimchi.annotations.ContributesBinding
import com.r0adkll.kimchi.annotations.ContributesMultibinding
import com.r0adkll.kimchi.annotations.ContributesSubcomponent
import com.r0adkll.kimchi.annotations.ContributesTo
import com.r0adkll.kimchi.util.ksp.findActualType
import com.squareup.kotlinpoet.ClassName
import java.lang.IllegalStateException
import kotlin.reflect.KClass

internal const val HINT_PREFIX = "kimchi.merge.hint"
internal const val HINT_CONTRIBUTES_PACKAGE = "$HINT_PREFIX.merge"
internal const val HINT_BINDING_PACKAGE = "$HINT_PREFIX.binding"
internal const val HINT_MULTIBINDING_PACKAGE = "$HINT_PREFIX.multibinding"
internal const val HINT_SUBCOMPONENT_PACKAGE = "$HINT_PREFIX.subcomponent"

internal const val REFERENCE_SUFFIX = "_reference"
internal const val SCOPE_SUFFIX = "_scope"

@OptIn(KspExperimental::class)
class ClassScanner(
  private val resolver: Resolver,
  private val logger: KSPLogger,
) {

  /**
   * TODO: Add some form of caching for this
   */
  fun findContributedClasses(
    annotation: KClass<*>,
    scope: ClassName,
  ): Sequence<KSClassDeclaration> {
    val packageName = when (annotation) {
      ContributesBinding::class -> HINT_BINDING_PACKAGE
      ContributesMultibinding::class -> HINT_MULTIBINDING_PACKAGE
      ContributesTo::class -> HINT_CONTRIBUTES_PACKAGE
      ContributesSubcomponent::class -> HINT_SUBCOMPONENT_PACKAGE
      else -> throw IllegalArgumentException("Unrecognized annotation to look up")
    }

    val propertyGroups = resolver.getDeclarationsFromPackage(packageName)
      .filterIsInstance<KSPropertyDeclaration>()
      .mapNotNull { property ->
        GeneratedProperty.fromDeclaration(property)
      }
      .groupBy { it.baseName }
      .values

    return propertyGroups
      .asSequence()
      .mapNotNull { properties ->
        val reference = properties
          .filterIsInstance<ReferenceProperty>()
          .singleOrNull()
          ?: throw IllegalStateException("Couldn't find the reference for a generated hint: ${properties[0].baseName}.")

        val scopes = properties
          .filterIsInstance<ScopeProperty>()
          .ifEmpty {
            throw IllegalStateException("Couldn't find any scope for a generated hint: ${properties[0].baseName}.")
          }
          .mapNotNull {
            it.declaration.type.resolve().arguments.first().type
              ?.findActualType()
              ?.qualifiedName
              ?.asString()
          }

        // Look for the right scope even before resolving the class and resolving all its super
        // types.
        if (scope.canonicalName !in scopes) return@mapNotNull null

        reference
          .declaration.type.resolve()
          .arguments.first().type
          ?.findActualType()
      }
  }
}

private sealed class GeneratedProperty(
  val declaration: KSPropertyDeclaration,
  val baseName: String,
) {

  class ReferenceProperty(
    declaration: KSPropertyDeclaration,
    baseName: String,
  ) : GeneratedProperty(declaration, baseName)

  class ScopeProperty(
    declaration: KSPropertyDeclaration,
    baseName: String,
  ) : GeneratedProperty(declaration, baseName)

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
