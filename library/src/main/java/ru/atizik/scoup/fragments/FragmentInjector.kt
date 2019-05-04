package ru.atizik.scoup.fragments

import androidx.lifecycle.LifecycleOwner
import android.os.Bundle
import androidx.fragment.app.Fragment
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import ru.atizik.scoup.di.module
import ru.atizik.scoup.viewmodel.DisposableScope
import toothpick.Scope
import toothpick.Toothpick
import toothpick.config.Module
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

const val appScope = "APP_SCOPE"
const val scopeArg = "SCOPE_TAG_ARG"

interface FragmentInjector : LateinitFragment {
    val modules: MutableList<Module>
    val parentScopes: (() -> List<Any>)?
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
class FragmentInjectorImpl(
    override val scopeBuilder: (Scope.() -> Unit)? = null,
    override var scopeTag: String = UUID.randomUUID().toString(),
    override val modules: MutableList<Module> = mutableListOf(),
    override val parentScopes: (() -> List<Any>)? = null,
    private val injector: Injector = SimpleInjector()
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
            val args = fragment.arguments ?: Bundle().also { fragment.arguments = it }
            args.putString(scopeArg, scopeTag)
        }

        injector.inject(
            parentScopes?.invoke() ?: listOf(fragmentDelegate.parentFragment?.getScopeTag() ?: appScope),
            modules,
            fragment,
            scopeTag,
            scopeBuilder
        )
    }

    override fun onDestroy(owner: LifecycleOwner) {
        if (fragmentDelegate.isRemovingCompat()) {
            injector.close(scopeTag)
        }
    }
}

fun Fragment.getScopeTag(): String = arguments!!.getString(scopeArg)!!


interface Injector {
    fun inject(
        scopes: List<Any>,
        modules: List<Module>,
        obj: Any,
        scopeTag: Any,
        scopeBuilder: (Scope.() -> Unit)?
    )

    fun close(injectionRecipientTag: Any)
}

class SimpleInjector() : Injector {
    override fun inject(
        scopes: List<Any>,
        modules: List<Module>,
        obj: Any,
        scopeTag: Any,
        scopeBuilder: (Scope.() -> Unit)?
    ) {
        inject(scopes, scopeTag, scopeBuilder, modules, obj)
    }

    override fun close(injectionRecipientTag: Any) {
        Toothpick.closeScope(injectionRecipientTag)
    }
}

private fun inject(
    scopes: List<Any>,
    scopeTag: Any,
    scopeBuilder: (Scope.() -> Unit)?,
    modules: List<Module>,
    obj: Any
) {
    val scope = Toothpick.openScopes(*(scopes + scopeTag).toTypedArray())
    scopeBuilder?.invoke(scope)
    scope.installModules(*modules.toTypedArray())
    Toothpick.inject(obj, scope)
}

class ScopeCounter @Inject constructor(): Disposable {
    val disposables = CompositeDisposable()

    override fun isDisposed(): Boolean = set.get() == 0


    override fun dispose() {
        if(set.decrementAndGet()<=0) {
            disposables.clear()
        }
    }

    val set = AtomicInteger(0)
}

//*-
//;LLLLTJU89TGLLL
//PictureInPictureParamsKUJHY
//-09876543WQ
// 5.BUHNIMJO[
//FKTYJXRF  ,ELJDBX KEXIFZ LTDXJYRF
//] 75/9ШГ7Н5УК32ЦЫ3В4УКА5ПТРЬ7БОГЮШ.ЩЖзэх

