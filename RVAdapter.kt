package com.battery.cygni.presentation.common.base.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.battery.cygni.BR

class RVAdapter<M, B : ViewDataBinding>(
    @field:LayoutRes @param:LayoutRes private val layoutResId: Int,
    private val modelVariableId: Int,
    private val callback: Callback<M>
) : RecyclerView.Adapter<RVAdapter.Holder<B>>() {
    private val dataList: MutableList<M> = ArrayList()
    fun removeItem(i: Int) {
        try {
            if (i != -1) {
                dataList.removeAt(i)
                notifyItemRemoved(i)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getDataList(): MutableList<M> {
        return dataList;
    }

    operator fun set(i: Int, scanResult: M?) {
        if (scanResult == null) return
        dataList.add(i, scanResult)
        notifyItemChanged(i)
    }

    interface Callback<M> {
        fun onItemClick(v: View, m: M)
        fun onItemClick(v: View, m: M, position: Int) {

        }
    }


    override fun getItemCount(): Int {
        return dataList.size
    }


    fun addToList(newDataList: List<M>?) {
        var newDataList = newDataList
        if (newDataList == null) {
            newDataList = emptyList()
        }
        val positionStart = dataList.size
        val itemCount = newDataList.size
        dataList.addAll(newDataList)
        notifyItemRangeInserted(positionStart, itemCount)
    }

    fun setList(newDataList: List<M>?) {
        dataList.clear()
        if (newDataList != null)
            dataList.addAll(newDataList)
        notifyDataSetChanged()
    }

    fun clearList() {
        dataList.clear()
        notifyDataSetChanged()
    }

    fun addData(data: M) {
        val positionStart = dataList.size
        dataList.add(data)
        notifyItemInserted(positionStart)
    }

    fun addData(pos: Int, data: M) {
        dataList.add(0, data)
        notifyItemInserted(0)
    }

    /**
     * Simple view holder for this adapter
     *
     * @param <S>
    </S> */
    class Holder<S : ViewDataBinding>(var binding: S) : RecyclerView.ViewHolder(
        binding.root
    ) {

    }

    override fun onBindViewHolder(holder: Holder<B>, position: Int) {
        onBind(holder.binding, dataList[position], position)
        holder.binding.executePendingBindings()
    }

    fun onBind(binding: B, bean: M, position: Int) {
        binding.setVariable(modelVariableId, bean)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder<B> {
        val binding: B =
            DataBindingUtil.inflate(LayoutInflater.from(parent.context), layoutResId, parent, false)
        val holder = Holder(binding)
        binding.setVariable(BR.callback, callback)
        // binding.setVariable(BR.holder, holder)
        return holder
    }
}