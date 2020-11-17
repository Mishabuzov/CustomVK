package ru.home.customvk.presentation

import androidx.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import ru.home.customvk.presentation.posts_screen.Action

abstract class BaseRxViewModel : ViewModel() {

    private val disposables = CompositeDisposable()

    protected fun Disposable.disposeOnFinish(): Disposable {
        disposables += this
        return this
    }

    private operator fun CompositeDisposable.plusAssign(disposable: Disposable) {
        add(disposable)
    }

    protected fun Observable<Action>.setupDefaultOnErrorLoadingPostsAction(): Observable<Action> {
        return onErrorReturn { error -> Action.ErrorLoadingPosts(error) }
    }

    override fun onCleared() = disposables.clear()
}
