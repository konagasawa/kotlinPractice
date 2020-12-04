package com.technology.mycow.kotlinpractice

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private var mFirebaseViewModel: FirebaseViewModel? = null
    private lateinit var user : User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(mFirebaseViewModel == null){
            val factory = FirebaseViewModel.Factory(this.application)
            mFirebaseViewModel = ViewModelProvider(this, factory).get(FirebaseViewModel::class.java)
        }

        var email = emailEt.text
        var password = passwordEt.text
        var displayName = displayNameEt.text

        signUpBtn.setOnClickListener {
            if(!email.isNullOrEmpty() && !password.isNullOrEmpty() && !displayName.isNullOrEmpty()){
                mFirebaseViewModel!!.authSignUpUser(email.toString(), password.toString(), displayName.toString())
                clearEntry()
            }
        }

        signInBtn.setOnClickListener {
            if(!email.isNullOrEmpty() && !password.isNullOrEmpty()){
                mFirebaseViewModel!!.authSignInUser(email.toString(), password.toString())
                clearEntry()
            }
        }

        //FOR DEBUG
//        checkBtn.setOnClickListener {
//            val a = mFirebaseViewModel!!.user.toString()
//            Log.d(LOG_MSG, a)
//        }

        signOutBtn.setOnClickListener {
            mFirebaseViewModel!!.authSignOut()
            clearEntry()
            clearDisplayName()
        }

        changeEmailBtn.setOnClickListener {
            val newEmail = emailEt.text.toString().trim()
            mFirebaseViewModel!!.authChangeEmailAddress(newEmail)
        }

        setNewPasswordBtn.setOnClickListener {
            val newPassword = passwordEt.text.toString()
            mFirebaseViewModel!!.authSetNewPassword(newPassword)
        }

        sendPasswordResetBtn.setOnClickListener {
            mFirebaseViewModel!!.authSendPasswordReset(email.toString())
        }

        deleteUserBtn.setOnClickListener {
            mFirebaseViewModel!!.authDeleteUser()
        }

        //*********************************************
        //**RETROFIT*****
        val request = ServiceBuilder.buildService(RetrofitEndpoint::class.java)
        val call = request.getMovies("f4b7e5ccf9606a0350a04eec5b343df1")

        call.enqueue(object : Callback<PopularMovies> {
            override fun onResponse(call: Call<PopularMovies>, response: Response<PopularMovies>) {
                if(response.isSuccessful){
                    val resp = response.body()!!.results
                }
            }

            override fun onFailure(call: Call<PopularMovies>, t: Throwable) {
                Toast.makeText(this@MainActivity, "${t.message}", Toast.LENGTH_LONG).show()
            }

        })

        //*********************************************
    }

    override fun onStart() {
        super.onStart()

        mFirebaseViewModel!!.user?.observe(this, Observer { user ->
            if(user != null) {
                displayNameFieldTv.text = user.displayName
            } else {
                displayNameFieldTv.text = ""
            }
        })

        mFirebaseViewModel!!.isEmailChanged?.observe(this, Observer { isEmailChanged ->
            if(isEmailChanged){
                showToastMessage("EMAIL ADDRESS IS CHANGED")
            } else {
                showToastMessage("EMAIL ADDRESS IS NOT CHANGED")
            }
        })

        mFirebaseViewModel!!.isSignedUp?.observe(this, Observer {isSignedUp ->
            if(isSignedUp){
            } else {
                showToastMessage("THE EMAIL ADDRESS IS ALREADY IN USE BY ANOTHER USER!")
            }
        })

        mFirebaseViewModel!!.isSignedIn?.observe(this, Observer { isSignedIn ->
            if(isSignedIn){
            } else {
                showToastMessage("Email address and password does NOT match. For the first time? Please SIGN UP!")
            }
        })

    }

    override fun onResume() {
        super.onResume()
    }

    fun clearEntry(){
        displayNameEt.text.clear()
        emailEt.text.clear()
        passwordEt.text.clear()
    }

    fun clearDisplayName(){
        displayNameFieldTv.text = ""
    }

    companion object {
        private const val LOG_MSG = "MAIN LOG: "
    }

    private fun showToastMessage(message: String){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}