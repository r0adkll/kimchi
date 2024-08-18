![](.github/art/banner.png)

# Kimchi

**K**_otlin_ **I**_nject_ **M**_erge_ **C**_omponent_ **H**_ints_ **I**_ntuitively_

Kimchi is an [Anvil](https://github.com/square/anvil)-like KSP processor for [kotlin-inject](https://github.com/evant/kotlin-inject) that lets you contribute bindings, modules, and subcomponents across multi-module projects to form your dependency injection graph without having manual wiring your components upstream.

## Getting started

Getting started is easy! Just create a component abstract class or interface annotated with `@MergeComponent` like so

```kotlin
@MergeComponent(AppScope::class)
abstract class AppComponent
```

Now we have a component ready to collect contributions on the scope `AppScope`
Next, start contributing modules and bindings.

### Binding
```kotlin
interface MenuRepository {
  suspend fun getItems(): List<MenuItem>
}

@ContributesBinding(AppScope::class)
@Inject
class MenuRepositoryImpl(/*…*/) : MenuRepository {
  override suspend fun getItems(): List<MenuItem> = //…
}
```

### Multi-bindings
```kotlin
interface AppInitializer // { … }

@ContributesMultibinding(AppScope::class)
@Inject
class AnalyticsInitializer() : AppInitializer

@ContributesMultibinding(AppScope::class)
@Inject
class LoggingInitializer() : AppInitializer

// Will come together in a Set
@MergeComponent(AppScope::class)
abstract class AppComponent {
  abstract val initializers: Set<AppInitializer>
```
or you can specify a `MapKey` (`StringKey`, `IntKey`, `LongKey`, or you can make a custom key if needed) for mapped bindings

```kotlin
interface MenuSection

@StringKey("appetizers")
@ContributesMultibinding(AppScope::class)
@Inject
class AppetizerSection : MenuSection

@StringKey("entrees")
@ContributesMultibinding(AppScope::class)
@Inject
class EntreeSection : MenuSection

// Will come together in a Map
@MergeComponent(AppScope::class)
abstract class AppComponent {
  abstract val menu: Map<String, MenuSection>
```

### Modules / Component interfaces
```kotlin
@ContributesTo(AppScope::class)
interface CoroutinesModule {

  @Provides
  fun provideCoroutineDispatchers(): DispatcherProvider = DispatcherProvider(
    io = Dispatchers.IO,
    computation = Dispatchers.Default,
    main = Dispatchers.Main,
  )
}
```
This allows you to extend the upstream generated `@Component` with an interface full of bindings or other exposing provisions that you can control. This will look something like:

```kotlin
@Component
abstract class AppComponent : CoroutinesModule {
  //…
}
```

### Subcomponent

```kotlin
@ContributesSubcomponent(
  scope = UserScope::class,
  parentScope = AppScope::class,
)
interface UserComponent {

  @ContributesSubcomponent.Factory
  interface Factory {
    fun create(
      userSession: UserSession,
    ) : UserComponent
  }
}
```
This will merge all contributions to its scope, e.g. `UserScope`, and generate a subcomponent when its parent, e.g. `AppScope`, is processed. Outputting something like this:

```kotlin

@Component
abstract MergedAppComponent : UserComponent.Factory {

  override fun create(userSession: UserSession): UserComponent =
    MergedUserComponent::class.create(userSession, this)

  @Component
  abstract class MergedUserComponent(
    @get:Provides val userSession: UserSession,
    @Component val parent: MergedAppComponent,
  ) : UserComponent // { … }
}

```

### Scopes

**Kimchi** uses scopes as markers to help the KSP processor connect the contributions you make to the target component you want to merge them to. The class `AppScope` from these examples can be represented like this:

```kotlin
object AppScope
```
These scope marker classes are independent of [kotlin-inject's scopes](https://github.com/evant/kotlin-inject?tab=readme-ov-file#scopes) but can be used together by creating a kotlin-inject scope wrapper like so:

```kotlin
// SingleIn.kt
@Scope // <-- kotlin-inject scoping
@Retention(AnnotationRetention.RUNTIME)
annotation class SingleIn(val scope: KClass<*>)

// AppComponent.kt
@SingleIn(AppScope::class)
@MergeComponent(AppScope::class)
abstract class AppComponent

// or…
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class MenuRepositoryImpl : MenuRepository
```

### Exclusions & Replacements

Components and bindings contributed with Kimchi can support replacements and overrides in a few ways. This is handy when you, for example, want to provide different bindings/contributions for instrumentation tests or different build flavors.

**Using `replaces = […]` parameter**

All `@Contributes*` annotations support specify an array of class references in their `replaces` parameter, like so:

```kotlin
@ContributesBinding(
  scope = AppScope::class,
  replaces = [MenuRepositoryImpl::class],
)
class TestMenuRepository : MenuRepository //…
```
Then if both `TestMenuRepository` and `MenuRepositoryImpl` are on the classpath, Kimchi will read the `replaces` argument and replace the bindings. This logic follows for all the other contribution annotations and merging.

**Using `rank` parameter**

When using `@ContributesBinding`, if you don't have access to the class/implementation that you want to replace/override on the classpath (e.g. if you are doing a sort of `no-op` setup), then you can define a `rank` integer that kimchi will use to determine priority when merging  contributions of the same scope and bound on the classpath. The higher rank will always replace lower ranks. If components have the same rank then an error will be thrown.

e.g.
```kotlin
@ContributesBinding(
  scope = AppScope::class,
  rank = ContributesBinding.RANK_HIGHEST,
)
class NoOpMenuRepository : MenuRepository
```

> [!NOTE]   
> It is always recommended to use `replaces` instead of `rank` whenever possible. The `replaces` parameter will always take precedence no matter what you set the `rank` value to.

**Component Exclusions**

You can explicitly declare contributed elements on the classpath from being merged into components using the `excludes` parameter on the `@MergeComponent` and `@ContributesSubcomponent` annotations, like so:

```kotlin
@MergeComponent(
  scope = AppScope::class,
  excludes = [ SomeLegacyModule::class ],
)
abstract class AppComponent
```

In a perfect world this feature shouldn't be needed. However, due to legacy setups, poor modularization, and other constraints of modern day software development, applications might need to use it.

## Setup

[![Maven Central](https://img.shields.io/maven-central/v/com.r0adkll.kimchi/kimchi-compiler.svg)](https://search.maven.org/search?q=g:com.r0adkll.kimchi)
[![Sonatype Snapshot](https://img.shields.io/nexus/s/https/oss.sonatype.org/com.r0adkll.kimchi/kimchi-compiler.svg)](https://oss.sonatype.org/content/repositories/snapshots/com/r0adkll/kimchi/)

**`build.gradle.kts`**

```kotlin
plugins {
  id("org.jetbrains.kotlin.jvm") version "2.0.10"
  id("com.google.devtools.ksp") version "2.0.10-1.0.24"
}

dependencies {
  implementation("me.tatarka.inject:kotlin-inject-runtime:0.7.1")
  ksp("me.tatarka.inject:kotlin-inject-compiler-ksp:0.7.1")

  implementation("com.r0adkll.kimchi:kimchi-annotations:<latest_version>")
  ksp("com.r0adkll.kimchi:kimchi-compiler:<latest_version>")

  // Add these to support Circuit integration: https://github.com/slackhq/circuit
  implementation("com.r0adkll.kimchi:kimchi-circuit-annotations:<latest_version>")
  ksp("com.r0adkll.kimchi:kimchi-circuit-compiler:<latest_version>")
}
```

### Multiplatform

**`build.gradle.kts`**

```kotlin
plugins {
  id("org.jetbrains.kotlin.multiplatform") version "2.0.10"
  id("com.google.devtools.ksp") version "2.0.10-1.0.24"
}

kotlin {
  applyDefaultHierarchyTemplate()

  jvm()
  androidTarget()
  listOf(
    iosX64(),
    iosArm64(),
    iosSimulatorArm64()
  ).forEach {
    it.binaries.framework {
      baseName = "shared"
    }
  }

  sourceSets {
    commonMain.dependencies {
      implementation("me.tatarka.inject:kotlin-inject-runtime:0.7.1")
      implementation("com.r0adkll.kimchi:kimchi-annotations:<latest_version>")

      // Circuit integration: https://github.com/slackhq/circuit
      implementation("com.r0adkll.kimchi:kimchi-circuit-annotations:<latest_version>")
    }
  }
}
dependencies {
  add("kspJvm", "me.tatarka.inject:kotlin-inject-compiler-ksp:0.7.1")
  add("kspAndroid", "me.tatarka.inject:kotlin-inject-compiler-ksp:0.7.1")
  add("kspIosX64", "me.tatarka.inject:kotlin-inject-compiler-ksp:0.7.1")
  add("kspIosArm64", "me.tatarka.inject:kotlin-inject-compiler-ksp:0.7.1")
  add("kspIosSimulatorArm64", "me.tatarka.inject:kotlin-inject-compiler-ksp:0.7.1")

  add("kspJvm", "com.r0adkll.kimchi:kimchi-compiler:<latest_version>")
  add("kspAndroid", "com.r0adkll.kimchi:kimchi-compiler:<latest_version>")
  add("kspIosX64", "com.r0adkll.kimchi:kimchi-compiler:<latest_version>")
  add("kspIosArm64", "com.r0adkll.kimchi:kimchi-compiler:<latest_version>")
  add("kspIosSimulatorArm64", "com.r0adkll.kimchi:kimchi-compiler:<latest_version>")

  // Circuit integration: https://github.com/slackhq/circuit
  add("kspJvm", "com.r0adkll.kimchi:kimchi-circuit-compiler:<latest_version>")
  add("kspAndroid", "com.r0adkll.kimchi:kimchi-circuit-compiler:<latest_version>")
  add("kspIosX64", "com.r0adkll.kimchi:kimchi-circuit-compiler:<latest_version>")
  add("kspIosArm64", "com.r0adkll.kimchi:kimchi-circuit-compiler:<latest_version>")
  add("kspIosSimulatorArm64", "com.r0adkll.kimchi:kimchi-circuit-compiler:<latest_version>")
}
```

<details>
    <summary>Try this convenience function to mass apply ksp compilers to all targets</summary>

```kotlin
fun Project.addKspDependencyForAllTargets(dependencyNotation: Any) {
  val kmpExtension = extensions.getByType<KotlinMultiplatformExtension>()
  dependencies {
    kmpExtension.targets
      .asSequence()
      .filter { target ->
        // Don't add KSP for common target, only final platforms
        target.platformType != KotlinPlatformType.common
      }
      .forEach { target ->
        add(
          "ksp${target.targetName.capitalized()}",
          dependencyNotation,
        )
      }
  }
}

fun String.capitalized(): CharSequence = let<CharSequence, CharSequence> {
  if (it.isEmpty()) {
    it
  } else it[0].titlecase(
    Locale.getDefault(),
  ) + it.substring(1)
}
```
</details>

## License

```
Copyright 2024 r0adkll

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
