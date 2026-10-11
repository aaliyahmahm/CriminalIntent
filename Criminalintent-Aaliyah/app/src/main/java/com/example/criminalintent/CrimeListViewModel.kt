package com.example.criminalintent

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData

class CrimeListViewModel(app: Application) : AndroidViewModel(app) {
    val crimes = (app as CrimeApplication).repository.crimes.asLiveData()
}
