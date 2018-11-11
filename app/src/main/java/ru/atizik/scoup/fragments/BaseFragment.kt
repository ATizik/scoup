package ru.atizik.scoup.fragments

import android.content.Context
import android.support.v4.app.Fragment
import ru.atizik.scoup.viewmodel.BaseCoordinator

//TODO:Document
abstract class BaseFragment<V : BaseCoordinator>(
    clazz: Class<V>,
    protected val injector: FragmentInjector = FragmentInjectorImpl(),
    protected val coordinatorOwner: CoordinatorOwner<V> = CoordinatorOwnerImpl(clazz),
    protected val toolbarDelegate: ToolbarDelegate? = ToolbarDelegateImpl()
) : Fragment() {

    override fun onAttach(context: Context) {
        super.onAttach(context)
        injector.init(this)
        coordinatorOwner.init(this)
        toolbarDelegate?.init(this)
    }


}