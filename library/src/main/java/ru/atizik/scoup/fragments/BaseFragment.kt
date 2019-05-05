package ru.atizik.scoup.fragments

import android.content.Context
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.fragment.app.Fragment
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.map
import ru.atizik.scoup.viewmodel.BaseCoordinator
import ru.atizik.scoup.viewmodel.DisposableScope
import ru.atizik.scoup.viewmodel.MvState
import ru.atizik.scoup.viewmodel.StateCoordinator
import kotlin.coroutines.CoroutineContext

//TODO:Document
abstract class BaseFragment<V : BaseCoordinator>(
    clazz: Class<V>/**use [infer] in inheritors here*/,
    protected val injector: FragmentInjector = FragmentInjectorImpl(),
    protected val coordinatorOwner: CoordinatorOwner<V> = CoordinatorOwnerImpl(clazz),
    protected val toolbarDelegate: ToolbarDelegate? = ToolbarDelegateImpl()
) : Fragment(), DisposableScope, CoordinatorOwner<V> by coordinatorOwner {

    override val coroutineContext: CoroutineContext = SupervisorJob()
    override val disposable: CompositeDisposable = CompositeDisposable()


    override fun onAttach(context: Context) {
        super.onAttach(context)
        injector.init(this)
        coordinatorOwner.init(this)
        toolbarDelegate?.init(this)
    }

}


abstract class BaseFragmentState<T : MvState, V : StateCoordinator<T>>(
    clazz: Class<V>/**use [infer] in inheritors here*/ ,
    protected val injector: FragmentInjector = FragmentInjectorImpl(),
    protected val coordinatorOwner: CoordinatorOwnerState<T, V> = CoordinatorOwnerStateImpl(clazz),
    protected val toolbarDelegate: ToolbarDelegate? = ToolbarDelegateImpl()
) : Fragment(), DisposableScope, CoordinatorOwnerState<T, V> by coordinatorOwner {

    override val coroutineContext: CoroutineContext = SupervisorJob()
    override val disposable: CompositeDisposable = CompositeDisposable()


    override fun onAttach(context: Context) {
        super.onAttach(context)
        injector.init(this)
        coordinatorOwner.init(this)
        toolbarDelegate?.init(this)
    }

}

inline fun <reified V> infer():Class<V> = V::class.java