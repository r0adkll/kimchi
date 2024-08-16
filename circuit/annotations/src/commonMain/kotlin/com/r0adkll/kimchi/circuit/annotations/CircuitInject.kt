// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.circuit.annotations

import kotlin.reflect.KClass

/**
 * TODO: Add kdoc to demonstrate this annotations use
 */
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class CircuitInject(
  val scope: KClass<*>,
  val screen: KClass<*>,
)
