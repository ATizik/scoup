There is no active development of this project. If you need an alternative, please look at: https://github.com/square/workflow 

It has a Swift version, Kotlin Multiplatform coming in the future, and overall has better quality and uses more refined solutions.

If you need a more direct translation from this library, check this one: https://github.com/airbnb/MvRx

# scoup
[ ![Download](https://api.bintray.com/packages/atizik/Scoup/scoup/images/download.svg) ](https://bintray.com/atizik/Scoup/scoup/_latestVersion)

```
maven { url "https://dl.bintray.com/atizik/Scoup" }
compile 'ru.atizik:scoup:X.X.X' - look at badge above for latest version
```

Scoup is a scope oriented framework for Android development

## Why?
Since the introduction of Android Architecture Components we saw an increase in amount of developers using LiveData,ViewModel etc., or at least familiar with the general idea of these components. Unfortunately, they do have enough flaws that inspired the creation of this library. It features a similar workflow, so people familiar with AAC can use it without too much mental overhead, but extended with useful features.  

## Features:
1. ConflatedState - sane LiveData replacement, built on coroutines, with RxJava extensions. Automatic Lifecycle control, without coupling to Main thread. Set value from any context. MVI support coming in future versions.
2. Coordinators - scope oriented configuration change resistant component(a.k.a ViewModel in Android Architecture Components terminology). Since it uses Toothpick under the hood, it's just part of a usual DI graph, you can do whatever you want with it, don't create parentFragments or Activity-based ViewModels just to have a common ViewModel between Fragments, just put your parent Coordinator in a DI graph, or use convenince `getFlowCoordinatorInstance` function. Implements Disposable interface, and is disposed automatically on complete Fragment removal(no Activity support to encourage single Activity design, but nothing stops you from implementing lifecycle attachments to any component(Activity,plain View,Conductor etc.)
3. Lce(Loading-Content-Error) containers compatible with ConflatedState - a feature that's kinda implemented in AAC samples, named Resource, but without much extensibility. Lce allows you to define your own flows, for example if you don't want to show any errors if some data exists in cache, you can do it with single ConflatedState and Lce, without any magic envolved. 
4. BaseFragment built on delegates principle - keep your BaseFragment thin and keep everything flexible through configurable delegates. Just use implementation by delegation feature if you don't want to inherit from BaseFragment, applying exactly as much delegates as you need.
5. onSaveInstanceState/onRestoreInstanceState methods exposed to Coordinators


## Usage:
1. Setup Application class, importing `appScope` from the library:
```
        val toothpickConfig = if (BuildConfig.DEBUG) {
            Configuration.forDevelopment().disableReflection()
        } else
            Configuration.forProduction().disableReflection()
        Toothpick.setConfiguration(toothpickConfig)
        FactoryRegistryLocator.setRootRegistry(FactoryRegistry())
        MemberInjectorRegistryLocator.setRootRegistry(MemberInjectorRegistry())

        val appScope = Toothpick.openScope(appScope)
        appScope.installModules(
                SmoothieApplicationModule(this),
                ApplicationModule()
                // Application level modules
        )
        Toothpick.inject(Application, appScope)
```

2. Setup Application module, for example:
```ApplicationModule() = module {
    ...
    bindInstance<ErrorHandler> {
        object : ErrorHandler {
            override fun invoke(p1: Throwable) {
                if (BuildConfig.DEBUG) {
                    Log.e("ErrorHandler", p1.message)
                }
            }
        }
    }
    ``````
``````

3. Setup BaseFragment, using either MVI or MVVM pattern. 
Look for example here: https://github.com/ATizik/scoup/blob/master/library/src/main/java/ru/atizik/scoup/fragments/BaseFragment.kt
Do note, that you can inherit them, but it is recommended to write your own by example in case you ever need to inherit some other base fragment class.

Implement ArgumentReceiver in this fragment if you want to inject an argument in Coordinator constructor. Do note that this argument in most cases should be some kind of id that, for example, represents data in injected alongside it repository



4. Implement Coordinator like this:
```
class ExampleCoordinator @Inject constructor(errorHandler: ErrorHandler) : BaseCoordinator(errorHandler) {

}
```
Override onSaveInstanceState/onRestoreInstanceState inside Coordinator to save non-UI data across process death



Coming in future versions:
1. Data scoping(app session, user session etc.) with reactive bindings to ConflatedState
2. More delegates
3. Repositories extensions
4. Examples and complete guide, with pictures!

Feature requests and PRs are welcome
