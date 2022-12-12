package com.battery.cygni.presentation.common.base

import android.app.ProgressDialog
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.battery.cygni.BR
import com.battery.cygni.R
import com.battery.cygni.data.local.SharedPrefManager
import com.battery.cygni.presentation.MyApplication
import com.battery.cygni.presentation.common.hideKeyboard
import com.battery.cygni.presentation.common.utilities.AppConstants
import com.battery.cygni.presentation.common.utilities.rxbus.EventBus
import javax.inject.Inject

abstract class BaseActivity<Binding : ViewDataBinding> : AppCompatActivity() {
    @Inject
    lateinit var sharepref: SharedPrefManager
    val TAG: String = this.javaClass.simpleName
    open val onRetry: (() -> Unit)? = null
    lateinit var binding: Binding
    val app: MyApplication
        get() = application as MyApplication

    lateinit var progressDialog: ProgressDialog
    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateTheme()
        val layout: Int = getLayoutResource()
        binding = DataBindingUtil.setContentView(this, layout)
        val vm = getViewModel()
        binding.setVariable(BR.vm, vm)
        progressDialog= ProgressDialog(this)
        progressDialog.setMessage("Please Wait....")
        onCreateView()
    }

    private fun updateTheme() {
        setTheme(R.style.AppTheme_Orange)
     /*   sharepref.getTheme()?.let {
            when (it) {
                AppConstants.Theme_Orange ->
                    setTheme(R.style.AppTheme_Orange)
                AppConstants.Theme_Blue ->
                    setTheme(R.style.AppTheme_Blue)
            }
        }*/
    }

    protected abstract fun getLayoutResource(): Int
    protected abstract fun getViewModel(): BaseViewModel
    protected abstract fun onCreateView()

    fun showToast(msg: String? = "Something went wrong !!") {
        Toast.makeText(this, msg ?: "Showed null value !!", Toast.LENGTH_SHORT).show()
    }

    override fun onStop() {
        super.onStop()
        hideKeyboard()
    }

    /*  fun showLoading(s: String?) {
          progressSheet?.dismissAllowingStateLoss()
          progressSheet = ProgressSheet(object : ProgressSheet.BaseCallback {
              override fun onClick(view: View?) {}
              override fun onBind(bind: ViewProgressSheetBinding) {
                  progressSheet?.showMessage(s);
              }
          })
          progressSheet?.show(supportFragmentManager, progressSheet?.tag)

      }


      fun hideLoading() {
          progressSheet?.dismissAllowingStateLoss()
          progressSheet = null
          getLoaderView()?.setVariable(BR.show, false)
      }*/


    /*private fun registerEventBus(viewModel: BaseViewModel) {
        viewModel.compositeDisposable.add(
            EventBus.subscribe<UnAuthUser>()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    showUnauthSheet()
                }, {
                    Log.e(TAG, it.message.toString())
                })
        )
    }*/

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EventBus.post(BaseFragment.PermissionsResult().apply {
            this.requestCode = requestCode
            this.permissions = permissions
            this.grantResults = grantResults
        })
    }

}