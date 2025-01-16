package com.example.menstruacionnavapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "Visita la página Premium para más información!"
    }
    val text: LiveData<String> = _text

}