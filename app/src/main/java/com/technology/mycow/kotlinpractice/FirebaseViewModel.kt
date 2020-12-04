package com.technology.mycow.kotlinpractice

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel


class FirebaseViewModel(application: Application, firebaseRepository: FirebaseRepository)
    : AndroidViewModel(application) {

    private val mFirebaseRepository: FirebaseRepository

    init {
        mFirebaseRepository = firebaseRepository
    }

    val user : LiveData<User>?
        get() = mFirebaseRepository.user

    val isEmailChanged : LiveData<Boolean>?
        get() = mFirebaseRepository.isEmailChanged

    val isSignedUp : LiveData<Boolean>?
        get() = mFirebaseRepository.isSignedUp

    val isSignedIn : LiveData<Boolean>?
        get() = mFirebaseRepository.isSignedIn

    fun authSignUpUser(email: String, password: String, displayName: String) = viewModelScope.launch(Dispatchers.IO){
        mFirebaseRepository.authSignUpUser(email, password, displayName)
    }

    fun authSignInUser(email: String, password: String) = viewModelScope.launch(Dispatchers.IO) {
        mFirebaseRepository.authSignInUser(email, password)
    }

    fun authSignOut() = viewModelScope.launch(Dispatchers.IO){
        mFirebaseRepository.authSignOutUser()
    }

    fun authChangeEmailAddress(email: String) = viewModelScope.launch(Dispatchers.IO) {
        mFirebaseRepository.authChangeEmailAddress(email)
    }

    fun authSetNewPassword(newPassword: String) = viewModelScope.launch(Dispatchers.IO) {
        mFirebaseRepository.authSetNewPassword(newPassword)
    }

    fun authSendPasswordReset(email: String) = viewModelScope.launch(Dispatchers.IO) {
        mFirebaseRepository.authSendPasswordReset(email)
    }

    fun authDeleteUser() = viewModelScope.launch(Dispatchers.IO) {
        mFirebaseRepository.authDeleteUser()
    }

//    fun writeNewUser(childName: String, user: User) = viewModelScope.launch(Dispatchers.IO) {
//        mFirebaseRepository.writeNewUser(childName, user)
//    }

    companion object {
        private const val LOG_MSG = "FirebaseViewModel: "
    }

    class Factory(private val mApplication: Application): ViewModelProvider.NewInstanceFactory() {
        private val mFirebaseRepository: FirebaseRepository

        init {
            mFirebaseRepository = FirebaseRepository.getInstance()!!
        }

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return FirebaseViewModel(mApplication, mFirebaseRepository) as T
        }

    }

}