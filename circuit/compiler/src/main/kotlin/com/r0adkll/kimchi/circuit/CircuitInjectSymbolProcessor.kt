// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.circuit

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.validate
import com.r0adkll.kimchi.annotations.ContributesTo
import com.r0adkll.kimchi.circuit.annotations.CircuitInject
import com.r0adkll.kimchi.circuit.util.ClassNames
import com.r0adkll.kimchi.circuit.util.kotlinpoet.addUiFactoryCreateStatement
import com.r0adkll.kimchi.util.buildFile
import com.r0adkll.kimchi.util.ksp.findAnnotation
import com.r0adkll.kimchi.util.ksp.getAllSymbolsWithAnnotation
import com.r0adkll.kimchi.util.ksp.getScope
import com.r0adkll.kimchi.util.ksp.getScreen
import com.r0adkll.kimchi.util.ksp.hasAnnotation
import com.r0adkll.kimchi.util.toClassName
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.kspDependencies
import com.squareup.kotlinpoet.ksp.writeTo
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

/**
 * Generate the necessary kotlin-inject boilerplate needed to wire up Circuit Ui composables
 * and Presenter implementations to be consumed upstream in a Circuit.Builder
 *
 * TODO: More kdoc here on this symbol processor
 */
class CircuitInjectSymbolProcessor(
  private val env: SymbolProcessorEnvironment,
) : SymbolProcessor {

  @AutoService(SymbolProcessorProvider::class)
  class Provider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
      return CircuitInjectSymbolProcessor(environment)
    }
  }

  override fun process(resolver: Resolver): List<KSAnnotated> {
    val deferred = mutableListOf<KSAnnotated>()

    resolver
      .getAllSymbolsWithAnnotation(CircuitInject::class)
      .forEach { element ->
        if (element.validate()) {
          when (element) {
            is KSFunctionDeclaration -> generateUiFactory(element)
            is KSClassDeclaration -> generatePresenterFactory(element)
            else -> null
          }?.let { fileSpec ->
            fileSpec.writeTo(
              codeGenerator = env.codeGenerator,
              dependencies = fileSpec.kspDependencies(aggregating = false),
            )
          }
        } else {
          deferred += element
        }
      }

    return deferred
  }

  private fun generateUiFactory(
    element: KSFunctionDeclaration,
  ): FileSpec {
    val packageName = element.packageName.asString()
    val classSimpleName = "${element.simpleName.asString()}UiFactory"
    val className = ClassName(packageName, classSimpleName)
    val componentClassName = ClassName(packageName, "${classSimpleName}Component")

    // Verify that this element has @Composable annotation @ first param is state
    // and it
    if (!element.hasAnnotation(ClassNames.Composable)) {
      env.logger.error("Missing @Composable on this function", element)
      throw IllegalStateException("@CircuitInject is only usable on composable functions or Presenter implementations")
    }

    // Get the targeted scope and screen
    val scope = element.findAnnotation(CircuitInject::class)
      ?.getScope()
      ?.toClassName()
      ?: throw IllegalStateException("Unable to find scope to contribute injection to")

    val screen = element.findAnnotation(CircuitInject::class)
      ?.getScreen()
      ?.toClassName()
      ?: throw IllegalStateException("Unable to find screen for injected UI")

    return FileSpec.buildFile(packageName, classSimpleName) {
      addType(
        TypeSpec.interfaceBuilder(componentClassName)
          .addOriginatingKSFile(element.containingFile!!)
          .addAnnotation(
            AnnotationSpec.builder(ContributesTo::class)
              .addMember("%T::class", scope)
              .build(),
          )
          .addFunction(
            FunSpec.builder("bind$classSimpleName")
              .addAnnotation(IntoSet::class)
              .addAnnotation(Provides::class)
              .addParameter("factory", className)
              .returns(ClassNames.Circuit.UiFactory)
              .addStatement("return factory")
              .build(),
          )
          .build(),
      )

      addType(
        TypeSpec.classBuilder(className)
          .addOriginatingKSFile(element.containingFile!!)
          .addAnnotation(Inject::class)
          .addSuperinterface(ClassNames.Circuit.UiFactory)
          .addFunction(
            FunSpec.builder("create")
              .addModifiers(KModifier.OVERRIDE)
              .addParameter("screen", ClassNames.Circuit.Screen)
              .addParameter("context", ClassNames.Circuit.Context)
              .returns(
                ClassNames.Circuit.Ui
                  .parameterizedBy(STAR)
                  .copy(nullable = true),
              )
              .addCode(
                CodeBlock.builder()
                  .beginControlFlow("return when(screen)")
                  .beginControlFlow("is %T ->", screen)
                  .addUiFactoryCreateStatement(
                    element = element,
                    screen = screen,
                  )
                  .endControlFlow()
                  .addStatement("else -> null")
                  .endControlFlow()
                  .build(),
              )
              .build(),
          )
          .build(),
      )
    }
  }

  private fun generatePresenterFactory(element: KSClassDeclaration): FileSpec {
    val packageName = element.packageName.asString()
    val classSimpleName = "${element.simpleName.asString()}Factory"
    val className = ClassName(packageName, classSimpleName)
    val componentClassName = ClassName(packageName, "${classSimpleName}Component")

    // Verify that this element has @Composable annotation @ first param is state
    // and it
    if (!element.hasAnnotation(Inject::class)) {
      env.logger.error("Missing @Inject on this class", element)
      throw IllegalStateException("@CircuitInject on presenter classes must have an @Inject annotation")
    }

    // FIXME: Outdated
    element.primaryConstructor
      ?.parameters
      ?.firstOrNull()
      ?.let { firstParam ->
        require(
          firstParam.type.resolve().declaration.toClassName() == ClassNames.Circuit.Navigator,
        ) {
          "@CircuitInject annotated presenter classes just have an " +
            "@Assisted private val navigator: Navigator as their first param"
        }
      }
      ?: throw IllegalStateException(
        "@CircuitInject annotated presenter classes just have an " +
          "@Assisted private val navigator: Navigator as their first param",
      )

    // Get the targeted scope and screen
    val scope = element.findAnnotation(CircuitInject::class)
      ?.getScope()
      ?.toClassName()
      ?: throw IllegalStateException("Unable to find scope to contribute injection to")

    val screen = element.findAnnotation(CircuitInject::class)
      ?.getScreen()
      ?.toClassName()
      ?: throw IllegalStateException("Unable to find screen for injected UI")

    // Gather all assisted annotated parameters and evaluate providing them
    val allowedAssistedTypes = listOf(
      screen,
      ClassNames.Circuit.Navigator,
      ClassNames.Circuit.Context,
    )

    val assistInjectedParameters = element.primaryConstructor
      ?.parameters
      ?.mapNotNull { parameter ->
        if (parameter.hasAnnotation(Assisted::class)) {
          // Validate that injected type is allowed type
          val parameterTypeClassName = parameter.type.resolve().declaration.toClassName()
          if (parameterTypeClassName in allowedAssistedTypes) {
            ParameterSpec(parameter.name!!.asString(), parameterTypeClassName)
          } else {
            null
          }
        } else {
          null
        }
      }
      ?: emptyList()

    return FileSpec.buildFile(packageName, classSimpleName) {
      addType(
        TypeSpec.interfaceBuilder(componentClassName)
          .addOriginatingKSFile(element.containingFile!!)
          .addAnnotation(
            AnnotationSpec.builder(ContributesTo::class)
              .addMember("%T::class", scope)
              .build(),
          )
          .addFunction(
            FunSpec.builder("bind$classSimpleName")
              .addAnnotation(IntoSet::class)
              .addAnnotation(Provides::class)
              .addParameter("factory", className)
              .returns(ClassNames.Circuit.PresenterFactory)
              .addStatement("return factory")
              .build(),
          )
          .build(),
      )

      val factoryLambda = LambdaTypeName.get(
        parameters = assistInjectedParameters,
        returnType = element.toClassName(),
      )

      addType(
        TypeSpec.classBuilder(classSimpleName)
          .addOriginatingKSFile(element.containingFile!!)
          .addAnnotation(Inject::class)
          .addSuperinterface(ClassNames.Circuit.PresenterFactory)
          .primaryConstructor(
            FunSpec.constructorBuilder()
              .addParameter("presenterFactory", factoryLambda)
              .build(),
          )
          .addProperty(
            PropertySpec.builder("presenterFactory", factoryLambda)
              .initializer("presenterFactory")
              .addModifiers(KModifier.PRIVATE)
              .build(),
          )
          .addFunction(
            FunSpec.builder("create")
              .addModifiers(KModifier.OVERRIDE)
              .addParameter("screen", ClassNames.Circuit.Screen)
              .addParameter("navigator", ClassNames.Circuit.Navigator)
              .addParameter("context", ClassNames.Circuit.Context)
              .returns(
                ClassNames.Circuit.Presenter
                  .parameterizedBy(STAR)
                  .copy(nullable = true),
              )
              .addCode(
                CodeBlock.builder()
                  .beginControlFlow("return when(screen)")
                  .addStatement(
                    "is %T -> presenterFactory(${assistInjectedParameters.joinToString { it.name }})",
                    screen,
                  )
                  .addStatement("else -> null")
                  .endControlFlow()
                  .build(),
              )
              .build(),
          )
          .build(),
      )
    }
  }
}
