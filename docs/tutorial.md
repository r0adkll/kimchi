Tutorial
========

This tutorial will help you get started with Kimchi with a simple restaurant menu app.

!!! warning

    This tutorial assumes some prior experience with [kotlin-inject](https://github.com/evant/kotlin-inject), [Circuit](https://github.com/slackhq/circuit), and [Jetpack Compose](https://developer.android.com/compose). Check out the following for more resources on these topics:

    * [Getting started with Jetpack Compose](https://developer.android.com/develop/ui/compose/documentation)
    * [Circuit Tutorial](https://slackhq.github.io/circuit/tutorial/)
    * [kotlin-inject README.md](https://github.com/evant/kotlin-inject)

## Setup

Follow the steps in the [Installation](setup.md) guide to get your project configured with Kimchi and kotlin-inject. This guide is going to assume that you are running a kotlin multiplatform app, but the concepts should be easy to apply to Android or Jvm applications.

## Create scope markers

### `AppScope`

We'll first want to define a scope to represent the application lifecycle. Dependencies and elements at this level of the graph are expected to be scoped to the entirety of the application and could live for the entire application lifecycle.

```kotlin title="common/…/AppScope.kt"
object AppScope
```

### `WindowScope`

Next, we'll define a scope to represent the lifecycle of our application window, i.e. `Activity` or `UIViewController`, that we'll use to collect all our UI dependencies to render in our app.

```kotlin title="common/…/WindowScope.kt"
object WindowScope
```

!!! tip

    You'll want to create your scopes in a common module that can be made accessible to all other modules that plan to contribute to it. For example, I will typically have a `:core` module that has no dependencies and contains only elements like this.

## Create an `AppComponent`

Next, we'll want to create a component that will merge all our dependencies and components contributed to our `AppScope`

=== "androidApp"

    ```kotlin title="AndroidAppComponent.kt"
    @SingleIn(AppScope::class)
    @MergeComponent(AppScope::class)
    abstract class AndroidAppComponent(
      @get:Provides val application: Application,
    ) {
      // The compiler uses this as an extension point to create the underlying merged component
      companion object
    }
    ```

=== "desktopApp"

    ```kotlin title="DesktopAppComponent.kt"
    @SingleIn(AppScope::class)
    @MergeComponent(AppScope::class)
    interface DesktopAppComponent {
      // The compiler uses this as an extension point to create the underlying merged component
      companion object
    }
    ```

=== "shared/iosMain"

    ```kotlin title="IosAppComponent.kt"
    @SingleIn(AppScope::class)
    @MergeComponent(AppScope::class)
    interface IosApplicationComponent {
      // The compiler uses this as an extension point to create the underlying merged component
      companion object
    }
    ```

!!! abstract "Multiplatform"

    Typically you'll create the root-level component, i.e. our `AppComponent`, in the application target module of your setup. This allows you to provide to the graph platform specific elements at the root level (like our `Application` class on Android) that can be used for platform-specific dependencies in other modules.

Then just instantiate your component to create and start using your graph

```kotlin
class RestaurantApp : Application() {
  override fun onCreate() {
    super.onCreate()
    val appComponent = AndroidAppComponent.createAppComponent(this)
    // …
  }
}
```

???+ note

    For the rest of this tutorial we will be using a `ComponentHolder` pattern to store and access all our component, and subcomponent elements. You can see examples of this in the `:sample` modules, but it essentially looks like this:
    ```kotlin
    object ComponentHolder {
      var components = mutableSetOf<Any>()

      inline fun <reified T> component(): T {
        return components
          .filterIsInstance<T>()
          .firstOrNull()
          ?: throw IllegalArgumentException("Unable to find a component for type '${T::class.qualifiedName}'")
        }
    }

    // RestaurantApp.kt
    val appComponent = AndroidAppComponent.createAppComponent(this)
    ComponentHolder.components += appComponent
    ```

## Create a `WindowComponent`

Now lets create the component that will collect our UI and essentially encapsulate the visual portion of our app.

```kotlin title="WindowComponent.kt"
@SingleIn(WindowScope::class) // (1)!
@MergeSubcomponent(
  scope = WindowScope::class,
  parentScope = AppScope::class,
)
interface WindowComponent {
  val restaurantContent: RestaurantContent // (2)!

  @Provides
  @SingleIn(WindowScope::class)
  fun provideCircuit(
    uiFactories: Set<Ui.Factory>,
    presenterFactories: Set<Presenter.Factory>,
  ): Circuit = Circuit.Builder() // (3)!
    .addUiFactories(uiFactories)
    .addPresenterFactories(presenterFactories)
    .build()

  @MergeSubcomponent.Factory
  interface Factory { // (4)!
    fun create(
      activity: Activity, // (5)!
    ): WindowComponent
  }
}
```

1.  A kotlin-inject scope wrapper so we can re-use our Kimchi scope markers
2.  This is an injectable composable function typealias that will act like our entry point into the application and our injected code
3.  This tutorial will be using Circuit for our presentation architecture
4.  The merged parent component, for `parentScope`, will end up implementing this interface. Making it easy to retrieve using our `ComponentHolder` pattern
5.  This will be provided to the merged dependency graph

## "Build" the graph

Now with the basic shape of our graph in place, lets finish creating our components and begin rendering our app

```kotlin title="MainActivity.kt"
class MainActivity : ComponentActivity {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // (1)
    val windowComponent = ComponentHolder.component<WindowComponent.Factory>()
      .create(this)

    // (2)
    ComponentHolder.components += windowComponent

    setContent {
      // (3)
      activityComponent.restaurantContent(
        Modifier,
      )
    }
  }
}
```

1.  Since our `AppComponent` we created earlier is already in our holder we can simply grab the factory of our contributed subcomponent
2.  We want to make sure that this component is also on the graph so contributed subcomponents to the `WindowScope` can be accessed
3.  Our injected composable function itself injects `Circuit` and performs the necessary boilerplate to render a Circuit app

!!! warning "Wait!"

    If you try building now we'll get an error that nothing has been provided for Circuit's `Ui.Factory` and `Ui.Presenter`

## Create a screen

So let's create a screen to render!

```kotlin title="MenuUi.kt" hl_lines="9"
@Composable
fun MenuUi(
  state: MenuUiState,
  modifier: Modifier = Modifier,
) {
  // Some UI code...
}

@ContributesTo(UiScope::class) // (1)!
interface MenuUiFactoryComponent {
  @IntoSet
  @Provides
  fun bindMenuUiUiFactory(factory: MenuUiUiFactory): Ui.Factory = factory
}

@Inject
class MenuUiFactory : Ui.Factory {
  override fun create(screen: Screen, context: CircuitContext): Ui<*>? = when(screen) {
    is MenuScreen -> {
      ui<MenuUiState> { state, modifier -> MenuUi(state, modifier) }
    }
    else -> null
  }
}

```

1.  Kimchi will contribute this binding to the component we made above!

```kotlin title="MenuPresenter.kt" hl_lines="12"
@Inject
class MenuPresenter(
  @Assisted private val navigator: Navigator
) : Presenter<MenuUiState>() {

  @Composable
  override fun present(): MenuUiState {
    return MenuUiState()
  }
}

@ContributesTo(WindowScope::class) // (1)!
interface MenuPresenterFactoryComponent {
  @IntoSet
  @Provides
  fun bindMenuPresenterFactory(factory: MenuPresenterFactory): Presenter.Factory = factory
}

@Inject
class MenuPresenterFactory(
  private val presenterFactory: (navigator: Navigator) -> MenuPresenter,
) : Presenter.Factory {
  override fun create(
    screen: Screen,
    navigator: Navigator,
    context: CircuitContext,
  ): Presenter<*>? = when(screen) {
    is MenuScreen -> presenterFactory(navigator)
    else -> null
  }
}

```

1.  Kimchi will contribute this binding to the scope we made above!

!!! tip

    Using the out-of-the-box circuit artifacts Kimchi can generate the above boilerplate DI using a `CircuitInject` annotation.
    Just add these artifacts, following the [Installation](setup.md) guide.

      - `com.r0adkll.kimchi:kimchi-circuit-annotations`
      - `com.r0adkll.kimchi:kimchi-circuit-compiler`

    Then use the annotation like so:

    ```kotlin title="MenuUi.kt" hl_lines="1"
    @CircuitInject(MenuScreen::class, WindowScope::class)
    @Composable
    fun MenuUi(
      state: MenuUiState,
      modifier: Modifier = Modifier,
    ) //…
    ```

    ```kotlin title="MenuUi.kt" hl_lines="1"
    @CircuitInject(MenuScreen::class, WindowScope::class)
    @Inject
    class MenuPresenter(
      @Assisted private val navigator: Navigator
    ) : Presenter<MenuUiState>() //…
    ```



## Create a dependency

Next, let's create a simple repository to provide a `Menu` object for our UI to render.

```kotlin title="MenuRepository.kt"
interface MenuRepository {

  suspend fun getMenu(): Menu
}
```

Now create the implementation and bind it to the `AppScope` using `@ContributesBinding`. For now this will be very basic and we'll look at tying in more features later.

```kotlin title="RealMenuRepository.kt" hl_lines="1"
@ContributesBinding(AppScope::class)
@Inject
class RealMenuRepository : MenuRepository {

  override suspend fun getMenu(): Menu {
    return Menu(
      title = "The Kimchi Restaurant",
    )
  }
}
```

Now inject and use it
```kotlin title="MenuPresenter.kt"
@Inject
class MenuPresenter(
  @Assisted private val navigator: Navigator,
  private val menuRepository: MenuRepository,
) : Presenter<MenuUiState>() {
  @Composable
  override fun present(): MenuUiState {
    val menu by remember {
      flow { emit(menuRepository.getMenu()) }
    }.collectAsState(null)

    return MenuUiState(menu)
  }
}
```

## Conclusion

This is just a brief example of Kimchi and what you can do. Check out more of the docs for more detailed dives into each feature of Kimchi and checkout the `circuit/` module for an example on how to extend Kimchi.
