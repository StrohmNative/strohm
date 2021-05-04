package dev.strohmnative.strohm.viewmodels

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dev.strohmnative.strohm.PropName
import dev.strohmnative.strohm.PropPath
import dev.strohmnative.strohm.Props
import dev.strohmnative.strohm.Strohm
import kotlinx.collections.immutable.persistentMapOf
import java.util.*

abstract class ViewModelBase<DataType>(
    initialData: DataType,
    val propName: PropName,
    val propPath: PropPath
) {
    private var subscriptionId: UUID? = null

    var data: MutableLiveData<DataType> =  MutableLiveStrohmData(::onActive, ::onInactive)
        private set

    private val handler = Handler(Looper.getMainLooper())

    init {
        data.value = initialData
    }

    private fun receiveProps(props: Props): Unit {
        propsToData(props)?.let { data ->
            handler.post(Runnable {
                this.data.value  = data
            })
        }
    }

    private fun onActive() {
        Strohm.getInstance().subscribe(persistentMapOf(propName to propPath), ::receiveProps) {
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

