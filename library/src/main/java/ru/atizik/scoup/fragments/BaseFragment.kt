package ru.atizik.scoup.fragments

import android.content.Context
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.app.Fragment
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.map
import ru.atizik.scoup.viewmodel.BaseCoordinator
import ru.atizik.scoup.viewmodel.DisposableScope
import kotlin.coroutines.CoroutineContext

//TODO:Document
abstract class BaseFragment<V : BaseCoordinator>(
    clazz: Class<V>,
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

    override fun onDestroyView() {
        super.onDestroyView()
        coroutineContext.cancelChildren()
        disposable.clear()
    }

}