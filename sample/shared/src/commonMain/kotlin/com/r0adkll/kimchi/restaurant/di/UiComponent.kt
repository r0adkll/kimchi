// Copyright (C) 2023 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.restaurant.di

import com.r0adkll.kimchi.annotations.ContributesTo
import com.r0adkll.kimchi.restaurant.common.scopes.SingleIn
import com.r0adkll.kimchi.restaurant.common.scopes.UiScope
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import me.tatarka.inject.annotations.Provides

@ContributesTo(UiScope::class)
interface UiComponent {

  @Provides
  @SingleIn(UiScope::class)
  fun provideCircuit(
    uiFactories: Set<Ui.Factory>,
    presenterFactories: Set<Presenter.Factory>,
  ): Circuit = Circuit.Builder()
    .addUiFactories(uiFactories)
    .addPresenterFactories(presenterFactories)
    .build()
}
