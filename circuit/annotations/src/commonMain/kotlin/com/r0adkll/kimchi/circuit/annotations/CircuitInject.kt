// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.circuit.annotations

import androidx.compose.runtime.Composable
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.Ui
import kotlin.reflect.KClass

/**
 * This annotation is used to mark a UI or presenter class or function for code generation. When
 * annotated, the type's corresponding factory will be generated and keyed with the defined [screen].
 *
 * The generated factories are then added to the kotlin-inject graph by generating a module
 * component interface that defines the binding and then uses
 * [com.r0adkll.kimchi.annotations.ContributesTo] scoped with the provided [scope] to contribute it.
 *
 * ## Classes
 *
 * [Presenter] and [Ui] classes can be annotated and have their corresponding [Presenter.Factory] or
 * [Ui.Factory] classes generated for them.
 *
 * **Presenter**
 *
 * ```kotlin
 * @CircuitInject(HomeScreen::class, AppScope::class)
 * @Inject
 * class HomePresenter(…) : Presenter<HomeState> { … }
 *
 * // Generates
 * @ContributesTo(AppScope::class)
 * interface HomePresenterFactoryComponent {
 *   @Provides @IntoSet
 *   fun bindHomePresenterFactory(factory: HomePresenterFactory): Presenter.Factory = factory
 * }
 *
 * @Inject
 * public class HomePresenterFactory(): Presenter.Factory { … }
 * ```
 *
 * **UI**
 *
 * ```kotlin
 * @CircuitInject(HomeScreen::class, AppScope::class)
 * @Inject
 * class HomeUi(…) : Ui<HomeState> { … }
 *
 * // Generates
 * interface HomeUiFactoryComponent {
 *   @Provides @IntoSet
 *   fun bindHomeUiFactory(factory: HomeUiFactory): Ui.Factory = factory
 * }
 *
 * @Inject
 * class HomeUiFactory() : Ui.Factory { … }
 * ```
 *
 * ## Functions
 *
 * Simple functions can be annotated and have a corresponding [Presenter.Factory] generated. This is
 * primarily useful for simple cases where a class is just technical tedium.
 *
 * **Requirements**
 * - Presenter functions _must_ return a [CircuitUiState] type, otherwise they will be treated as UI
 *   functions.
 * - UI functions can optionally accept a [CircuitUiState] type as a parameter, but it is not
 *   required.
 * - UI functions _must_ return [Unit].
 * - Both presenter and UI functions _must_ be [Composable].
 *
 * **Presenter**
 *
 * ```kotlin
 * @CircuitInject(HomeScreen::class, AppScope::class)
 * @Composable
 * fun HomePresenter(): HomeState { … }
 * ```
 *
 * **UI**
 *
 * ```kotlin
 * @CircuitInject(HomeScreen::class, AppScope::class)
 * @Composable
 * fun Home(state: HomeState) { … }
 * ```
 *
 * ## Assisted injection
 *
 * Any type that is offered in [Presenter.Factory] and [Ui.Factory] can be offered as an assisted
 * injection to types using Dagger [me.tatarka.inject.annotations.Assisted].
 *
 * Types available for assisted injection are:
 * - [Screen] – the screen key used to create the [Presenter] or [Ui].
 * - [Navigator] – (presenters only)
 * - [CircuitContext]
 *
 * Each should only be defined at-most once.
 *
 * **Examples**
 *
 * ```kotlin
 * // Function example
 * @CircuitInject(HomeScreen::class, AppScope::class)
 * @Composable
 * fun HomePresenter(@Assisted screen: Screen, @Assisted navigator: Navigator): HomeState { … }
 *
 * // Class example
 * class HomePresenter @AssistedInject constructor(
 *   @Assisted screen: Screen,
 *   @Assisted navigator: Navigator,
 *   …
 * ) : Presenter<HomeState> // …
 * ```
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class CircuitInject(
  val screen: KClass<*>,
  val scope: KClass<*>,
)
