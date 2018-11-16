# scoup
[ ![Download](https://api.bintray.com/packages/atizik/Scoup/scoup/images/download.svg) ](https://bintray.com/atizik/Scoup/scoup/_latestVersion)

```
maven { url "https://dl.bintray.com/atizik/Scoup" }
compile 'ru.atizik:scoup:X.X.X' - look at badge above for latest version
```

Scoup is a scope oriented framework for Android development

Features:
1. ConflatedState - sane LiveData replacement, built on coroutines, with RxJava extensions
2. Coordinators - scope oriented configuration change resistant component(a.k.a ViewModel in Android Architecture Components terminology)
3. Lce(Loading-Content-Error) containers built-in in ConflatedState 
4. BaseFragment built on delegates principle - keep your BaseFragment thin and keep everything flexible through configurable delegates

Coming in future versions:
1. onSaveInstance state method exposed to Coordinators
2. Data scoping(app session, user session etc.) with reactive bindings to ConflatedState
3. More delegates
4. Repositories extensions
5. Examples and complete guide, with pictures!

Feature requests are welcome
