// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.annotations

import com.squareup.kotlinpoet.ClassName

sealed interface ReplaceableAnnotation {

  /**
   * List of other classes annotated with a [ReplaceableAnnotation] that are
   * replaced by this class.
   */
  val replaces: List<ClassName>
}
