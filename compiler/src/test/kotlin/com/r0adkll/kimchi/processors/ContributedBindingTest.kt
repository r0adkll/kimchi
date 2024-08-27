package com.r0adkll.kimchi.processors

import com.r0adkll.kimchi.compileKimchiWithTestSources
import org.junit.jupiter.api.Test

class ContributedBindingTest {

  @Test
  fun `contributed binding gets added to a component`() {
    compileKimchiWithTestSources(
      """
        package kimchi

        import org.jetbrains.kotlin.javax.inject.Inject

        interface Binding

        @ContributesBinding(TestScope::class)
        @Inject
        class RealBinding : Binding
      """.trimIndent(),
    ) {

    }
  }

}
