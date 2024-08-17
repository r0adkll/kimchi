package com.r0adkll.kimchi.restaurant.common.session

sealed interface UserSession {
  data object LoggedOut : UserSession
  data object LoggedIn : UserSession
}
