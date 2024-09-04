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
import com.r0adkll.kimchi.circuit.annotations.CircuitInjectAnnotation
import com.r0adkll.kimchi.circuit.util.ClassNames
import com.r0adkll.kimchi.circuit.util.MemberNames
import com.r0adkll.kimchi.circuit.util.addUiFactoryCreateStatement
import com.r0adkll.kimchi.util.KimchiException
import com.r0adkll.kimchi.util.applyIf
import com.r0adkll.kimchi.util.buildConstructor
import com.r0adkll.kimchi.util.buildFile
import com.r0adkll.kimchi.util.capitalized
import com.r0adkll.kimchi.util.kotlinpoet.toParameterSpec
import com.r0adkll.kimchi.util.ksp.directReturnTypeIs
import com.r0adkll.kimchi.util.ksp.hasAnnotation
import com.r0adkll.kimchi.util.ksp.implements
import com.r0adkll.kimchi.util.ksp.returnTypeIs
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
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.kspDependencies
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

/**
 * Generate the necessary kotlin-inject boilerplate needed to wire up Circuit Ui composables
 * and Presenter implementations to be consumed upstream in a Circuit.Builder
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
      .getSymbolsWithAnnotation(CircuitInject::class.qualifiedName!!)
      .forEach { element ->
        if (element.validate()) {
          when (element) {
            is KSFunctionDeclaration -> when {
              element.directReturnTypeIs(Unit::class) -> generateUiFunctionFactory(element)
              element.returnTypeIs(ClassNames.Circuit.UiState) -> generatePresenterFunctionFactory(element)
              else -> null
            }
            is KSClassDeclaration -> when {
              element.implements(ClassNames.Circuit.Ui) -> generateUiFactory(element)
              element.implements(ClassNames.Circuit.Presenter) -> generatePresenterFactory(element)
              else -> null
            }
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

  private fun generateUiFunctionFactory(element: KSFunctionDeclaration): FileSpec {
    val packageName = element.packageName.asString()
    val classSimpleName = "${element.simpleName.asString()}UiFactory"
    val className = ClassName(packageName, classSimpleName)
    val componentClassName = ClassName(packageName, "${classSimpleName}Component")
    val annotation = CircuitInjectAnnotation.from(element)

    // Verify that this element has @Composable annotation @ first param is state
    // and it
    if (!element.hasAnnotation(ClassNames.Composable)) {
      throw KimchiException("@CircuitInject Ui functions must be annotated with @Composable", element)
    }

    // Verify that the function return type IS Unit
    if (element.returnType?.toTypeName() != Unit::class.asTypeName()) {
      throw KimchiException("Circuit Ui functions can only return 'Unit'", element)
    }

    // These are the list of parameter types that can be resolved via a Ui.Factory, where
    // any other parameters must be injected
    val assistedParameterTypes = listOf(
      annotation.screen,
      ClassNames.Circuit.UiState,
      ClassNames.Modifier,
    )

    // Filter out all the parameters that will need to be injected into the factory
    val injectParameters = element.parameters.filter { param ->
      assistedParameterTypes.none { param.implements(it) }
    }

    return FileSpec.buildFile(packageName, classSimpleName) {
      addType(
        TypeSpec.interfaceBuilder(componentClassName)
          .addOriginatingKSFile(element.containingFile!!)
          .addAnnotation(
            AnnotationSpec.builder(ContributesTo::class)
              .addMember("%T::class", annotation.scope)
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
          .applyIf(injectParameters.isNotEmpty()) {
            primaryConstructor(
              FunSpec.buildConstructor {
                injectParameters.forEach { param ->
                  addParameter(param.toParameterSpec())

                  // Ensure that the params get set as properties and in the primary constructor
                  addProperty(
                    PropertySpec.builder(param.name!!.asString(), param.type.toTypeName())
                      .initializer(param.name!!.asString())
                      .addModifiers(KModifier.PRIVATE)
                      .build(),
                  )
                }
              },
            )
          }
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
                  .beginControlFlow("is %T ->", annotation.screen)
                  .addUiFactoryCreateStatement(
                    element = element,
                    screen = annotation.screen,
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

  private fun generateUiFactory(element: KSClassDeclaration): FileSpec {
    val packageName = element.packageName.asString()
    val classSimpleName = "${element.simpleName.asString()}Factory"
    val className = ClassName(packageName, classSimpleName)
    val componentClassName = ClassName(packageName, "${classSimpleName}Component")
    val annotation = CircuitInjectAnnotation.from(element)

    // Gather all assisted annotated parameters and evaluate providing them
    val allowedAssistedTypes = listOf(
      annotation.screen,
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

    val factoryLambda = LambdaTypeName.get(
      parameters = assistInjectedParameters,
      returnType = element.toClassName(),
    )

    return FileSpec.buildFile(packageName, classSimpleName) {
      addType(
        TypeSpec.interfaceBuilder(componentClassName)
          .addOriginatingKSFile(element.containingFile!!)
          .addAnnotation(
            AnnotationSpec.builder(ContributesTo::class)
              .addMember("%T::class", annotation.scope)
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
          .primaryConstructor(
            FunSpec.constructorBuilder()
              .addParameter("uiFactory", factoryLambda)
              .build(),
          )
          .addProperty(
            PropertySpec.builder("uiFactory", factoryLambda)
              .initializer("uiFactory")
              .addModifiers(KModifier.PRIVATE)
              .build(),
          )
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
                  .addStatement(
                    "is %T -> uiFactory(${assistInjectedParameters.joinToString { it.name }})",
                    annotation.screen,
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

  private fun generatePresenterFunctionFactory(element: KSFunctionDeclaration): FileSpec {
    val packageName = element.packageName.asString()
    val classSimpleName = "${element.simpleName.asString().capitalized()}Factory"
    val className = ClassName(packageName, classSimpleName)
    val componentClassName = ClassName(packageName, "${classSimpleName}Component")
    val annotation = CircuitInjectAnnotation.from(element)

    // Verify that this element has @Composable annotation
    if (!element.hasAnnotation(ClassNames.Composable)) {
      throw KimchiException("@Composable is required on the presenter function, ${element.simpleName.asString()}")
    }

    // Validate that the return type implements CircuitUiState
    if (!element.returnTypeIs(ClassNames.Circuit.UiState)) {
      throw KimchiException("Annotated presenter functions must return a class that implements CircuitUiState", element)
    }

    return FileSpec.buildFile(packageName, classSimpleName) {
      addType(
        TypeSpec.interfaceBuilder(componentClassName)
          .addOriginatingKSFile(element.containingFile!!)
          .addAnnotation(
            AnnotationSpec.builder(ContributesTo::class)
              .addMember("%T::class", annotation.scope)
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

      addType(
        TypeSpec.classBuilder(className)
          .addOriginatingKSFile(element.containingFile!!)
          .addAnnotation(Inject::class)
          .addSuperinterface(ClassNames.Circuit.PresenterFactory)
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
                  .beginControlFlow("is %T ->", annotation.screen)
                  .addStatement(
                    "%M { %T() }",
                    MemberNames.CircuitPresenterOf,
                    element.toClassName(),
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
    val annotation = CircuitInjectAnnotation.from(element)

    // Verify that this element has @Composable annotation @ first param is state
    // and it
    if (!element.hasAnnotation(Inject::class)) {
      throw KimchiException("@CircuitInject on presenter classes must have an @Inject annotation", element)
    }

    // Gather all assisted annotated parameters and evaluate providing them
    val allowedAssistedTypes = listOf(
      annotation.screen,
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
              .addMember("%T::class", annotation.scope)
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
                    annotation.screen,
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
