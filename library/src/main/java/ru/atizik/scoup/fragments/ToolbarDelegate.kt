package ru.atizik.scoup.fragments

import android.arch.lifecycle.LifecycleOwner
import android.support.v7.widget.Toolbar


//TODO:Implement and document

interface ToolbarDelegate:LateinitFragment {
    var toolbarBuilder: Toolbar.()->Unit
}

class ToolbarDelegateImpl(override var toolbarBuilder: Toolbar.() -> Unit = {}):FragmentDelegate(),ToolbarDelegate {
    lateinit var toolbar: Toolbar


    override fun onStart(owner: LifecycleOwner) {
 /*       toolbar = Toolbar(fragmentDelegate.context)
        val currentLayout = ((fragmentDelegate.view as? ConstraintLayout)
            ?: (fragmentDelegate.view as ViewGroup?)?.firstChildOrNull { it is ConstraintLayout && (it.parent is ScrollView || it.parent is NestedScrollView) }) as? ConstraintLayout
            ?: return*/

    }

    override fun onDestroy(owner: LifecycleOwner) {
        /*(toolbar.parent as? ViewGroup)?.removeView(toolbar)*/
    }
}