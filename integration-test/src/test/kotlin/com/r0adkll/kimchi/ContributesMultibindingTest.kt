// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi

import kimchi.merge.com.r0adkll.kimchi.createContributesMultibindingComponent
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.getValue
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isSameInstanceAs

class ContributesMultibindingTest {

  /**
   * Integration test for [com.r0adkll.kimchi.annotations.ClassKey] based
   * multi-bindings. There is a bug in kotlin-compile-testing with kotlin-inject
   * that causes this setup to not compile. So instead lets just use an integration
   * test here to verify outputs.
   */
  @Test
  fun `ClassKey @ContributesMultibinding merges bindings`() {
    // given
    val mergedComponent = ContributesMultibindingComponent::class.createContributesMultibindingComponent()

    // when
    val bindings = mergedComponent.bindings

    // then
    expectThat(bindings) {
      hasSize(2)
      getValue(ObjectBinding::class) isSameInstanceAs ObjectBinding
      getValue(InjectedBinding::class).isA<InjectedBinding>()
    }
  }
}
