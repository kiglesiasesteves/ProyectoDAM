package com.example.menstruacionnavapp.model

import java.util.Date

data class Menstruacion(
    var usuario: String,
    var lastPeriod: Date,
    var mediaCiclo: Int,
    var mediaSangrado: Int
)
