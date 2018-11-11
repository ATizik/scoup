package ru.atizik.scoup.fragments

import android.arch.lifecycle.*
import android.support.v4.app.Fragment

interface LateinitFragment {
    fun init(fragment: Fragment)
    var fragmentDelegate: Fragment
}

/**
 * Repeats every lifecycle callback of fragmentDelegate.
 */
abstract class FragmentDelegate : LifecycleOwner, DefaultLifecycleObserver, LateinitFragment {

    val lifecycle = LifecycleRegistry(this)
    override lateinit var fragmentDelegate: Fragment

    override fun getLifecycle(): Lifecycle = lifecycle

    override fun init(fragment: Fragment) {
        fragmentDelegate = fragment
        onInit(fragment)
        fragment.lifecycle.addObserver(this@FragmentDelegate)
    }

    /**
     *  Always called on initialization, unlike other lifecycle methods
     *  than can be skipped if [init] was called later in lifecycle
     */
    open fun onInit(fragment: Fragment) {}
}