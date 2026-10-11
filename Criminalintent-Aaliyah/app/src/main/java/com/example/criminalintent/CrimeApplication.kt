package com.example.criminalintent

import android.app.Application

class CrimeApplication : Application() {
    val repository by lazy { CrimeRepository(CrimeDatabase.get(this).crimeDao(), this) }
}
