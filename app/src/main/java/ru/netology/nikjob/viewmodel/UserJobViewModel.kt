package ru.netology.nikjob.viewmodel

import android.content.Context
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nikjob.api.JobApiService
import ru.netology.nikjob.auth.AppAuth
import ru.netology.nikjob.dto.Job
import util.DataTime
import javax.inject.Inject

@HiltViewModel
class UserJobViewModel @Inject constructor(
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
