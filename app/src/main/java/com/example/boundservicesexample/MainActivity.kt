package com.example.boundservicesexample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.boundservicesexample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var service: MyService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        binding.toggleUpdates.setOnClickListener {
            toggleUpdates()
        }

        viewModel.getBinder().observe(this, Observer {
            if(it != null) {
                Log.d(TAG, "connected to service")
                service = it.getService()
            }
            else Log.d(TAG, "unbound from service")
        })

        viewModel.getIsProgressUpdating().observe(this, Observer {
            val handler = Handler()
            val runnable = object: Runnable {
                override fun run() {
                    if(it){
                        if(viewModel.getBinder().value != null){
                            if(service.getProgress() == service.getMaxValue()) {
                                viewModel.setIsUpdating(false)
                            }
                            binding.progressBar.progress = service.getProgress()
                            binding.progressBar.max = service.getMaxValue()
                            val progress = "${100 * service.getProgress() / service.getMaxValue()}%"
                            binding.textView.text = progress
                            handler.postDelayed(this, 100)
                        }
                    }
                    else {
                        handler.removeCallbacks(this)
                    }
                }
            }
            if(it) {
                binding.toggleUpdates.setText(R.string.pause)
                handler.postDelayed(runnable, 100)
            }
            else{
                if(service.getProgress() == service.getMaxValue()) {
                    binding.toggleUpdates.setText(R.string.restart)
                }
                else {
                    binding.toggleUpdates.setText(R.string.start)
                }
            }
        })
    }

    private fun toggleUpdates() {
        if(this::service.isInitialized ) {
            if(service.getProgress() == service.getMaxValue()) {
                service.resetTask()
                binding.toggleUpdates.setText(R.string.start)
            }
            else {
                if(service.getIsPaused()) {
                    service.unPausePretendLongRunningTask()
                    viewModel.setIsUpdating(true)
                }
                else {
                    service.pausePretendLongRunningTask()
                    viewModel.setIsUpdating(false)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if(viewModel.getBinder().value != null) {
            unbindService(viewModel.getServiceConnection())
        }
    }

    override fun onResume() {
        super.onResume()
        startService()
    }

    private fun startService() {
       val serviceIntent = Intent(this, MyService::class.java)
        startService(serviceIntent)
        bindService()
    }

    private fun bindService() {
        val serviceIntent = Intent(this, MyService::class.java)
        bindService(serviceIntent, viewModel.getServiceConnection(), BIND_AUTO_CREATE)
    }
}