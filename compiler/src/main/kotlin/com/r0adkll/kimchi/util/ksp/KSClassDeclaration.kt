// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.util.ksp

import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 * Returns true if this [KSClassDeclaration] has a companion object.
 */
fun KSClassDeclaration.hasCompanionObject(): Boolean {
  return declarations
    .filterIsInstance<KSClassDeclaration>()
    .filter { it.isCompanionObject }
    .firstOrNull() != null
}
