package com.r0adkll.kimchi.annotations

import kotlin.reflect.KClass

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class ContributesSubcomponent(
  val scope: KClass<*>,
  val parentScope: KClass<*>,
)
