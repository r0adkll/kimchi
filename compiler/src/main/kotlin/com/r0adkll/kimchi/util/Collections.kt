package com.r0adkll.kimchi.util

infix fun <T> List<T>.addIfNonNull(value: T?): List<T> {
  return if (value != null) plus(value) else this
}
