package ru.atizik.scoup

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LifecycleRegistry
import android.support.v4.app.Fragment
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import ru.atizik.scoup.fragments.CoordinatorOwner
import ru.atizik.scoup.viewmodel.BaseCoordinator

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    class crdOwner : CoordinatorOwner<BaseCoordinator> {
        override val coordinator: BaseCoordinator
            get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        override val compDisp: CompositeDisposable = CompositeDisposable()

        override fun init(fragment: Fragment) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override var fragmentDelegate: Fragment
            get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
            set(value) {}

    }

    @Test
    fun addition_isCorrect() = runBlocking(Dispatchers.Default) {
/*
        var some = "not_some"
        val lifecycle = LifecycleRegistry(mockk<LifecycleOwner>())
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        val confl = ConflatedState(Lce.Success(0))
        launch(Dispatchers.IO) {
            with(crdOwner()) {
                confl.observe(lifecycle).onContent { some = "someaa" }.onContent { some = "somegg" }.onContent { some = "somesd" }.onContent { some = "some" }.subscribe()
            }
        }

        delay(1500)
        assert(some == "some")*/
    }
}
