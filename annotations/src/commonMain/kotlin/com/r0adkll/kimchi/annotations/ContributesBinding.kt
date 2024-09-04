// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.annotations

import kotlin.reflect.KClass

/**
 * This is for contributing concrete instances of implementations to their interfaces/types
 * on the graph
 */
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class ContributesBinding(
  val scope: KClass<*>,
  val boundType: KClass<*> = Unit::class,
  val replaces: Array<KClass<*>> = [],
  val priority: Priority = Priority.NORMAL,
) {
  /**
   * The priority of a contributed binding.
   */
  @Suppress("unused")
  enum class Priority {
    NORMAL, HIGH, HIGHEST
  }
}
