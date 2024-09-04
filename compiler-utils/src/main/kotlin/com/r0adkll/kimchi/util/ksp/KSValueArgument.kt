// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.util.ksp

import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueArgument
import com.r0adkll.kimchi.util.toClassName
import com.squareup.kotlinpoet.ClassName

/**
 * Get the arguments value as a ClassName, if possible
 */
val KSValueArgument.valueAsClassName: ClassName?
  get() = value
    ?.let { it as? KSType }
    ?.declaration
    ?.toClassName()

/**
 * Get the arguments value as a list of class names, if possible
 */
val KSValueArgument.valueAsClassNameList: List<ClassName>?
  get() = value
    ?.let { it as? List<*> }
    ?.mapNotNull {
      (it as? KSType)?.declaration?.toClassName()
    }
