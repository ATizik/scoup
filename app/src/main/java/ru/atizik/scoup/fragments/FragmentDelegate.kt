package ru.atizik.scoup.fragments

import android.arch.lifecycle.*
import android.support.v4.app.Fragment

interface LateinitFragment {
    fun init(fragment: Fragment)
}

/**
 * Repeats every lifecycle callback of fragmentDelegate.
 */
abstract class FragmentDelegate : LifecycleOwner, DefaultLifecycleObserver, LateinitFragment {

    val lifecycle = LifecycleRegistry(this)
    lateinit var fragmentDelegate: Fragment

    override fun getLifecycle(): Lifecycle = lifecycle

    override fun init(fragment: Fragment) {
        fragmentDelegate = fragment
        onInit(fragment)
        fragment.lifecycle.addObserver(this@FragmentDelegate)
    }

    /**
     *  Always called on initialization
     */
    open fun onInit(fragment: Fragment) {}
}

infix fun <T : FragmentDelegate> T.to(list: MutableList<FragmentDelegate>): T {
    list += this
    return this
}

fun List<FragmentDelegate>.init(fragment: Fragment) {
    forEach { it.init(fragment) }
}