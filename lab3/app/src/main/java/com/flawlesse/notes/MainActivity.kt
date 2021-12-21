package com.flawlesse.notes

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.flawlesse.notes.databinding.ActivityMainBinding
import com.flawlesse.notes.viewmodels.NotesViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var view: View
    private lateinit var viewModel: NotesViewModel

    companion object {
        val CREATE_NOTE_CODE = 89993
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        view = binding.root
        setContentView(view)

        // hack
        //val devIntent = Intent(this, CreateEditNoteActivity::class.java)
        //startActivity(devIntent)
        viewModel = ViewModelProvider(this)[NotesViewModel::class.java]

        binding.btnAddNote.setOnClickListener{
            val createNoteIntent = Intent(this, CreateEditNoteActivity::class.java)
            @Suppress("DEPRECATION")
            startActivityForResult(createNoteIntent, CREATE_NOTE_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CREATE_NOTE_CODE && resultCode == Activity.RESULT_OK && data != null){

            return
        }

    }
}