package dev.strohmnative.viewmodels

import androidx.lifecycle.MutableLiveData

class MutableLiveStrohmData<T>(
    val onActiveHandler: () -> Unit,
    val onInactiveHandler: () -> Unit
) : MutableLiveData<T>() {

    override fun onActive() {
        onActiveHandler()
    }

    override fun onInactive() {
        onInactiveHandler()
    }
}
