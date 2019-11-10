package com.atizik.scoupexample

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.first_fragment.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.atizik.scoup.ConflatedState
import ru.atizik.scoup.Lce
import ru.atizik.scoup.di.getFlowCoordinatorInstance
import ru.atizik.scoup.fragments.*
import ru.atizik.scoup.onSuccess
import ru.atizik.scoup.viewmodel.BaseCoordinator
import ru.atizik.scoup.viewmodel.ErrorHandler
import ru.atizik.scoup.viewmodel.MvState
import ru.atizik.scoup.viewmodel.StateCoordinator
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

data class MainState(val resource: Lce<String> = Lce.Success("data not loaded")): MvState

class MainCoordinator @Inject constructor(errorHandler: ErrorHandler) : BaseCoordinator(errorHandler)


data class FirstState(val resource: Lce<String> = Lce.Success("data not loaded")): MvState

class FirstFragment : BaseFragmentState<FirstState, FirstCoordinator>(
    FirstCoordinator::class.java
) {

    init {
        toolbarDelegate?.toolbarBuilder = {
            background = ColorDrawable(fragmentDelegate.resources.getColor(R.color.colorPrimaryDark))
            title = "Toolbar"
            setNavigationIcon(android.R.drawable.ic_menu_add)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.first_fragment, null)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        coordinator
            .conflatedState
            .observe()
            .onEach { textView.text = it.toString() }
            .subscribe()

        subscribe{
            it.resource.onSuccess {
                Toast.makeText(context!!, "Data loaded", Toast.LENGTH_LONG).show()
            }
        }


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
    ArgumentReceiver<SecondArgument> by argRec() {


    override fun onStart() {
        super.onStart()
        print(coordinatorOwner.coordinator.secondArgument)
    }
}

class FirstCoordinator @Inject constructor() : StateCoordinator<FirstState>(FirstState()) {
    val conflatedState:ConflatedState<Int> = ConflatedState(1).saveStateSerial()
    val conflatedStateLce:ConflatedState<Lce<Int>> = ConflatedState(Lce.Loading())
    val some = getFlowCoordinatorInstance<FlowCoordinator>(appScope, flowTag)

    fun onClick() {
        conflatedState.value = ++conflatedState.value
    }

    init {
        launch {
            delay(5000)
            setState { copy(resource = Lce.Success("Data loaded!")) }
        }
    }
}

class FlowCoordinator @Inject constructor(errorHandler: ErrorHandler) : BaseCoordinator(errorHandler) {

    override fun dispose() {
        super.dispose()
        Log.i(flowTag, "Flow Coordinator disposed")
    }
}

data class FlowState(val todo: Unit = Unit): MvState

class SecondCoordinator @Inject constructor(errorHandler: ErrorHandler, val secondArgument: SecondArgument) :
    BaseCoordinator(errorHandler) {
    val some = getFlowCoordinatorInstance<FlowCoordinator>(appScope, flowTag)

    init {
        Log.d("SOMETHING:", secondArgument.toString())
    }
}