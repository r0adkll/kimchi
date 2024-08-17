package com.r0adkll.kimchi.restaurant.common.scopes

import kotlin.reflect.KClass
import me.tatarka.inject.annotations.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class SingleIn(val scope: KClass<*>)
