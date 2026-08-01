package com.adam.ecolens.ui.learn

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adam.ecolens.data.model.EncyclopediaItem
import com.adam.ecolens.data.repository.EducationRepository

class LearnViewModel(private val educationRepository: EducationRepository) : ViewModel() {

    private val _encyclopediaItems = MutableLiveData<List<EncyclopediaItem>>()
    val encyclopediaItems: LiveData<List<EncyclopediaItem>> = _encyclopediaItems

    init {
        _encyclopediaItems.value = educationRepository.getEncyclopediaItems()
    }
}
