package com.example.boundservicesexample

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {

    private val TAG = "MainViewModel"

    private val _isProgressUpdating = MutableLiveData<Boolean>()
    private val _binder = MutableLiveData<MyService.MyBinder>()

    private val serviceConnection = object: ServiceConnection {

        override fun onServiceConnected(componentName: ComponentName?, iBinder: IBinder?) {
            Log.d(TAG, "onServiceConnected: connected to service")
            val binder = iBinder as MyService.MyBinder
            _binder.postValue(binder)
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            _binder.postValue(null)
        }
    }

    fun getIsProgressUpdating(): LiveData<Boolean> = _isProgressUpdating

    fun getBinder(): LiveData<MyService.MyBinder> = _binder

    fun getServiceConnection(): ServiceConnection = serviceConnection

    fun setIsUpdating (isUpdating: Boolean) {
        _isProgressUpdating.postValue(isUpdating)
    }
}