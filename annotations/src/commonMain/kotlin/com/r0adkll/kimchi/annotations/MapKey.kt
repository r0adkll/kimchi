// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.annotations

/**
 * Defines a key that can be used for contributing multibinding elements to a map. Annotations
 * annotated with [MapKey] should have a single parameter of any type. Kimchi will then use this
 * parameter type to key the map that elements annotated with [ContributesMultibinding] will be
 * added to.
 *
 * Take this example:
 * ```
 * @MapKey
 * annotation class ClassKey(val clazz: KClass<*>)
 *
 * @ClassKey(FeatureScreen::class)
 * @ContributesMultibinding(AppScope::class)
 * @Inject
 * class FeatureInjector : Injector
 *
 * // Will generate this binding
 * @Provides
 * @IntoMap
 * public fun provideFeatureInjector_FeatureScreen(
 *   `value`: FeatureInjector
 * ): Pair<KClass<*>, Injector> = (FeatureScreen::class to `value`)
 * ```
 */
@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class MapKey

@MapKey
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class StringKey(
  val value: String,
)

@MapKey
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class IntKey(
  val value: Int,
)

@MapKey
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class LongKey(
  val value: Long,
)
