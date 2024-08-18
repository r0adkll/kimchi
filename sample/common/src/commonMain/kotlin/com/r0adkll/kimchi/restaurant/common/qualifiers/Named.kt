package com.r0adkll.kimchi.restaurant.common.qualifiers

import me.tatarka.inject.annotations.Qualifier

@Qualifier
@Target(
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.VALUE_PARAMETER,
  AnnotationTarget.TYPE,
  AnnotationTarget.CLASS
)
annotation class Named(val name: String)
