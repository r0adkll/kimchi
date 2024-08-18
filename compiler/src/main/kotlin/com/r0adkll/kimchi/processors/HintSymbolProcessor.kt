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
import com.r0adkll.kimchi.util.ksp.getSymbolsWithClassAnnotation
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
  private val env: SymbolProcessorEnvironment,
  private val hintPackageName: String,
) : SymbolProcessor {

  /**
   * Define the annotation to scan and generate hints for
   */
  abstract val annotation: KClass<*>

  /**
   * Define how to determine the targeted scope of the hint
   * @param element the [KSClassDeclaration] of the class annotated with [annotation]
   * @return the [ClassName] of the scope this hint will target
   */
  abstract fun getScope(element: KSClassDeclaration): ClassName

  /**
   * Validate that the [element] annotated with this hint generator
   * can actually be contributed
   */
  open fun validate(element: KSClassDeclaration) = Unit

  override fun process(resolver: Resolver): List<KSAnnotated> {
    resolver.getSymbolsWithClassAnnotation(annotation)
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
    val fileName = element.simpleName.asString()
    val className = element.toClassName()
    val propertyName = element.qualifiedName!!.asString()
      .replace(".", "_")

    validate(element)

    val scope = getScope(element)

    return FileSpec.buildFile(hintPackageName, fileName) {
      // Reference Hint
      addProperty(
        PropertySpec
          .builder(
            name = propertyName + REFERENCE_SUFFIX,
            type = KClass::class.asClassName().parameterizedBy(className),
          )
          .initializer("%T::class", className)
          .addModifiers(KModifier.PUBLIC)
          .build(),
      )

      // Scope Hint
      addProperty(
        PropertySpec
          .builder(
            name = propertyName + SCOPE_SUFFIX,
            type = KClass::class.asClassName().parameterizedBy(scope),
          )
          .initializer("%T::class", scope)
          .addModifiers(KModifier.PUBLIC)
          .build(),
      )
    }
  }
}
