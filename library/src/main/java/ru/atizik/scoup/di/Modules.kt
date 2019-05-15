package ru.atizik.scoup.di

import android.os.Parcelable
import androidx.fragment.app.Fragment
import ru.atizik.scoup.fragments.SCOUP_ARG
import ru.atizik.scoup.fragments.ScopeCounter
import ru.atizik.scoup.viewmodel.BaseCoordinator
import toothpick.config.Module

//TODO:Document
fun ViewModelModule(viewModelClass: Class<out BaseCoordinator>) = module {
    bind(viewModelClass).apply { singletonInScope() }
        bindInstance {
            ScopeCounter()
        }

}

fun ViewModelArgument(viewModelArgumentClass: Class<out Parcelable>,fragment: Fragment) = object: Module() {
    init {
        bind(viewModelArgumentClass).toInstance(fragment.arguments!!.getParcelable(SCOUP_ARG))
    }
    //bind(viewModelArgumentClass).apply { toInstance(fragment.arguments?.getParcelable("__SCOUP_ARG")) }
}