package com.r0adkll.kimchi.util

import java.util.Locale

fun String.capitalized(): String {
  return replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}
