package com.example.menstruacionnavapp.ui.fitness

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FitnessViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Visita la página premium para más información"
    }
    val text: LiveData<String> = _text
}