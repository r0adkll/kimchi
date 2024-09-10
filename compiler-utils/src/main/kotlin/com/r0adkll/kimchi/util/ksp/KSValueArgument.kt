// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.util.ksp

import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueArgument
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName

/**
 * Get the arguments value as a ClassName, if possible
 */
public val KSValueArgument.valueAsClassName: ClassName?
  get() = value
    ?.let { it as? KSType }
    ?.classDeclaration
    ?.toClassName()

/**
 * Get the arguments value as a list of class names, if possible
 */
public val KSValueArgument.valueAsClassNameList: List<ClassName>?
  get() = value
    ?.let { it as? List<*> }
    ?.mapNotNull {
      (it as? KSType)?.classDeclaration?.toClassName()
    }
