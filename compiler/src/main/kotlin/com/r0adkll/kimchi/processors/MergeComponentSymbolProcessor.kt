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
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.validate
import com.r0adkll.kimchi.ClassScanner
import com.r0adkll.kimchi.annotations.ContributesBinding
import com.r0adkll.kimchi.annotations.ContributesBindingAnnotation
import com.r0adkll.kimchi.annotations.ContributesMultibinding
import com.r0adkll.kimchi.annotations.ContributesMultibindingAnnotation
import com.r0adkll.kimchi.annotations.ContributesSubcomponent
import com.r0adkll.kimchi.annotations.ContributesTo
import com.r0adkll.kimchi.annotations.ContributesToAnnotation
import com.r0adkll.kimchi.annotations.MergeComponent
import com.r0adkll.kimchi.annotations.MergingAnnotation
import com.r0adkll.kimchi.util.KimchiException
import com.r0adkll.kimchi.util.addIfNonNull
import com.r0adkll.kimchi.util.buildClass
import com.r0adkll.kimchi.util.buildFile
import com.r0adkll.kimchi.util.kotlinpoet.addBinding
import com.r0adkll.kimchi.util.kotlinpoet.toParameterSpec
import com.r0adkll.kimchi.util.ksp.SubcomponentDeclaration
import com.r0adkll.kimchi.util.ksp.findBindingTypeFor
import com.r0adkll.kimchi.util.ksp.findInjectScope
import com.r0adkll.kimchi.util.ksp.findQualifier
import com.r0adkll.kimchi.util.ksp.hasAnnotation
import com.r0adkll.kimchi.util.ksp.isInterface
import com.r0adkll.kimchi.util.toClassName
import com.squareup.kotlinpoet.AnnotationSpec
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
import me.tatarka.inject.annotations.Provides

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

  override fun process(resolver: Resolver): List<KSAnnotated> {
    val classScanner = ClassScanner(resolver, env.logger)
    val deferred = mutableListOf<KSClassDeclaration>()

    // Scan new files for contributing annotations
    val anyNewContributed = resolver.getNewFiles()
      .flatMap { it.declarations }
      .filterIsInstance<KSClassDeclaration>()
      .any {
        it.hasAnnotation(ContributesBinding::class) ||
          it.hasAnnotation(ContributesMultibinding::class) ||
          it.hasAnnotation(ContributesTo::class) ||
          it.hasAnnotation(ContributesSubcomponent::class)
      }

    // Scan for new annotations to be processed
    resolver
      .getSymbolsWithAnnotation(MergeComponent::class.qualifiedName!!)
      .filterIsInstance<KSClassDeclaration>()
      .forEach { element ->
        // If there are still new files that are contributing then defer the processing of
        // this to later so that we can make sure we pick up hints being generated in the
        // same module
        if (anyNewContributed) {
          env.logger.info("New contributions still being generated, deferring ${element.qualifiedName?.asString()}")
          deferred += element
        } else if (element.validate()) {
          process(classScanner, element).let { fileSpec ->
            fileSpec.writeTo(
              codeGenerator = env.codeGenerator,
              dependencies = fileSpec.kspDependencies(aggregating = true),
            )
          }
        } else {
          env.logger.warn("Unable to validate, deferring ${element.qualifiedName?.asString()}")
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
        FunSpec.builder("create${element.simpleName.asString()}")
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

    val mergingAnnotation = MergingAnnotation.from(element)

    // Pull the contributed components for the scope
    val subcomponents = classScanner.findContributedClasses(
      annotation = ContributesSubcomponent::class,
      scope = mergingAnnotation.scope,
    ).filterNot(mergingAnnotation::excludes)
      .map { SubcomponentDeclaration(it) }

    // Subcomponents can only replace other subcomponents so collect the set
    // of replaced subcomponents so we can filter them later from being merged
    // into the final component
    val subcomponentsReplaced = subcomponents
      .flatMap { it.annotation.replaces }

    val modules = classScanner.findContributedClasses(
      annotation = ContributesTo::class,
      scope = mergingAnnotation.scope,
    ).filterNot(mergingAnnotation::excludes)

    val bindings = classScanner.findContributedClasses(
      annotation = ContributesBinding::class,
      scope = mergingAnnotation.scope,
    ).filterNot(mergingAnnotation::excludes)

    val multiBindings = classScanner.findContributedClasses(
      annotation = ContributesMultibinding::class,
      scope = mergingAnnotation.scope,
    ).filterNot(mergingAnnotation::excludes)

    // Bindings and modules can replace each other so collect them all to use to filter
    // each other out later in the final component
    val replaced = sequence {
      yieldAll(modules.map(ContributesToAnnotation::from))
      yieldAll(bindings.map(ContributesBindingAnnotation::from))
      yieldAll(multiBindings.map(ContributesMultibindingAnnotation::from))
    }.flatMap { it.replaces }

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

      // Pass along any scope that was attached to the component
      element.findInjectScope()?.let { scopeAnnotation ->
        addAnnotation(scopeAnnotation.toAnnotationSpec())
      }

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
        subcomponent.factoryClass.factoryFunction.parameters.map {
          // Add initializer property to make this parameter a `val` in the constructor
          addProperty(
            PropertySpec.builder(it.name!!.asString(), it.type.toTypeName())
              .initializer(it.name!!.asString())
              .build(),
          )

          it.toParameterSpec {
            // Add an @get:Provides annotation to provide this parameter to the
            // dependency graph
            addAnnotation(
              AnnotationSpec.builder(Provides::class)
                .useSiteTarget(AnnotationSpec.UseSiteTarget.GET)
                .build(),
            )
          }
        }
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
          .filterNot { replaced.contains(it) }
          .toList(),
      )

      // Generate all the contributed bindings
      bindings
        .filterNot { replaced.contains(it.toClassName()) }
        .groupBy { binding ->
          // Qualifiers would make a binding unique, so be sure to account for it
          val qualifier = binding.findQualifier()?.toAnnotationSpec()?.toString()
          val boundType = binding.findBindingTypeFor(ContributesBinding::class).toClassName()
          "$boundType${(qualifier?.let { "_$it" } ?: "")}"
        }
        .forEach { (_, group) ->
          if (group.size == 1) {
            addBinding(group.first(), ContributesBinding::class)
          } else {
            // Check if there are multiple in the group with the same rank
            val rankSet = group
              .map { ContributesBindingAnnotation.from(it).rank }
              .toSet()

            // We could probably have a little more grace in this check where we check if ALL have the same
            // rank, but it seems easier to just fail if ANY duplicate bindings have the same rank, even
            // if say a third had a higher rank (which would really mask an underlying issue if that 3rd was removed)
            if (rankSet.size != group.size) {
              throw KimchiException(
                """Multiple @ContributesBinding for the same boundType with the same rank were found:
                  ${group.joinToString("\n") { "- ${it.qualifiedName!!.asString()}" }}
                """.trimIndent(),
              )
            } else {
              val maxRankedBinding = group.maxBy { ContributesBindingAnnotation.from(it).rank }
              addBinding(maxRankedBinding, ContributesBinding::class)
            }
          }
        }

      // Generate all the contributed multi-bindings
      multiBindings
        .filterNot { replaced.contains(it.toClassName()) }
        .forEach { multiBinding ->
          addBinding(multiBinding, ContributesMultibinding::class)
        }

      // Now iterate through all the subcomponents, and add them
      subcomponents
        .filterNot { subcomponentsReplaced.contains(it.toClassName()) }
        .forEach { subcomponent ->

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
