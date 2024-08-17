// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.restaurant.common.session

sealed interface UserSession {
  data object LoggedOut : UserSession
  data object LoggedIn : UserSession
}
