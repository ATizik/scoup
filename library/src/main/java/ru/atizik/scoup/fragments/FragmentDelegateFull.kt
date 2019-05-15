package ru.atizik.scoup.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import android.view.View

open class FragmentDelegateFull : LateinitFragment, FragmentManager.FragmentLifecycleCallbacks() {

    override lateinit var fragmentDelegate: Fragment
    lateinit var scopeTag: String
    override fun init(fragment: Fragment) {
        fragmentDelegate = fragment
        scopeTag = fragment.arguments!!.getString(scopeArg)!!
        fragment.requireFragmentManager().registerFragmentLifecycleCallbacks(this, false)
    }

    /**
     * Check if we're receiving callback from the same fragment that registered this callbacks
     */
    private fun Fragment.doOnSameScope(action: () -> Unit) {
        if (arguments?.getString(scopeArg) == scopeTag) action()
    }


    final override fun onFragmentStopped(fm: FragmentManager, f: Fragment) {
        f.doOnSameScope {
            onStopped()
        }
    }

    open fun onStopped() = Unit


    final override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        f.doOnSameScope {
            onCreated(savedInstanceState)
        }
    }

    open fun onCreated(savedInstanceState: Bundle?) = Unit

    final override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) =
        Unit

    //TODO: implement rest of overrides
    final override fun onFragmentResumed(fm: FragmentManager, f: Fragment) = Unit

    final override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) = Unit

    final override fun onFragmentPreAttached(fm: FragmentManager, f: Fragment, context: Context) = Unit

    final override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
        f.doOnSameScope {
            onDestroyed()
        }
    }

    open fun onDestroyed() = Unit

    final override fun onFragmentSaveInstanceState(fm: FragmentManager, f: Fragment, outState: Bundle) {
        f.doOnSameScope {
            onSaveInstanceState(outState)
        }
    }

    open fun onSaveInstanceState(outState: Bundle) = Unit


    final override fun onFragmentStarted(fm: FragmentManager, f: Fragment) = Unit

    final override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
        f.doOnSameScope {
            onViewDestroyed()
        }
    }

    open fun onViewDestroyed() = Unit

    final override fun onFragmentPreCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        f.doOnSameScope {
            onPreCreated(savedInstanceState)
        }
    }

    open fun onPreCreated(savedInstanceState: Bundle?) = Unit

    final override fun onFragmentActivityCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) = Unit

    final override fun onFragmentPaused(fm: FragmentManager, f: Fragment) = Unit

    final override fun onFragmentDetached(fm: FragmentManager, f: Fragment) = Unit
}