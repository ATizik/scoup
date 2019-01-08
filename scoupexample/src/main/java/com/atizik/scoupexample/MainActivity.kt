package com.atizik.scoupexample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ru.atizik.scoup.fragments.BaseFragment
import ru.atizik.scoup.viewmodel.BaseCoordinator

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportFragmentManager.beginTransaction().add(R.id.fragmentHost,FirstFragment()).commitAllowingStateLoss()
    }
}

class FirstFragment: BaseFragment<FirstCoordinator>(FirstCoordinator::class.java) {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.first_fragment,container)
    }
}

class SecondFragment: BaseFragment<SecondCoordinator>(SecondCoordinator::class.java) {

}

class FirstCoordinator(errorHandler: (Throwable) -> Unit = { _->}):BaseCoordinator(errorHandler) {}

class SecondCoordinator(errorHandler: (Throwable) -> Unit = { _->}):BaseCoordinator(errorHandler) {}
