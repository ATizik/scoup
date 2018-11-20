package ru.atizik.scoup.fragments

import android.arch.lifecycle.LifecycleOwner
import android.os.Bundle
import android.support.v4.app.Fragment
import ru.atizik.scoup.di.bind
import ru.atizik.scoup.di.module
import toothpick.Scope
import toothpick.Toothpick
import toothpick.config.Module
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

const val appScope = "APP_SCOPE"
const val scopeArg = "SCOPE_TAG_ARG"

interface FragmentInjector:LateinitFragment {
    val modules: MutableList<Module>
    val parentScopes: (()->List<Any>)?
    val scopeBuilder: (Scope.() -> Unit)?
    val scopeTag: String
}

/**
 * Injects dependencies in Fragment respecting it's lifecycle
 * [scopeTag] is generated on first initialization and is stored in fragment arguments
 * until the scope closed on fragment removal.
 * Parent scopes are assumed to be correctly binded, therefore it's enough to attachLifecycle to the closest parent(parentFragment if such exists, otherwise activity)
 *
 */
class FragmentInjectorImpl(override val scopeBuilder: (Scope.() -> Unit)? = null,
                           override var scopeTag: String = UUID.randomUUID().toString(),
                           override val modules: MutableList<Module> = mutableListOf(),//get parent scope during instantiation?
                           override val parentScopes: (() -> List<Any>)? = null,
                           val injector:
) : FragmentDelegate(), FragmentInjector {

    /**
     * Generates unique scopeTag on first initialization and saves it in arguments bundle
     * On consequent recreation reuses scope by accessing scopeTag
     */
    override fun onInit(fragment: Fragment) {

        val scopeT = fragment.arguments?.getString(scopeArg)

        if (scopeT != null) {
            scopeTag = scopeT
        } else {
            val args = fragment.arguments ?: Bundle()
            args.putString(scopeArg, scopeTag)
            fragment.arguments = args
        }

        injector.inject(parentScopes?.invoke() ?: listOf(fragmentDelegate.parentFragment?.getScopeTag() ?: appScope), modules, fragment, scopeTag, scopeBuilder)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        if (fragmentDelegate.isRemovingCompat()) {
            injector.close(scopeTag)
        }
    }
}

fun Fragment.getScopeTag(): String = arguments!!.getString(scopeArg)!!


interface Injector {
    fun inject(scopes: List<Any>, modules: List<Module>, obj: Any, scopeTag: Any, scopeBuilder: (Scope.() -> Unit)?)
    fun close(injectionRecipientTag: Any)
}

class SimpleInjector() : Injector {
    override fun inject(scopes: List<Any>, modules: List<Module>, obj: Any, scopeTag: Any, scopeBuilder: (Scope.()->Unit)?) {
        val scope = Toothpick.openScopes(*(scopes + scopeTag).toTypedArray())
        scopeBuilder?.invoke(scope)
        scope.installModules(*modules.toTypedArray())
        Toothpick.inject(obj, scope)
    }

    override fun close(injectionRecipientTag: Any) {
        Toothpick.closeScope(injectionRecipientTag)
    }
}

class FlatInjector() : Injector {
    override fun inject(scopes: List<Any>, modules: List<Module>, obj: Any, scopeTag: Any, scopeBuilder: (Scope.()->Unit)?) {
        val scope = Toothpick.openScopes(*(scopes + scopeTag).toTypedArray())
        module { bind().apply {sin} }
        scopeBuilder?.invoke(scope)
        scope.installModules(*modules.toTypedArray())
        Toothpick.inject(obj, scope)
    }

    override fun close(injectionRecipientTag: Any) {
        Toothpick.closeScope(injectionRecipientTag)
    }
}

@Singleton
class ScopeCounter @Inject constructor() {
    val map = ConcurrentHashMap<Any,    >()
}
