// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.annotations

import kotlin.reflect.KClass
import me.tatarka.inject.annotations.Component

/**
 * Define a kotlin-inject component that will automatically include bindings, multibindings, and component interfaces
 * that are contributed to the same [scope]. Use this annotation instead of [Component].
 * The KSP processor will generate a "Merged" implementation of interfaces or abstract classes annotated with this
 * annotation automatically adding the contributed elements and adding the [Component] annotation for
 * kotlin-inject processor to pick up.
 * ```
 * @MergeComponent(AppScope::class)
 * interface AppComponent
 *
 * // Will generate the following
 * @Component
 * abstract class MergedAppComponent : AppComponent, MergedInterface // { …
 *
 * fun AppComponent.Companion.create(): MergedAppComponent = MergedAppComponent::class.create()
 * ```
 *
 * It's possible to exclude any automatically added bindings, multibindings, and component interfaces with the
 * [excludes] parameter if needed.
 * ```
 * @MergeComponent(
 *   scope = AppScope::class,
 *   excludes = [
 *     MenuRepository::class,
 *     ComponentInterface::class
 *   ],
 * )
 * interface AppComponent
 * ```
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class MergeComponent(
  /**
   * The scope used to find all contributed bindings, multibindings, and component
   * interfaces, which should be included in this component.
   */
  val scope: KClass<*>,
  /**
   * List of bindings, multibindings, and component interfaces that are contributed to the
   * same scope, but should be excluded from the component.
   */
  val excludes: Array<KClass<*>> = [],
)
