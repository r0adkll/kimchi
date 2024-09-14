// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.annotations

import com.squareup.kotlinpoet.ClassName

interface BindingAnnotation {

  val boundType: ClassName
}
