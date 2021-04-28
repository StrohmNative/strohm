package dev.strohmnative.strohm.viewmodels

import android.os.Handler
import android.os.Looper
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import dev.strohmnative.strohm.PropName
import dev.strohmnative.strohm.PropPath
import dev.strohmnative.strohm.Props
import dev.strohmnative.strohm.Strohm
import java.util.*
import androidx.databinding.library.baseAdapters.BR

abstract class ViewModelBase<DataType>(
    initialData: DataType,
    propName: PropName,
    propPath: PropPath
) : BaseObservable() { // TODO: use LiveData
    private var subscriptionId: UUID? = null
    val propName: PropName
    val propPath: PropPath
    @Bindable
    var data: DataType = initialData

    private val handler = Handler(Looper.getMainLooper())

    init {
        this.propName = "entries"
        this.propPath = listOf("entries")
        Strohm.getInstance().subscribe(mapOf(propName to propPath), ::receiveProps) {
                subscriptionId -> this.subscriptionId = subscriptionId
        }
    }

    fun receiveProps(props: Props): Unit {
        propsToData(props)?.let { data ->
            handler.post(Runnable {
                this.data = data
                notifyPropertyChanged(BR.data)
            })
        }
    }

    abstract fun propsToData(props: Props): DataType?
}