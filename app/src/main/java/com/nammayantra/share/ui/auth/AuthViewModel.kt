package com.nammayantra.share.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammayantra.share.data.repository.AuthRepository
import com.nammayantra.share.util.Resource
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repo = AuthRepository

    private val _loginResult = MutableLiveData<Resource<String>>()
    val loginResult: LiveData<Resource<String>> = _loginResult

    private val _signupResult = MutableLiveData<Resource<String>>()
    val signupResult: LiveData<Resource<String>> = _signupResult

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginResult.value = Resource.Error("Please fill in all fields")
            return
        }
        _loginResult.value = Resource.Loading
        viewModelScope.launch {
            repo.login(email, password).fold(
                onSuccess = { _loginResult.postValue(Resource.Success(it)) },
                onFailure = { _loginResult.postValue(Resource.Error(it.message ?: "Login failed")) }
            )
        }
    }

    fun signup(name: String, email: String, password: String, role: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _signupResult.value = Resource.Error("Please fill in all fields")
            return
        }
        if (password.length < 6) {
            _signupResult.value = Resource.Error("Password must be at least 6 characters")
            return
        }
        _signupResult.value = Resource.Loading
        viewModelScope.launch {
            repo.signup(name, email, password, role).fold(
                onSuccess = { _signupResult.postValue(Resource.Success(it)) },
                onFailure = { _signupResult.postValue(Resource.Error(it.message ?: "Sign up failed")) }
            )
        }
    }

    fun isLoggedIn(): Boolean = repo.currentUserId() != null
    fun currentUid(): String? = repo.currentUserId()
}
