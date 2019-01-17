package ru.atizik.scoup.di

import ru.atizik.scoup.fragments.ScopeCounter
import ru.atizik.scoup.viewmodel.BaseCoordinator
import ru.atizik.scoup.viewmodel.ErrorHandler

//TODO:Document
fun ViewModelModule(viewModelClass: Class<out BaseCoordinator>) = module {
    bind(viewModelClass).apply { singletonInScope() }
        bindInstance {
            ScopeCounter()
        }

}