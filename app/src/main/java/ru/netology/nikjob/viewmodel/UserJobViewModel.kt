package ru.netology.nikjob.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nikjob.api.JobApiService
import ru.netology.nikjob.dto.Job

class UserJobViewModel(private val jobApiService: JobApiService) : ViewModel() {
    var jobs = MutableLiveData<List<Job>>()


     fun loadJobData(userId:Long){
        viewModelScope.launch {
            val response = jobApiService.getJobById(userId)
            if (!response.isSuccessful) throw RuntimeException("API SERVICE ERROR")
            val body = response.body() ?: throw RuntimeException("Body is null")
            jobs.value = body

        }
    }
}