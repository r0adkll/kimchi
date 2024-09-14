// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.processors

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.r0adkll.kimchi.REFERENCE_SUFFIX
import com.r0adkll.kimchi.SCOPE_SUFFIX
import com.r0adkll.kimchi.util.buildFile
import com.r0adkll.kimchi.util.ksp.asUrlSafeString
import com.r0adkll.kimchi.util.ksp.requireQualifiedName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import kotlin.reflect.KClass

/**
 * Base [SymbolProcessor] for generating hints that can be scanned and processed later
 * by the merging component generator
 */
internal abstract class HintSymbolProcessor(
  protected val env: SymbolProcessorEnvironment,
  private val hintPackageName: String,
) : SymbolProcessor {

  /**
   * Define the annotation to scan and generate hints for
   */
  abstract val annotation: KClass<*>

  /**
   * Define how to determine the targeted scopes of the hint
   * @param element the [KSClassDeclaration] of the class annotated with [annotation]s
   * @return the list [ClassName] of all the scopes this hint will target
   */
  abstract fun getScopes(element: KSClassDeclaration): Set<ClassName>

  /**
   * Validate that the [element] annotated with this hint generator
   * can actually be contributed
   */
  open fun validate(element: KSClassDeclaration) = Unit

  override fun process(resolver: Resolver): List<KSAnnotated> {
    resolver
      .getSymbolsWithAnnotation(annotation.qualifiedName!!)
      .filterIsInstance<KSClassDeclaration>()
      .forEach { element ->
        process(element).writeTo(
          codeGenerator = env.codeGenerator,
          aggregating = false,
          originatingKSFiles = listOf(element.containingFile!!),
        )
      }

    return emptyList()
  }

  private fun process(
    element: KSClassDeclaration,
  ): FileSpec {
    // TODO: In other examples using the fq name for file names has been known to hit length limits
    //  so we should look into using a hash instead.
    val fileName = element.requireQualifiedName.asUrlSafeString()
    val className = element.toClassName()

    validate(element)

    val scopes = getScopes(element)

    return FileSpec.buildFile(hintPackageName, fileName) {
      // Reference Hint
      addProperty(
        PropertySpec
          .builder(
            name = fileName + REFERENCE_SUFFIX,
            type = KClass::class.asClassName().parameterizedBy(className),
          )
          .initializer("%T::class", className)
          .addModifiers(KModifier.PUBLIC)
          .build(),
      )

      // Scope(s) Hint
      scopes.forEachIndexed { index, scope ->
        addProperty(
          PropertySpec
            .builder(
              name = "${fileName}${SCOPE_SUFFIX}_$index",
              type = KClass::class.asClassName().parameterizedBy(scope),
            )
            .initializer("%T::class", scope)
            .addModifiers(KModifier.PUBLIC)
            .build(),
        )
      }
    }
  }
}
