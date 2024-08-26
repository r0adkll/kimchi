// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.util

import com.google.devtools.ksp.symbol.KSDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

public fun FileSpec.Companion.buildFile(
  packageName: String,
  fileName: String,
  builder: FileSpec.Builder.() -> Unit,
): FileSpec = builder(packageName, fileName)
  .apply(builder)
  .build()

public fun TypeSpec.Companion.buildClass(
  name: String,
  builder: TypeSpec.Builder.() -> Unit,
): TypeSpec = classBuilder(name)
  .apply(builder)
  .build()

public fun PropertySpec.Companion.buildProperty(
  name: String,
  type: ClassName,
  builder: PropertySpec.Builder.() -> Unit,
): PropertySpec = builder(name, type)
  .apply(builder)
  .build()

public fun FunSpec.Companion.buildFun(
  name: String,
  builder: FunSpec.Builder.() -> Unit,
): FunSpec = builder(name)
  .apply(builder)
  .build()

public fun FunSpec.Companion.buildConstructor(
  builder: FunSpec.Builder.() -> Unit,
): FunSpec = constructorBuilder()
  .apply(builder)
  .build()

public fun KSDeclaration.toClassName(): ClassName = ClassName(packageName.asString(), simpleName.asString())

public fun <T> T.applyIf(predicate: Boolean, block: T.() -> Unit): T {
  return if (predicate) apply(block) else this
}
