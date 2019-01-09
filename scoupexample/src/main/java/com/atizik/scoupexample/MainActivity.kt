package com.atizik.scoupexample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.first_fragment.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.channels.consumeEach
import ru.atizik.scoup.ConflatedState
import ru.atizik.scoup.fragments.BaseFragment
import ru.atizik.scoup.subscribe
import ru.atizik.scoup.viewmodel.BaseCoordinator
import ru.atizik.scoup.viewmodel.ErrorHandler
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if(savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentHost, FirstFragment())
                .commitAllowingStateLoss()
        }
    }
}

class FirstFragment: BaseFragment<FirstCoordinator>(FirstCoordinator::class.java) {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = inflater.inflate(R.layout.first_fragment,null)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(coordinatorOwner) {
            coordinatorOwner
                .coordinator
                .conflatedState
                .observe()
                .onEach { textView.text = it.toString()}
                .subscribe()
        }
    }
}



class SecondFragment: BaseFragment<SecondCoordinator>(SecondCoordinator::class.java) {

}

class FirstCoordinator @Inject constructor(errorHandler: ErrorHandler):BaseCoordinator(errorHandler) {
    val conflatedState = ConflatedState(1)
    init {
        launch {
            while (isActive) {

                delay(10)
                conflatedState.value = conflatedState.value + 1
            }
        }
    }

}

class SecondCoordinator(errorHandler: ErrorHandler):BaseCoordinator(errorHandler) {}
