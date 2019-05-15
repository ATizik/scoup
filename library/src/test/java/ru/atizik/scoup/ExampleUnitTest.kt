package ru.atizik.scoup

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import android.os.Bundle
import androidx.fragment.app.Fragment
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
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

    }
}
