// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.util

import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds

data class MeasuredResult<R>(
  val result: R,
  val elapsedDuration: Duration,
)

/**
 * Helper function to wrap and profile different aspects of the code for debugging purposes
 */
inline fun <reified T> measure(block: () -> T): MeasuredResult<T> {
  val start = System.nanoTime()
  val result = block()
  val elapsed = System.nanoTime() - start
  return MeasuredResult(result, elapsed.nanoseconds)
}
