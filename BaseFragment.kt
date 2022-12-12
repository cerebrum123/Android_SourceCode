package com.battery.cygni.presentation.common.base

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.battery.cygni.BR
import com.battery.cygni.data.local.SharedPrefManager
import com.battery.cygni.presentation.common.hideKeyboard
import com.battery.cygni.presentation.common.utilities.rxbus.EventBus
import io.reactivex.android.schedulers.AndroidSchedulers

abstract class BaseFragment<Binding : ViewDataBinding> : Fragment() {
    val TAG: String = this.javaClass.simpleName
    lateinit var sharedPrefManager: SharedPrefManager
    lateinit var baseContext: Context
    lateinit var binding: Binding
    lateinit var progressDialog: ProgressDialog
    val parentActivity: BaseActivity<*>?
        get() = activity as? BaseActivity<*>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        baseContext = context
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        progressDialog= ProgressDialog(requireContext())
        progressDialog.setMessage("Please Wait....")
        progressDialog.setCancelable(false)
        parentActivity?.let {
            sharedPrefManager = it.sharepref
        }

        onCreateView(v, savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout: Int = getLayoutResource()
        binding = DataBindingUtil.inflate(layoutInflater, layout, container, false)
        binding.setVariable(BR.vm, getViewModel())
        registerEventBus(getViewModel())
        return binding.root
    }

    protected abstract fun getLayoutResource(): Int
    protected abstract fun getViewModel(): BaseViewModel
    protected abstract fun onCreateView(view: View, saveInstanceState: Bundle?)
    open fun onPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
    }


    override fun onPause() {
        super.onPause()
        activity?.hideKeyboard()
    }


    override fun onDetach() {
        super.onDetach()
    }

    private fun registerEventBus(viewModel: BaseViewModel) {
        viewModel.compositeDisposable.add(
            EventBus.subscribe<Any>()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it is PermissionsResult) {
                        it.permissions?.let { it1 ->
                            it.grantResults?.let { it2 ->
                                onPermissionsResult(it.requestCode,
                                    it1, it2
                                )
                            }
                        }
                    }
                }, {
                    Log.e(TAG, it.message.toString())
                })
        )
    }

    class PermissionsResult {
        var requestCode: Int = 0
        var permissions: Array<out String>?=null
        var grantResults: IntArray?=null
    }

}