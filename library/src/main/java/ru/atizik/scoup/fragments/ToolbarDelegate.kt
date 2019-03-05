package ru.atizik.scoup.fragments

import android.arch.lifecycle.LifecycleOwner
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.v4.widget.NestedScrollView
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import ru.atizik.scoup.R
import java.util.*


//TODO:Implement and document

interface ToolbarDelegate:LateinitFragment {
    var toolbarBuilder: (Toolbar.()->Unit)?
}

class ToolbarDelegateImpl(override var toolbarBuilder: (Toolbar.() -> Unit)? = null, private val textAppearance: Int? = null):FragmentDelegate(),ToolbarDelegate {
    lateinit var toolbar: Toolbar

    override fun onStart(owner: LifecycleOwner) {
        if (toolbarBuilder == null)
            return

        toolbar = Toolbar(fragmentDelegate.context)
        val currentLayout = ((fragmentDelegate.view as? ConstraintLayout)
            ?: (fragmentDelegate.view as ViewGroup?)?.firstChildOrNull { it is ConstraintLayout && (it.parent is ScrollView || it.parent is NestedScrollView) }) as? ConstraintLayout
            ?: return


        toolbarBuilder?.let { toolbar.it() }
        textAppearance?.let { toolbar.setTitleTextAppearance(fragmentDelegate.context, textAppearance) }

        (toolbar.parent as? ViewGroup)?.let {
            it.removeView(toolbar)
            currentLayout.addView(toolbar)
            val set = ConstraintSet()
            set.clone(currentLayout)
            set.connect(
                currentLayout.firstChild { true }.id,
                ConstraintSet.TOP,
                toolbar.id,
                ConstraintSet.BOTTOM
            )
            set.applyTo(currentLayout)
            return
        }


        val toolbarParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        toolbar.layoutParams = toolbarParams
        toolbar.id = R.id.toolbar

        currentLayout.addView(toolbar)
        val set = ConstraintSet()
        set.clone(currentLayout)
        set.connect(
            currentLayout.firstChild { true }.id,
            ConstraintSet.TOP,
            toolbar.id,
            ConstraintSet.BOTTOM
        )
        set.applyTo(currentLayout)


    }

    override fun onDestroy(owner: LifecycleOwner) {
        if (toolbarBuilder == null)
            return
        (toolbar.parent as? ViewGroup)?.removeView(toolbar)
    }
}

inline fun ViewGroup.firstChildOrNull(predicate: (View) -> Boolean): View? {
    for (i in 0 until childCount) {
        val child = getChildAt(i)
        if (predicate(child)) {
            return child
        }
    }
    return null
}


inline fun ViewGroup.firstChild(predicate: (View) -> Boolean): View {
    return firstChildOrNull(predicate)
        ?: throw NoSuchElementException("No element matching predicate was found.")
}