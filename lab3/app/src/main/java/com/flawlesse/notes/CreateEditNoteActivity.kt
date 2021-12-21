package com.flawlesse.notes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.flawlesse.notes.databinding.ActivityCreateEditNoteBinding

class CreateEditNoteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateEditNoteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateEditNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}