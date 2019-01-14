package com.atizik.scoupexample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.first_fragment.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.channels.consumeEach
import ru.atizik.scoup.ConflatedState
import ru.atizik.scoup.di.getCoordinatorInstance
import ru.atizik.scoup.fragments.BaseFragment
import ru.atizik.scoup.fragments.FlowInjector
import ru.atizik.scoup.fragments.FragmentDelegate
import ru.atizik.scoup.fragments.FragmentInjectorImpl
import ru.atizik.scoup.subscribe
import ru.atizik.scoup.viewmodel.BaseCoordinator
import ru.atizik.scoup.viewmodel.ErrorHandler
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragmentHost, MainFragment())
                .commitNow()


            supportFragmentManager!!.beginTransaction()
                .add(R.id.fragmentHost, FirstFragment().also { frag.add(it) })
                .commitNow()
        }
    }

    override fun onBackPressed() {
        ///super.onBackPressed()
        frag.forEach {
            supportFragmentManager.beginTransaction().remove(it).commitNow()
        }

    }
}

var frag = mutableListOf<Fragment>()
const val flowTag = "flowTag"

class MainFragment : BaseFragment<MainCoordinator>(MainCoordinator::class.java) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}

class MainCoordinator @Inject constructor(errorHandler: ErrorHandler) : BaseCoordinator(errorHandler)

class FirstFragment : BaseFragment<FirstCoordinator>(
    FirstCoordinator::class.java,
    injector = FragmentInjectorImpl(injector = FlowInjector(flowTag))
) {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.first_fragment, null)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(coordinatorOwner) {
            coordinatorOwner
                .coordinator
                .conflatedState
                .observe()
                .onEach { textView.text = it.toString() }
                .subscribe()
        }
        if (savedInstanceState == null)
            fragmentManager!!.beginTransaction().add(R.id.fragmentHost, SecondFragment().also { frag.add(it) }).commit()
    }
}


class SecondFragment : BaseFragment<SecondCoordinator>(
    SecondCoordinator::class.java,
    injector = FragmentInjectorImpl(injector = FlowInjector(flowTag))
) {

}

class FirstCoordinator @Inject constructor(errorHandler: ErrorHandler) : BaseCoordinator(errorHandler) {
    val conflatedState = ConflatedState(1)
    val some = getCoordinatorInstance<FlowCoordinator>(flowTag)

    init {
        launch {
            while (isActive) {

                delay(10)
                conflatedState.value = conflatedState.value + 1
            }
        }
    }

}

class FlowCoordinator @Inject constructor(errorHandler: ErrorHandler) : BaseCoordinator(errorHandler) {

    override fun dispose() {
        super.dispose()
        Log.i(flowTag, "Flow Coordinator disposed")
    }
}

class SecondCoordinator @Inject constructor(errorHandler: ErrorHandler) : BaseCoordinator(errorHandler) {}
