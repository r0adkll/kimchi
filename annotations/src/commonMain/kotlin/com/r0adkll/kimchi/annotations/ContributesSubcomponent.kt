// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.annotations

import kotlin.reflect.KClass

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class ContributesSubcomponent(
  val scope: KClass<*>,
  val parentScope: KClass<*>,
  val excludes: Array<KClass<*>> = [],
  val replaces: Array<KClass<*>> = [],
) {

  @MustBeDocumented
  @Retention(AnnotationRetention.SOURCE)
  annotation class Factory
}
