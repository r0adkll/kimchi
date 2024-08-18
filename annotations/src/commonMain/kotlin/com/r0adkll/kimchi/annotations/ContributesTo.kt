// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.annotations

import kotlin.reflect.KClass

/**
 * Marks a component interface to be included in the kotlin-inject dependency graph for the
 * given [scope]. Kimchi automatically extends the kotlin-inject component marked with [MergeComponent]
 * with the interface annotated.
 *
 * ```
 * @ContributesTo(AppScope::class)
 * interface SomeModule { … }
 * ```
 *
 * Component interfaces can replace other contributed component interfaces with the
 * [replaces] parameter. This is especially helpful for modules providing different bindings in
 * instrumentation tests.
 * ```
 * @ContributesTo(
 *   AppScope::class,
 *   replaces = [CoroutinesModule::class]
 * )
 * interface CoroutinesTestModule {
 *
 *   @Provides
 *   fun provideDispatcherProvider(): DispatcherProvider = TestDispatcherProvider
 * }
 * ```
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class ContributesTo(
  /**
   * The merging scope in which to include this binding
   */
  val scope: KClass<*>,
  /**
   * This contributed interface will replace these contributed classes. The array is allowed to
   * include other contributed bindings, multibindings and component interfaces. All replaced classes
   * must use the same scope.
   */
  val replaces: Array<KClass<*>> = [],
)
