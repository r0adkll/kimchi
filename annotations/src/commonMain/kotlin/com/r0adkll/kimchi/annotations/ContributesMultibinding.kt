// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.annotations

import kotlin.reflect.KClass

/**
 * Generate a kotlin-inject multibinding function for an annotated class and contributes this multibinding
 * function to the given [scope]. Take this example:
 * ```
 * interface AppInitializer
 *
 * @Inject
 * class LoggingInitializer : AppInitializer
 *
 * @ContributesTo(AppScope::class)
 * interface LoggingListenerModule {
 *   fun LoggingInitializer.binds: AppInitializer
 *     @Provides get() = this
 * }
 * ```
 * This is a lot of boilerplate. You can replace this entire module with the
 * [ContributesMultibinding] annotation. The equivalent would be:
 * ```
 * interface AppInitializer
 *
 * @ContributesMultibinding(AppScope::class)
 * @Inject
 * class LoggingInitializer : AppInitializer
 * ```
 * Notice that it's optional to specify [boundType], if there is only exactly one super type. If
 * there are multiple super types, then it's required to specify the parameter:
 * ```
 * @ContributesMultibinding(
 *   scope = AppScope::class,
 *   boundType = AppInitializer::class
 * )
 * @Inject
 * class AnalyticsInitializer : AppInitializer, AnalyticsListener
 * ```
 *
 * [ContributesMultibinding] supports qualifiers. If you annotate the class additionally with a
 * qualifier, then the generated multibinding function will be annotated with the same qualifier,
 * e.g.
 * ```
 * @ContributesMultibinding(AppScope::class)
 * @Named("Prod")
 * @Inject
 * class LoggingInitializer : AppInitializer
 *
 * // Will generate this binding function.
 * val LoggingInitializer.bind: AppInitializer
 *   @Provides @Named(name = "beverages")
 *   @IntoSet
 *   get() = this
 * ```
 *
 * To generate a Map multibindings function you need to annotate the class with the map key. Kimchi
 * will use the map key as hint to generate a binding function for a map instead of a set:
 * ```
 * @MapKey
 * annotation class BindingKey(val value: String)
 *
 * @ContributesMultibinding(AppScope::class)
 * @BindingKey("abc")
 * @Inject
 * class LoggingInitializer : AppInitializer
 *
 * // Will generate this binding function.
 * @Provides
 * @IntoMap
 * public fun provideAppInitializer_abc(
 * `value`: LoggingInitializer
 * ): Pair<String, MenuSection> = ("abc" to `value`)
 * ```
 * Note that map keys must allow classes as target. Kimchi provides a few built-in MapKeys that you can use
 * out of the box, such as [StringKey]. [IntKey], and [LongKey]. You can provide any custom map key and Kimchi
 * will bind your dependencies keyed to the first and only parameter of the annotation key you use.
 *
 * Contributed multibindings can replace other contributed modules and contributed multibindings
 * with the [replaces] parameter. This is especially helpful for different multibindings in
 * tests.
 * ```
 * @ContributesMultibinding(
 *   scope = AppScope::class,
 *   replaces = [MainInitializer::class]
 * )
 * @Inject
 * class FakeInitializer : AppInitializer
 * ```
 * [ContributesMultibinding] supports Kotlin objects, e.g.
 * ```
 * @ContributesMultibinding(AppScope::class)
 * object MainInitializer : Initializer
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class ContributesMultibinding(
  /**
   * The merging scope in which to include this multibinding
   */
  val scope: KClass<*>,
  /**
   * The type that this class is bound to. This class will be included in the collection for
   * [boundType].
   */
  val boundType: KClass<*> = Unit::class,
  /**
   * This contributed multibinding will replace these contributed classes. The array is allowed to
   * include other contributed multibindings. All replaced classes must use the same scope.
   */
  val replaces: Array<KClass<*>> = [],
)
