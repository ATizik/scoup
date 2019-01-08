package ru.atizik.scoup.di

import ru.atizik.scoup.viewmodel.BaseCoordinator

//TODO:Document
fun ViewModelModule(viewModelClass: Class<out BaseCoordinator>) = module {
    bind(viewModelClass).apply { singletonInScope() }
}