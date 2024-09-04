// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.annotations

import kotlin.reflect.KClass

/**
 * This is for contributing component interfaces to scopes on the graph
 */
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class ContributesTo(
  val scope: KClass<*>,
  val replaces: Array<KClass<*>> = [],
)
