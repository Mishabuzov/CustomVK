package ru.home.customvk.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseRxViewModel(application: Application) : AndroidViewModel(application) {

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
