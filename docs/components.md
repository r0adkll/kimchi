Components
==========

Components are the root building block for creating dependency graphs with [kotlin-inject](https://github.com/evant/kotlin-inject). For vanilla usage, it might look something like this:

```kotlin
@Component
abstract class AppComponent(
  @get:Provides protected val application: Application,
) // { …
```

kotlin-inject will generate an implementation to this class providing constructor passed arguments and solving for abstract property/functions to expose dependencies, looking something like:

```kotlin
public class InjectAppComponent(
  application: Application,
) : AppComponent(application), ScopedComponent // { …

public fun KClass<MergedAndroidAppComponent>.create(application: Application):
  MergedAndroidAppComponent = InjectMergedAndroidAppComponent(application)
```

## **`@MergeComponent`**

Kimchi extends this pattern with the `@MergeComponent` annotation where it will generate that abstract class for you, complete with merged elements, that will then get picked up by kotlin-inject and generate the above. In practice it should look something like this:

```kotlin
@MergeComponent(AppScope::class)
abstract class AppComponent(
  @get:Provides protected val application: Application,
) // { …
```

Under the hook this will generate a merged abstract implementation of our component, merging in all the contributed bindings, multi-bindings, modules and subcomponents to the scope that we specify. Here is an example of what that can look like:

```kotlin hl_lines
@Component
abstract class MergedAppComponent(
  application: Application,
) : AppComponent(application), ContributedComponentInterface {

  // Contributed binding
  fun RealMenuRepository.bind: MenuRepository
    @Provides get() = this
}

fun AppComponent.Companion.createAppComponent(): MergedAppComponent
  = MergedAppComponent::class.create()
```

Which in turn outputs

```kotlin

```
