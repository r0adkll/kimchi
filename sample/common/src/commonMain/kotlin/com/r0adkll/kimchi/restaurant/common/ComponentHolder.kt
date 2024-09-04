// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.restaurant.common

object ComponentHolder {
  var components = mutableSetOf<Any>()

  inline fun <reified T> component(): T {
    return components
      .filterIsInstance<T>()
      .firstOrNull()
      ?: throw IllegalArgumentException("Unable to find a component for type '${T::class.qualifiedName}'")
  }
}
