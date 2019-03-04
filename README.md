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

Coming in future versions:
1. Data scoping(app session, user session etc.) with reactive bindings to ConflatedState
2. More delegates
3. Repositories extensions
4. Examples and complete guide, with pictures!

Feature requests and PRs are welcome
