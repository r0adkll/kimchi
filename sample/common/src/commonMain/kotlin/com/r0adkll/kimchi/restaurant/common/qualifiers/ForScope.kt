package com.r0adkll.kimchi.restaurant.common.qualifiers

import kotlin.reflect.KClass
import me.tatarka.inject.annotations.Qualifier

@Qualifier
@Target(
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.VALUE_PARAMETER,
  AnnotationTarget.TYPE,
  AnnotationTarget.CLASS
)
annotation class ForScope(val scope: KClass<*>)
