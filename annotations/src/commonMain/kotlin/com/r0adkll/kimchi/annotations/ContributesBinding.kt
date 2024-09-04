// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.annotations

import kotlin.reflect.KClass

/**
 * Generate a kotlin-inject binding function for an annotated class and contribute this binding to
 * the given [scope]. Take this example:
 *
 * ```
 * interface MenuRepository
 *
 * @Inject
 * class MenuRepositoryImpl : MenuRepository
 *
 * @ContributesTo(AppScope::class)
 * interface MenuRepositoryModule {
 *   val MenuRepositoryImpl.bind: MenuRepository
 *     @Provides get() = this
 * }
 * ```
 * That is a lot of boilerplate. You can replace
 * this entire module with the [ContributesBinding] annotation.
 * The equivalent would be:
 * ```
 * interface MenuRepository
 *
 * @ContributesBinding(AppScope::class)
 * @Inject
 * class MenuRepositoryImpl : MenuRepository
 * ```
 * Notice that it's optional to specify [boundType], if there is only exactly one super type. If
 * there are multiple super types, then it's required to specify the parameter:
 * ```
 * @ContributesBinding(
 *   scope = AppScope::class,
 *   boundType = MenuRepository::class
 * )
 * @Inject
 * class MenuRepositoryImpl : AbstractSpecialsProvider(), MenuRepository
 * ```
 *
 * [ContributesBinding] supports qualifiers. If you annotate the class additionally with a
 * qualifier, then the generated binding function will be annotated with the same qualifier, e.g.
 * ```
 * @ContributesBinding(UserScope::class)
 * @Named("dessert")
 * @Inject
 * class DessertSection : MenuSection
 *
 * // Will generate this binding
 * val DessertSection.bind: MenuSection
 *   @Provides @Named(name = "dessert")
 *   get() = this
 * ```
 *
 * Contributed bindings can replace other contributed modules and bindings with the [replaces]
 * parameter. This is especially helpful for different bindings in instrumentation tests.
 * ```
 * @ContributesBinding(
 *   scope = AppScope::class,
 *   replaces = [MenuRepositoryImpl::class]
 * )
 * @Inject
 * class FakeMenuRepository : MenuRepository
 * ```
 * If you don't have access to the class of another contributed binding that you want to replace,
 * then you can change the [rank] of the bindings to avoid duplicate bindings. The contributed
 * binding with the higher rank will be used.
 *
 * [ContributesBinding] supports Kotlin objects, e.g.
 * ```
 * @ContributesBinding(AppScope::class)
 * object MenuRepositoryImpl : MenuRepository
 * ```
 * In this scenario instead of generating a `.binds` extension function Kimchi will generate a `@Provides`
 * function returning [boundType].
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class ContributesBinding(
  /**
   * The merging scope in which to include this binding
   */
  val scope: KClass<*>,
  /**
   * The type that this class is bound to. When injecting [boundType] the concrete class will be
   * this annotated class.
   */
  val boundType: KClass<*> = Unit::class,
  /**
   * This contributed binding will replace these contributed classes. The array is allowed to
   * include other contributed bindings. All replaced classes must use the same scope.
   */
  val replaces: Array<KClass<*>> = [],
  /**
   * The rank of this contributed binding. The rank should be changed only if you don't
   * have access to the contributed binding class that you want to replace at compile time. If
   * you have access and can reference the other class, then it's highly suggested to
   * use [replaces] instead.
   *
   * In case of a duplicate binding for multiple contributed bindings, the binding with the highest
   * rank will be used and replace other contributed bindings for the same type with a lower
   * rank. If duplicate contributed bindings use the same rank, then there will be an
   * error for duplicate bindings.
   *
   * Note that [replaces] takes precedence. If you explicitly replace a binding, it won't be
   * considered no matter what its rank is.
   *
   * All contributed bindings have a [RANK_NORMAL] rank by default.
   */
  val rank: Int = RANK_NORMAL,
) {

  companion object {
    const val RANK_NORMAL: Int = Int.MIN_VALUE
    const val RANK_HIGH: Int = 0
    const val RANK_HIGHEST: Int = Int.MAX_VALUE
  }
}
