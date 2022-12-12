package com.battery.cygni.presentation.common.base

import android.view.View
import androidx.lifecycle.ViewModel
import com.battery.cygni.utils.event.SingleActionEvent
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * base view model class
 */
open class BaseViewModel : ViewModel() {
    val TAG: String = this.javaClass.simpleName
    var compositeDisposable = CompositeDisposable()

    /**
     * common view clicks
     */
    val onClick: SingleActionEvent<View> = SingleActionEvent()

    /**
     * view model cleared
     */
    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

    fun Disposable.addToCompositeDisposable() {
        compositeDisposable.add(this)
    }

    /**
     * on click views
     */
    open fun onClick(view: View?) {
        view?.let {
            onClick.value = it
        }

    }
}