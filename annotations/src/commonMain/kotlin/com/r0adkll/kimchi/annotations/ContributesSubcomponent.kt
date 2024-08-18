// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.annotations

import kotlin.reflect.KClass

/**
 * Generate a kotlin-inject subcomponent that will merge dependencies of its [scope] and contribute it to the
 * provided [parentScope]. Contributed elements to the [scope] won't be processed until the component for the
 * [parentScope] is processed.
 *
 * As an example take this module layout:
 * ```
 *      :app
 *     /    \
 *    v      v
 *  :foo    :bar
 * ```
 * `:app` creates a parent component with `@MergeComponent`, `:foo` contributes a subcomponent with
 * `@ContributesSubcomponent` and `:bar` contributes a binding to the scope of the subcomponent. Since the
 * subcomponent merging is processed when the parent component is, the contributed binding from `:foo` will
 * be merged in `:app` without `:foo` having a dependency on `:bar`.
 *
 * ```
 * @ContributesSubcomponent(
 *   scope = UserScope::class,
 *   parentScope = AppScope::class,
 * )
 * interface UserComponent {
 *
 *   @ContributesSubcomponent.Factory
 *   interface Factory {
 *     fun create(): UserComponent
 *   }
 * }
 * ```
 *
 * [parentScope] can be the scope of a [MergeComponent] or another [ContributesSubcomponent]. A chain of
 * [ContributesSubcomponent] is supported.
 *
 * A nested [ContributesSubcomponent.Factory] annotated interface is required when defining subcomponents.
 * This interface is added to the [parentScope] component's supertypes and provides a means to instantiate
 * your subcomponent as well as provide additional dependencies to its graph and sub-graphs.
 *
 * It's possible to exclude any automatically added bindings, multibindings, or component interface with the
 * [excludes] parameter if needed.
 * ```
 * @ContributesSubcomponent(
 *   scope = UserScope::class,
 *   parentScope = AppScope::class,
 *   exclude = [
 *     MenuRepository::class,
 *     ComponentInterface::class
 *   ]
 * )
 * interface UserComponent
 * ```
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ContributesSubcomponent(
  /**
   * The scope used to find all contributed bindings, multibindings, and component
   * interfaces, which should be included in this subcomponent.
   */
  val scope: KClass<*>,
  /**
   * The actual subcomponent for this contributed subcomponent will be generated when a
   * [MergeComponent] or another [ContributesSubcomponent] annotated component
   * with the same scope as [parentScope] is merged.
   */
  val parentScope: KClass<*>,
  /**
   * List of bindings, multibindings, and component interfaces that are contributed to the
   * same [scope], but should be excluded from the subcomponent.
   */
  val excludes: Array<KClass<*>> = [],
  /**
   * This contributed subcomponent will replace these contributed subcomponents. All replaced
   * subcomponents must use the same scope.
   */
  val replaces: Array<KClass<*>> = [],
) {

  /**
   * A factory for the contributed subcomponent.
   *
   * An interface annotated with this annotation defines how this subcomponent is instantiated from its parent.
   * The annotated interface must be composed of just a single function that returns its subcomponent interface.
   * ```
   * @ContributesSubcomponent(
   *   scope = UserScope::class,
   *   parentScope = AppScope::class,
   * )
   * interface UserComponent {
   *
   *   @ContributesSubcomponent.Factory
   *   interface Factory {
   *     fun create(): UserComponent
   *   }
   * }
   *
   * // This will generate the following
   * @Component
   * abstract class MergedAppComponent : UserComponent.Factory {
   *   override fun create(): UserComponent = MergedUserComponent::class.create(this)
   *
   *   @Component
   *   abstract class MergedUserComponent(
   *     @Component val parent: MergedAppComponent,
   *   )
   * }
   * ```
   *
   * You can add additional parameters to the factory's create function to provide additional dependencies
   * to the underlying graph.
   * ```
   * @ContributesSubcomponent.Factory
   * interface Factory {
   *   fun create(
   *     userSession: UserSession,
   *     @Named("abc") analyticsSessionId: String,
   *   ): UserComponent
   * }
   *
   * // Will generate
   * @Component
   * abstract class MergedUserComponent(
   *   @get:Provides val userSession: UserSession,
   *   @get:Provides @Named("abc") analyticsSessionId: String,
   *   @Component val parent: MergedAppComponent,
   * )
   * ```
   */
  @Target(AnnotationTarget.CLASS)
  @Retention(AnnotationRetention.SOURCE)
  annotation class Factory
}
