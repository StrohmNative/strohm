package dev.strohmnative.viewmodels

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import dev.strohmnative.PropName
import dev.strohmnative.PropPath
import dev.strohmnative.PropSpec
import dev.strohmnative.Prop
import dev.strohmnative.StrohmNative
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

    private fun receiveProp(prop: Prop): Unit {
        propToData(prop)?.let { data ->
            handler.post(Runnable {
                this.data.value  = data
            })
        }
    }

    private fun onActive() {
        StrohmNative.getInstance().subscribe(PropSpec(propName, propPath), ::receiveProp) {
                subscriptionId -> this.subscriptionId = subscriptionId
        }
    }

    private fun onInactive() {
        this.subscriptionId = this.subscriptionId?.let {
            StrohmNative.getInstance().unsubscribe(it)
            null
        }
    }

    abstract fun propToData(prop: Prop): DataType?
}

