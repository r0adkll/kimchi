// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.restaurant.android

import android.app.Application
import com.r0adkll.kimchi.restaurant.android.di.AndroidAppComponent
import com.r0adkll.kimchi.restaurant.common.ComponentHolder
import com.r0adkll.kimchi.restaurant.common.session.UserSession
import com.r0adkll.kimchi.restaurant.di.UserComponent
import kimchi.merge.com.r0adkll.kimchi.restaurant.android.di.createAndroidAppComponent

class RestaurantApplication : Application() {

  override fun onCreate() {
    super.onCreate()

    ComponentHolder.components += AndroidAppComponent.createAndroidAppComponent(this)
    ComponentHolder.components += ComponentHolder
      .component<UserComponent.Factory>()
      .create(UserSession.LoggedIn)
  }
}
