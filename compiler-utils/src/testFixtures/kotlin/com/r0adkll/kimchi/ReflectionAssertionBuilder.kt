// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi

import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty2
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.full.primaryConstructor
import strikt.api.Assertion
import strikt.assertions.elementAt
import strikt.assertions.filterIsInstance
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

public fun <E : KAnnotatedElement> Assertion.Builder<E>.hasAnnotation(
  annotation: KClass<*>,
): Assertion.Builder<E> {
  return assert("has the annotation") {
    if (it.annotations.any { it.annotationClass == annotation }) {
      pass()
    } else {
      fail(
        description = "only has",
        actual = it.annotations,
      )
    }
  }
}

public inline fun <reified A : Annotation> Assertion.Builder<KClass<*>>.withAnnotation(
  crossinline block: Assertion.Builder<A>.() -> Unit,
): Assertion.Builder<KClass<*>> {
  return with("with annotation", { findAnnotation<A>() }) {
    isNotNull().block()
  }
}

public fun Assertion.Builder<KClass<*>>.primaryConstructor(): Assertion.Builder<KFunction<*>?> =
  get { primaryConstructor }

public fun Assertion.Builder<KClass<*>>.implements(
  clazz: KClass<*>,
): Assertion.Builder<KClass<*>> = assert("implements the class") {
  if (it.allSuperclasses.any { s -> s == clazz }) {
    pass()
  } else {
    fail(
      description = "does not implement",
    )
  }
}

public fun Assertion.Builder<KClass<*>>.declaredProperties(): Assertion.Builder<List<KProperty2<*, *, *>>> {
  return get { declaredMembers }
    .filterIsInstance<KProperty2<*, *, *>>()
}

public fun Assertion.Builder<KProperty2<*, *, *>>.hasReceiverOf(
  clazz: KClass<*>,
): Assertion.Builder<KClassifier?> {
  return get { extensionReceiverParameter }
    .isNotNull()
    .get { type.classifier } isEqualTo clazz
}

public fun Assertion.Builder<KProperty2<*, *, *>>.hasReturnTypeOf(
  clazz: KClass<*>,
): Assertion.Builder<KClassifier?> {
  return get { returnType.classifier } isEqualTo clazz
}

public fun Assertion.Builder<KProperty2<*, *, *>>.getter(): Assertion.Builder<KProperty2.Getter<*, *, *>> {
  return get { getter }
}

public inline fun Assertion.Builder<KClass<*>>.withFunction(
  name: String,
  crossinline block: Assertion.Builder<KFunction<*>>.() -> Unit,
): Assertion.Builder<KClass<*>> {
  return with({ functions.find { it.name == name } }) {
    isNotNull().block()
  }
}

public fun Assertion.Builder<KFunction<*>>.hasReturnType(
  clazz: KClass<*>,
): Assertion.Builder<KFunction<*>> = assert("has return type of") {
  if (it.returnType.classifier == clazz) {
    pass(it.returnType.classifier)
  } else {
    fail(
      description = "is actually type of",
      actual = it.returnType.classifier,
    )
  }
}

public fun Assertion.Builder<KFunction<*>>.parameter(
  index: Int,
): Assertion.Builder<KParameter> {
  return get { parameters }
    .elementAt(index)
}

public fun Assertion.Builder<KParameter>.isTypeOf(
  clazz: KClass<*>,
): Assertion.Builder<KParameter> = assert("is type of") {
  if (it.type.classifier == clazz) {
    pass(it.type.classifier)
  } else {
    fail(
      description = "is actually type of",
      actual = it.type.classifier,
    )
  }
}
