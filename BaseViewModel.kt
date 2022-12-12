package com.battery.cygni.presentation.common.base

import android.view.View
import androidx.lifecycle.ViewModel
import com.battery.cygni.utils.event.SingleActionEvent
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

open class BaseViewModel : ViewModel() {
    val TAG: String = this.javaClass.simpleName
    var compositeDisposable=CompositeDisposable()

    val onClick: SingleActionEvent<View> = SingleActionEvent()

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

    fun Disposable.addToCompositeDisposable() {
        compositeDisposable.add(this)
    }
    open fun onClick(view: View?) {
        view?.let {
            onClick.value = it
        }

    }
}