package com.battery.cygni.presentation.views.cell

import com.battery.cygni.data.dao.CellDataDao
import com.battery.cygni.data.entity.CellDataBean
import com.battery.cygni.data.entity.LiveDataBean
import com.battery.cygni.domain.user.repositary.BaseRepo
import com.battery.cygni.presentation.common.base.BaseViewModel
import com.battery.cygni.utils.Constants
import com.battery.cygni.utils.Constants.Companion.cmdCellData
import com.battery.cygni.utils.event.SingleLiveEvent
import com.battery.cygni.utils.event.SingleRequestEvent
import com.battery.cygni.utils.event.helper.Resource
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Observable
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class CellDataModel @Inject constructor(val baseRepo: BaseRepo,val cellDataDao: CellDataDao) :
    BaseViewModel() {
    val obrCommandBits: SingleLiveEvent<String> = SingleLiveEvent()
    val obrProgress: SingleLiveEvent<Boolean> = SingleLiveEvent()
    fun cellData() {
        var index = 0
        compositeDisposable.add(Observable.interval(300, 400, TimeUnit.MILLISECONDS)
            .flatMap {
                return@flatMap Observable.create<String> { shooter ->
                    if (index < cmdCellData.size) {
                        if (index == 0) {
                            obrProgress.postValue(true)
                        }
                        shooter.onNext(cmdCellData[index])
                        index++
                    } else {
                        obrProgress.postValue(false)
                        if (index == cmdCellData.size + 20) {
                            index = 0
                        } else
                            index++

                    }
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                obrCommandBits.postValue(it)
            })
    }
    val obrInsertRecord = SingleRequestEvent<Long>()
    fun insertRecord(list: List<String>?, mac: String) {
        list?.let { record ->
            val time = Calendar.getInstance().timeInMillis
            cellDataDao.insert(CellDataBean(time, mac, Gson().toJson(record)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(object : SingleObserver<Long> {
                    override fun onSubscribe(d: Disposable) {
                        compositeDisposable.add(d)
                        obrInsertRecord.value = Resource.loading()
                    }

                    override fun onSuccess(t: Long) {
                        obrInsertRecord.value = Resource.success(t, "Data Inserted")
                    }

                    override fun onError(e: Throwable) {
                        obrInsertRecord.value = Resource.error(null, e.message.toString())
                    }
                })
        }
    }

}
