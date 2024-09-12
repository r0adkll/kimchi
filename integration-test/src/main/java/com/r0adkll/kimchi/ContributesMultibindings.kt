// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi

import com.r0adkll.kimchi.annotations.ClassKey
import com.r0adkll.kimchi.annotations.ContributesMultibinding
import com.r0adkll.kimchi.annotations.MergeComponent
import kotlin.reflect.KClass
import me.tatarka.inject.annotations.Inject

object ContributesMultibindingScope

@MergeComponent(ContributesMultibindingScope::class)
interface ContributesMultibindingComponent {
  val bindings: Map<KClass<*>, Binding>
}

interface Binding

@ClassKey(ObjectBinding::class)
@ContributesMultibinding(ContributesMultibindingScope::class)
object ObjectBinding : Binding

@ClassKey(InjectedBinding::class)
@ContributesMultibinding(ContributesMultibindingScope::class)
@Inject
class InjectedBinding : Binding
