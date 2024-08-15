// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.annotations

import kotlin.reflect.KClass

/**
 * TODO: Move this to a separate module once we better productionize this processor
 *   and finish plugin APIs
 */
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class CircuitInject(
  val scope: KClass<*>,
  val screen: KClass<*>,
)
