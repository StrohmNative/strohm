package dev.strohmnative.strohm.viewmodels

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dev.strohmnative.strohm.PropName
import dev.strohmnative.strohm.PropPath
import dev.strohmnative.strohm.Props
import dev.strohmnative.strohm.Strohm
import java.util.*

abstract class ViewModelBase<DataType>(
    initialData: DataType,
    val propName: PropName,
    val propPath: PropPath
) {
    private var subscriptionId: UUID? = null

    var data: LiveData<DataType> =  MutableLiveStrohmData(::onActive, ::onInactive)
        private set

    private val handler = Handler(Looper.getMainLooper())

    init {
        (data as MutableLiveData).value = initialData
    }

    private fun receiveProps(props: Props): Unit {
        propsToData(props)?.let { data ->
            handler.post(Runnable {
                (this.data as MutableLiveData).value  = data
            })
        }
    }

    private fun onActive() {
        Strohm.getInstance().subscribe(mapOf(propName to propPath), ::receiveProps) {
                subscriptionId -> this.subscriptionId = subscriptionId
        }
    }

    private fun onInactive() {
        this.subscriptionId = this.subscriptionId?.let {
            Strohm.getInstance().unsubscribe(it)
            null
        }
    }

    abstract fun propsToData(props: Props): DataType?
}

