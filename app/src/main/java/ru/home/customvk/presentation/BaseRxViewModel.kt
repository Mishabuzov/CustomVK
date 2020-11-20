package ru.home.customvk.presentation

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseRxViewModel : ViewModel() {

    private val disposables = CompositeDisposable()

    protected fun Disposable.disposeOnFinish(): Disposable {
        disposables += this
        return this
    }

    private operator fun CompositeDisposable.plusAssign(disposable: Disposable) {
        add(disposable)
    }

    override fun onCleared() = disposables.clear()
}
