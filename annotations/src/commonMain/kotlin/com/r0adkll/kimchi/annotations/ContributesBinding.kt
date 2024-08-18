// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.annotations

import kotlin.reflect.KClass

/**
 * Generate a kotlin-inject binding method for an annotated class and contribute this binding to
 * the given [scope]. Take this example:
 *
 * ```
 * interface MenuRepository
 *
 * @Inject
 * class MenuRepositoryImpl : MenuRepository
 *
 * interface MenuRepositoryModule {
 *   val MenuRepositoryImpl.bind: MenuRepository
 *     @Provides get() = this
 * }
 *
 * @Component
 * abstract class AppComponent : MenuRepositoryModule
 * ```
 * That is a lot of boilerplate to use `MenuRepositoryImpl` when you inject `MenuRepository`. You can replace
 * this entire module and `AppComponent` wiring with the [ContributesBinding] annotation.
 * The equivalent would be:
 * ```
 * interface MenuRepository
 *
 * @ContributesBinding(AppScope::class)
 * @Inject
 * class MenuRepositoryImpl : MenuRepository
 *
 * @MergeComponent(AppScope::class)
 * abstract class AppComponent
 * ```
 *
 *
 */
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class ContributesBinding(
  val scope: KClass<*>,
  val boundType: KClass<*> = Unit::class,
  val replaces: Array<KClass<*>> = [],
  val rank: Int = RANK_NORMAL,
) {

  companion object {
    const val RANK_NORMAL: Int = Int.MIN_VALUE
    const val RANK_HIGH: Int = 0
    const val RANK_HIGHEST: Int = Int.MAX_VALUE
  }
}
