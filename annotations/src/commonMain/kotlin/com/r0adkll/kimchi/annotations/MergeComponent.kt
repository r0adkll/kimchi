// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.annotations

import kotlin.reflect.KClass

/**
 * This is for merging contributed interfaces and bindings to the graph
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
annotation class MergeComponent(
  val scope: KClass<*>,
)
