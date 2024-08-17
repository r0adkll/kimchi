package com.r0adkll.kimchi.restaurant.di

import com.r0adkll.kimchi.annotations.ContributesSubcomponent
import com.r0adkll.kimchi.restaurant.common.scopes.AppScope
import com.r0adkll.kimchi.restaurant.common.scopes.SingleIn
import com.r0adkll.kimchi.restaurant.common.scopes.UserScope
import com.r0adkll.kimchi.restaurant.common.session.UserSession

@SingleIn(UserScope::class)
@ContributesSubcomponent(
  scope = UserScope::class,
  parentScope = AppScope::class,
)
interface UserComponent {

  @ContributesSubcomponent.Factory
  interface Factory {
    fun create(userSession: UserSession): UserComponent
  }
}
