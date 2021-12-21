package com.flawlesse.notes.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class NotesViewModel(application: Application): AndroidViewModel(application) {
    var searchString = MutableLiveData<String>()
    enum class Ordering{ NONE, BY_TITLE, BY_DATE }
    var currentOrdering = Ordering.NONE

    init {

    }
}