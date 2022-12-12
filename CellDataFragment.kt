package com.battery.cygni.presentation.views.cell

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.battery.cygni.BR
import com.battery.cygni.R
import com.battery.cygni.databinding.FragmentCellDataBinding
import com.battery.cygni.databinding.HolderCellBinding
import com.battery.cygni.databinding.HolderTempBinding
import com.battery.cygni.presentation.common.base.BaseFragment
import com.battery.cygni.presentation.common.base.BaseViewModel
import com.battery.cygni.presentation.common.base.adapter.RVAdapter
import com.battery.cygni.presentation.common.showDefaultSnack
import com.battery.cygni.presentation.common.showErrorSnack
import com.battery.cygni.presentation.views.main.MainActivity
import com.battery.cygni.utils.SampleGattAttributes
import com.battery.cygni.utils.UtilsMethods.getCellItem
import com.battery.cygni.utils.UtilsMethods.getTemperatureItem
import com.battery.cygni.utils.event.helper.Status
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.clj.fastble.utils.HexUtil
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class CellDataFragment : BaseFragment<FragmentCellDataBinding>() {

    private val viewmodel: CellDataModel by viewModels()
    private var bleDevice: BleDevice? = null
    private lateinit var adapterCell1: RVAdapter<Item, HolderCellBinding>
    private lateinit var adapterCell2: RVAdapter<Item, HolderCellBinding>
    private lateinit var adapterTemp: RVAdapter<Item, HolderTempBinding>
    private var lastData: List<String>? = null
    private var insertAdded = false
    override fun getLayoutResource(): Int {
        return R.layout.fragment_cell_data
    }

    override fun getViewModel(): BaseViewModel {
        return viewmodel
    }

    override fun onCreateView(v: View, saveInstanceState: Bundle?) {
        val act = activity as MainActivity
        initInput()
        initCellAdapter()
        initTempAdapter()
        viewmodel.onClick.observe(viewLifecycleOwner, Observer<View> { view ->
            when (view.id) {
                R.id.v_cell -> {
                    binding.cell = !binding.cell
                }
                R.id.v_drop2 -> {
                    binding.temp = !binding.temp
                }
            }
        })
        viewmodel.obrInsertRecord.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.LOADING -> {
                }
                Status.ERROR -> {
                    activity?.showErrorSnack(it.message)
                }
                Status.SUCCESS -> {
                    //   showSuccessSnack(it.message)
                }
                Status.WARN -> {}
            }
        })

        act.obrCellData.observe(viewLifecycleOwner, Observer { i ->
            try {
                i?.let { it ->
                    if (it.size > 45) {
                        lastData = it
                        Log.e(TAG, "CeLL Data=>" + Gson().toJson(it))
                        processData()
                        if (insertAdded) {
                            act.currentDevice.value?.let {
                                viewmodel.insertRecord(lastData, it.mac)
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                activity?.showErrorSnack(e.message.toString())
            }
        })
        viewmodel.obrCommandBits.observe(viewLifecycleOwner, Observer {
            insertAdded = true
            sendCommand(it)
        })
        viewmodel.obrProgress.observe(viewLifecycleOwner, Observer {
            if (it) {
                if (lastData == null)
                    progressDialog.show()
            } else {
                progressDialog.dismiss()
            }
        })

        act.currentDevice.observe(viewLifecycleOwner, Observer {
            it?.let {
                viewmodel.cellData()
            }
        })

        act.obrCellDataUpdated.observe(viewLifecycleOwner, Observer {
            it?.let { time ->
                val c = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                c.timeInMillis = time
                binding.tvUpdated.text =
                    "Updated at " + SimpleDateFormat("HH:mm:ss", Locale.US).format(c.time)
            }
        })
    }


    private fun initCellAdapter() {
        adapterCell1 = RVAdapter(R.layout.holder_cell, BR.bean, object : RVAdapter.Callback<Item> {
            override fun onItemClick(v: View, m: Item) {

            }
        })
        adapterCell2 = RVAdapter(R.layout.holder_cell, BR.bean, object : RVAdapter.Callback<Item> {
            override fun onItemClick(v: View, m: Item) {

            }
        })
        binding.rvCell1.layoutManager = LinearLayoutManager(context)
        binding.rvCell1.adapter = adapterCell1
        binding.rvCell2.layoutManager = LinearLayoutManager(context)
        binding.rvCell2.adapter = adapterCell2
    }

    private fun initTempAdapter() {
        adapterTemp = RVAdapter(R.layout.holder_temp, BR.bean, object : RVAdapter.Callback<Item> {
            override fun onItemClick(v: View, m: Item) {

            }
        })
        binding.rvTemp.layoutManager = LinearLayoutManager(context)
        binding.rvTemp.adapter = adapterTemp
    }


    private fun initInput() {
        arguments?.let {
            bleDevice = it.getParcelable("device")
        }
    }


    private fun sendCommand(command: String) {
        BleManager.getInstance().write(
            bleDevice,
            SampleGattAttributes.BLE_SERVICE,
            SampleGattAttributes.BLE_RX_CHARACTERISTIC,
            HexUtil.hexStringToBytes(command),
            object : BleWriteCallback() {
                override fun onWriteSuccess(
                    current: Int,
                    total: Int,
                    justWrite: ByteArray
                ) {
                    activity?.runOnUiThread {
                        activity?.showDefaultSnack("Send CMD=> $command", true)
                    }
                }

                override fun onWriteFailure(exception: BleException) {
                    activity?.runOnUiThread {
                        activity?.showErrorSnack("onWriteFailure ${exception.description}")
                    }
                }
            }
        )
    }

    private fun processData() {
        lastData?.let {
            val tempList = arrayListOf<Item>()
            var index = 1
            for (i in 4..35 step 2) {
                tempList.add(getCellItem(index, it, i, i + 1))
                index++
            }
            adapterCell1.setList(tempList.subList(0, 8))
            adapterCell2.setList(tempList.subList(8, 16))
            index = 1
            adapterTemp.clearList()
            for (i in 36..47 step 2) {
                if (index < 5)
                    adapterTemp.addData(getTemperatureItem("Temperature $index", it, i, i + 1))
                else if (index == 5)
                    adapterTemp.addData(getTemperatureItem("Ambient Temperature", it, i, i + 1))
                else if (index == 6)
                    adapterTemp.addData(getTemperatureItem("MOSFET Temperature", it, i, i + 1))

                index++
            }

        }

    }


    override fun onDestroy() {
        insertAdded = false
        super.onDestroy()
    }

    data class Item(val id: Int, val heading: String, val value: String, var type: Int)
}