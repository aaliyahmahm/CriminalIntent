package com.example.criminalintent

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class CrimeDetailActivity : AppCompatActivity(R.layout.activity_host) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setTitle(R.string.detail_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (savedInstanceState == null) supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, CrimeDetailFragment().apply {
                arguments = Bundle().apply {
                    putString("crimeId", intent.getStringExtra("crimeId"))
                    putBoolean("isNew", intent.getBooleanExtra("isNew", false))
                }
            }).commit()
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
    companion object {
        fun newIntent(context: Context, id: String, isNew: Boolean = false) =
            Intent(context, CrimeDetailActivity::class.java).putExtra("crimeId", id).putExtra("isNew", isNew)
    }
}
