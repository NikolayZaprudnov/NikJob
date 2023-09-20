package ru.netology.nikjob.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nikjob.api.JobApiService
import ru.netology.nikjob.auth.AppAuth
import ru.netology.nikjob.dto.Job

class UserJobViewModel(
    private val jobApiService: JobApiService,
    private val appAuth: AppAuth,
) : ViewModel() {
    var jobs = MutableLiveData<List<Job>>()


    fun loadJobData(userId: Long) {
        viewModelScope.launch {
            val response = jobApiService.getJobById(userId)
            if (!response.isSuccessful) throw RuntimeException("API SERVICE ERROR")
            val body = response.body() ?: throw RuntimeException("Body is null")
            jobs.value = body
            if (appAuth.authStateFlow.value.id == userId) {
                jobs.value!!.forEach {
                    it.ownedByMe = true
                }

            }
        }
    }
    fun createJob(job: Job){
        viewModelScope.launch {
            val response = jobApiService.createJob(job)
            if (!response.isSuccessful)throw RuntimeException(("API SERVICE ERROR"))
        }
    }
    fun deleteJob(id:Long){
        viewModelScope.launch {
            val response = jobApiService.removeJobById(id)
            if (!response.isSuccessful)throw RuntimeException(("API SERVICE ERROR"))
        }
    }
}
