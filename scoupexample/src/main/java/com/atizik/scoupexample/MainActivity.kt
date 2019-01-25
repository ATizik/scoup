package com.atizik.scoupexample

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.first_fragment.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.channels.consumeEach
import ru.atizik.scoup.ConflatedState
import ru.atizik.scoup.Lce
import ru.atizik.scoup.di.getCoordinatorInstance
import ru.atizik.scoup.di.getFlowCoordinatorInstance
import ru.atizik.scoup.fragments.*
import ru.atizik.scoup.onContent
import ru.atizik.scoup.subscribe
import ru.atizik.scoup.viewmodel.BaseCoordinator
import ru.atizik.scoup.viewmodel.ErrorHandler
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("SAVED:", savedInstanceState.toString())
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
    FirstCoordinator::class.java
) {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.first_fragment, null)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        coordinator
            .conflatedState
            .observe()
            .onEach { textView.text = it.toString() }
            .subscribe()



        textView.setOnClickListener { coordinator.onClick() }

        if (savedInstanceState == null)
            fragmentManager!!.beginTransaction().add(R.id.fragmentHost, SecondFragment()
                .also {
                    frag.add(it)
                    it.putArgs(SecondArgument(192)).putString("WHAT", "WHAT")
                }).commit()
    }
}

@Parcelize
data class SecondArgument(val something: Int) : Parcelable

class SecondFragment : BaseFragment<SecondCoordinator>(SecondCoordinator::class.java),
    ArgumentReceiver<SecondArgument> {
    override val argumentClazz: Class<SecondArgument> = SecondArgument::class.java

    override fun onStart() {
        super.onStart()
        print(coordinatorOwner.coordinator.secondArgument)
    }
}

class FirstCoordinator @Inject constructor(errorHandler: ErrorHandler) : BaseCoordinator(errorHandler) {
    val conflatedState:ConflatedState<Int> = ConflatedState(1).saveStateSerial()
    val conflatedStateLce:ConflatedState<Lce<Int>> = ConflatedState(Lce.Loading())
    val some = getFlowCoordinatorInstance<FlowCoordinator>(appScope, flowTag)

    fun onClick() {
        conflatedState.value = ++conflatedState.value
    }



}

class FlowCoordinator @Inject constructor(errorHandler: ErrorHandler) : BaseCoordinator(errorHandler) {

    override fun dispose() {
        super.dispose()
        Log.i(flowTag, "Flow Coordinator disposed")
    }
}

class SecondCoordinator @Inject constructor(errorHandler: ErrorHandler, val secondArgument: SecondArgument) :
    BaseCoordinator(errorHandler) {
    val some = getFlowCoordinatorInstance<FlowCoordinator>(appScope, flowTag)

    init {
        Log.d("SOMETHING:", secondArgument.toString())
    }
}
