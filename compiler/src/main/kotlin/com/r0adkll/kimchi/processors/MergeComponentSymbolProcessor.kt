// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.processors

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.r0adkll.kimchi.ClassScanner
import com.r0adkll.kimchi.annotations.ContributesBinding
import com.r0adkll.kimchi.annotations.ContributesMultibinding
import com.r0adkll.kimchi.annotations.ContributesSubcomponent
import com.r0adkll.kimchi.annotations.ContributesTo
import com.r0adkll.kimchi.annotations.MergeComponent
import com.r0adkll.kimchi.util.addIfNonNull
import com.r0adkll.kimchi.util.buildClass
import com.r0adkll.kimchi.util.buildFile
import com.r0adkll.kimchi.util.kotlinpoet.addBinding
import com.r0adkll.kimchi.util.kotlinpoet.parameterSpecs
import com.r0adkll.kimchi.util.ksp.SubcomponentDeclaration
import com.r0adkll.kimchi.util.ksp.findAnnotation
import com.r0adkll.kimchi.util.ksp.getScope
import com.r0adkll.kimchi.util.ksp.getSymbolsWithClassAnnotation
import com.r0adkll.kimchi.util.ksp.isInterface
import com.r0adkll.kimchi.util.toClassName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.kspDependencies
import com.squareup.kotlinpoet.ksp.toAnnotationSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import me.tatarka.inject.annotations.Component

internal class MergeComponentSymbolProcessor(
  private val env: SymbolProcessorEnvironment,
) : SymbolProcessor {

  @AutoService(SymbolProcessorProvider::class)
  class Provider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
      return MergeComponentSymbolProcessor(environment)
    }
  }

  private val isGenerateCompanionExtensionsEnabled: Boolean
    get() = env.options["me.tatarka.inject.generateCompanionExtensions"] == "true"

  private var deferred: MutableList<KSClassDeclaration> = mutableListOf()

  override fun process(resolver: Resolver): List<KSAnnotated> {
    val classScanner = ClassScanner(resolver, env.logger)

    val previousDiffered = deferred
    deferred = mutableListOf()

    // Process previously deferred symbols
    for (element in previousDiffered) {
      process(classScanner, element).let { fileSpec ->
        fileSpec.writeTo(
          codeGenerator = env.codeGenerator,
          dependencies = fileSpec.kspDependencies(aggregating = true),
        )
      }
    }

    // Scan for new annotations to be processed
    resolver
      .getSymbolsWithClassAnnotation(MergeComponent::class)
      .forEach { element ->
        // If there are still new files being generated defer the processing of
        // this to be last so that we can make sure we pick up hints being generated in the
        // same module
        if (resolver.getNewFiles().any()) {
          deferred += element
        } else if (element.validate()) {
          process(classScanner, element).let { fileSpec ->
            fileSpec.writeTo(
              codeGenerator = env.codeGenerator,
              dependencies = fileSpec.kspDependencies(aggregating = true),
            )
          }
        } else {
          deferred += element
        }
      }

    return deferred
  }

  private fun process(
    classScanner: ClassScanner,
    element: KSClassDeclaration,
  ): FileSpec {
    val packageName = "kimchi.merge.${element.packageName.asString()}"
    val classSimpleName = "Merged${element.simpleName.asString()}"
    val className = ClassName(packageName, classSimpleName)

    return FileSpec.buildFile(packageName, classSimpleName) {
      // Recursively Generate a component and its contributed subcomponents
      addType(generateComponent(classScanner, packageName, element))

      // Create Companion extension method on original element to create this component
      val constructorParameters = getConstructorParameters(element)
      val createFunction = if (isGenerateCompanionExtensionsEnabled) {
        "%T.create"
      } else {
        "%T::class.create"
      }
      addFunction(
        FunSpec.builder("create$classSimpleName")
          .receiver(element.toClassName().nestedClass("Companion"))
          .addParameters(constructorParameters)
          .addStatement(
            "return $createFunction(${constructorParameters.joinToString { "%L" }})",
            className,
            *constructorParameters.map { it.name }.toTypedArray(),
          )
          .returns(className)
          .build(),
      )
    }
  }

  private fun FileSpec.Builder.generateComponent(
    classScanner: ClassScanner,
    packageName: String,
    element: KSClassDeclaration,
    parent: ClassName? = null,
  ): TypeSpec {
    val classSimpleName = "Merged${element.simpleName.asString()}"
    val className = parent?.nestedClass(classSimpleName)
      ?: ClassName(packageName, classSimpleName)
    val isSubcomponent: Boolean = parent != null

    val annotationKlass = if (isSubcomponent) ContributesSubcomponent::class else MergeComponent::class
    val scope = element.findAnnotation(annotationKlass)
      ?.getScope()
      ?.toClassName()
      ?: throw IllegalArgumentException("Unable to find scope for ${element.simpleName}")

    // Pull the contributed components for the scope
    val subcomponents = classScanner.findContributedClasses(
      annotation = ContributesSubcomponent::class,
      scope = scope,
    ).map { SubcomponentDeclaration(it) }

    val modules = classScanner.findContributedClasses(
      annotation = ContributesTo::class,
      scope = scope,
    )

    val bindings = classScanner.findContributedClasses(
      annotation = ContributesBinding::class,
      scope = scope,
    )

    val multiBindings = classScanner.findContributedClasses(
      annotation = ContributesMultibinding::class,
      scope = scope,
    )

    // Build the kotlin poet code
    return TypeSpec.buildClass(classSimpleName) {
      // Add this original file + contributed modules to the metadata
      // for incremental processing
      element.containingFile
        ?.let { addOriginatingKSFile(it) }
      modules
        .mapNotNull { it.containingFile }
        .forEach { addOriginatingKSFile(it) }
      subcomponents
        .mapNotNull { it.containingFile }
        .forEach { addOriginatingKSFile(it) }

      addModifiers(KModifier.ABSTRACT)

      // Mark our generated class as the component to be generated
      addAnnotation(Component::class)

      // Setup the constructor / superclass
      if (element.isInterface) {
        addSuperinterface(element.toClassName())
      } else {
        superclass(element.toClassName())
      }

      // If we are generating a subcomponent, then parse the underlying component constructor params
      // from its defined factory class and function.
      val constructorParams = if (isSubcomponent) {
        val subcomponent = SubcomponentDeclaration(element)
        subcomponent.factoryClass.factoryFunction.parameterSpecs()
      } else {
        getConstructorParameters(element)
      }

      // If this is a subcomponent, i.e it has a parent,
      // then we need to add it's parent as an @Component parameter, but
      // not add it to the superclass constructor params
      val parentParameter = if (parent != null) {
        // Add the initializer property to the class to properly create the
        // constructor parent component reference.
        addProperty(
          PropertySpec.builder("parent", parent)
            .initializer("parent")
            .build(),
        )

        ParameterSpec.builder("parent", parent)
          .addAnnotation(Component::class)
          .build()
      } else {
        null
      }

      primaryConstructor(
        FunSpec.constructorBuilder()
          .addParameters(constructorParams addIfNonNull parentParameter)
          .build(),
      )

      // Subcomponents currently have a hard restriction on being interfaces with using a Factory
      // to define how the merged component implements its constructor parameters. So we can just
      // skip adding superclass constructor params in this case.
      if (!isSubcomponent) {
        constructorParams.map { it.name }.forEach {
          addSuperclassConstructorParameter("%L", it)
        }
      }

      // Add all the contributed interfaces
      addSuperinterfaces(
        modules
          .map { it.toClassName() }
          .toList(),
      )

      // Generate all the contributed bindings
      bindings.forEach { binding ->
        addBinding(binding, ContributesBinding::class)
      }

      // Generate all the contributed multi-bindings
      multiBindings.forEach { multiBinding ->
        addBinding(multiBinding, ContributesMultibinding::class)
      }

      // Now iterate through all the subcomponents, and add them
      subcomponents.forEach { subcomponent ->
        // Add this subcomponents factory to the parent
        addSuperinterface(subcomponent.factoryClass.toClassName())

        // Generate the factory creation function overload to generate this subcomponent
        addFunction(subcomponent.createFactoryFunctionOverload(isGenerateCompanionExtensionsEnabled))

        // Generate the Subcomponent
        addType(
          generateComponent(
            classScanner = classScanner,
            packageName = packageName,
            element = subcomponent,
            parent = className,
          ),
        )
      }

      // Every component should have an empty companion object
      addType(
        TypeSpec.companionObjectBuilder()
          .build(),
      )
    }
  }

  private fun getConstructorParameters(element: KSClassDeclaration): List<ParameterSpec> {
    return element.primaryConstructor?.let { primaryConstructor ->
      primaryConstructor.parameters.map { param ->
        ParameterSpec.builder(
          name = param.name!!.asString(),
          type = param.type.toTypeName(),
        )
          .addAnnotations(
            param.annotations
              .map { annotation -> annotation.toAnnotationSpec() }
              .toList(),
          )
          .build()
      }
    } ?: emptyList()
  }
}
