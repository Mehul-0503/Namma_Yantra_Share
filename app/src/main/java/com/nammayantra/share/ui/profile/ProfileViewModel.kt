package com.nammayantra.share.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammayantra.share.data.model.User
import com.nammayantra.share.data.repository.AuthRepository
import com.nammayantra.share.util.Resource
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val repo = AuthRepository

    private val _user = MutableLiveData<Resource<User>>()
    val user: LiveData<Resource<User>> = _user

    fun loadProfile() {
        val uid = repo.currentUserId() ?: return
        _user.value = Resource.Loading
        viewModelScope.launch {
            repo.getUser(uid).fold(
                onSuccess = { _user.postValue(Resource.Success(it)) },
                onFailure = { _user.postValue(Resource.Error(it.message ?: "Failed to load profile")) }
            )
        }
    }

    fun logout() {
        repo.logout()
    }

    fun currentUid(): String? = repo.currentUserId()
}
