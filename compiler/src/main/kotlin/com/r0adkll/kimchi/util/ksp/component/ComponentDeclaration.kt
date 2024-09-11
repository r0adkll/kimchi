// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.util.ksp.component

import com.r0adkll.kimchi.util.ksp.ConstructorParameter

/**
 * A central interface for [com.r0adkll.kimchi.annotations.MergeComponent]
 * and [com.r0adkll.kimchi.annotations.ContributesSubcomponent] for generating
 * the parameter list needed to build the constructor of the generated Merged component
 */
interface ComponentDeclaration {

  fun constructorParameters(): List<ConstructorParameter>
}
