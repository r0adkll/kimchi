// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.util.ksp.component

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isOpen
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.r0adkll.kimchi.annotations.MergeComponentAnnotation
import com.r0adkll.kimchi.util.KimchiException
import com.r0adkll.kimchi.util.applyIf
import com.r0adkll.kimchi.util.ksp.ConstructorParameter
import com.r0adkll.kimchi.util.ksp.hasAnnotation
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.ksp.toTypeName
import me.tatarka.inject.annotations.Component

class MergeComponentDeclaration(
  private val clazz: KSClassDeclaration,
) : KSClassDeclaration by clazz, ComponentDeclaration {

  val annotation: MergeComponentAnnotation by lazy {
    MergeComponentAnnotation.from(clazz)
  }

  override fun constructorParameters(): List<ConstructorParameter> {
    return primaryConstructor?.let { primaryConstructor ->
      primaryConstructor.parameters.map { param ->
        val name = param.name!!.asString()
        val type = param.type.toTypeName()

        if (param.hasAnnotation(Component::class)) {
          // Due to the nature of kotlin inheritance passed parent components must be either
          // open value parameters, or no value params
          if (param.isVal) {
            val isOpen = getDeclaredProperties()
              .find { it.simpleName.asString() == name }
              ?.isOpen() == true

            if (!isOpen) {
              throw KimchiException(
                "Parent @Component properties passed to @MergeComponent classes must be " +
                  "marked `open` or have no val modifiers",
                node = this,
              )
            }
          } else if (param.isVar || param.isVararg) {
            throw KimchiException(
              "Parent @Component properties passed to @MergeComponent classes must be " +
                "marked `open val` or have no val modifiers",
              node = this,
            )
          }

          // If the constructor parameter is annotated with @Component,
          // then we'll want to make sure the merged component parameter
          // is also annotated with @Component and is a value property
          ConstructorParameter(
            parameterSpec = ParameterSpec.builder(name, type)
              .addAnnotation(Component::class)
              .build(),
            propertySpec = PropertySpec.builder(name, type)
              .initializer(name)
              // If the parameter is a val, we need to assume that it is an
              // `open val` and add an override modifier here
              .applyIf(param.isVal) {
                addModifiers(KModifier.OVERRIDE)
              }
              .build(),
          )
        } else {
          // If the constructor parameter is NOT annotated with @Component,
          // then do not cary over any annotations.
          // kotlin-inject seems to respect @get:Provides annotations in the
          // super class so we don't need to duplicate them here
          ConstructorParameter(
            ParameterSpec.builder(name, type)
              .build(),
          )
        }
      }
    } ?: emptyList()
  }
}
