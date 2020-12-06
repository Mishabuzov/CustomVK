package ru.home.customvk.presentation.login_screen

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.freeletics.rxredux.reduxStore
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import ru.home.customvk.VkApplication
import ru.home.customvk.presentation.BaseRxViewModel
import ru.home.customvk.utils.PreferencesUtils
import javax.inject.Inject

class LoginViewModel(application: Application) : BaseRxViewModel(application) {

    @Inject
    lateinit var preferencesUtils: PreferencesUtils

    private val inputRelay: Relay<Action> = PublishRelay.create()
    private val input: Consumer<Action> get() = inputRelay
    private val state: Observable<State> = inputRelay.reduxStore(
        initialState = State(),
        reducer = { state, action -> state.reduce(action) }
    )
    private val currentState: MutableLiveData<State> = MutableLiveData()
    fun getStateLiveData() = currentState as LiveData<State>

    init {
        (application as VkApplication).appComponent.loginViewModelSubComponentBuilder().build().inject(this)

        state.observeOn(AndroidSchedulers.mainThread())
            .subscribe { currentState.value = it }
            .disposeOnFinish()
    }

    fun loginByToken() {
        val accessToken = preferencesUtils.getToken()
        if (accessToken.isNullOrEmpty()) {  // VKScope.OFFLINE grants endless token, just check if it was already got
            input.accept(Action.StartLogin)
        } else {
            preferencesUtils.accessToken = accessToken
            input.accept(Action.SuccessLogin)
        }
    }

    fun saveToken(accessToken: String) = preferencesUtils.saveToken(accessToken)

}
