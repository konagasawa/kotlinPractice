package com.technology.mycow.kotlinpractice

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Layout
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isEmpty
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.android.synthetic.main.actionbar.*
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private var mFirebaseViewModel: FirebaseViewModel? = null


    private lateinit var menuItem : MenuItem
    private lateinit var menuLayout : LinearLayout
    private lateinit var userIconTv : TextView

    private var userIconText : String = ""

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
                //val addCommentFragment = AddCommentFragment.sInstance
                //supportFragmentManager.beginTransaction().replace(R.id.root_frag_layout, addCommentFragment, "AddComment").commit()
                val mapFragment = MapFragment.sInstance //as MapFragment
                //val mapFragment = SupportMapFragment.newInstance()
                supportFragmentManager.beginTransaction().replace(R.id.root_frag_layout, mapFragment , "mapFragment").commit()
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

        authScreenUpdate()


        mFirebaseViewModel!!.user?.observe(this, Observer { user ->
            if(user != null) {

                //Show drawable as icon background in menu
                userIconText = user.displayName.substring(0,1).toUpperCase()
                if(!userIconText.isNullOrEmpty() && this::menuLayout.isInitialized){
                    userIconTv = menuLayout.findViewById(R.id.actionBarTx)
                    userIconTv.text = user.displayName.substring(0,1).toUpperCase()
                    menuLayout.background = resources.getDrawable(R.drawable.drawable_circle_on, null)
                }


                displayNameFieldTv.text = user.displayName
                //menuItem.title = user.displayName.substring(0,1).toUpperCase()
            } else {

                //Hide drawable as icon background in menu
                if(this::menuLayout.isInitialized){
                    userIconTv.text = ""
                    menuLayout.background = null

                    displayNameFieldTv.text = "NOT SIGNED"
                    menuItem.title = "NOT SIGNED"
                } else {
                    displayNameFieldTv.text = "NOT SIGNED"
                }
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {

        //Show drawable as icon background in menu. onPrepareOptionMenu runs after onCreateOptionMenu.
        //This code needs to be in here, not onCreationOptionMenu
        menuItem = menu?.findItem(R.id.currentUser)!!
        menuLayout = menuItem.actionView as LinearLayout
        userIconTv = menuLayout.findViewById(R.id.actionBarTx)
        if(!userIconText.isNullOrEmpty()){
            userIconTv.text = userIconText
            menuLayout.background = resources.getDrawable(R.drawable.drawable_circle_on, null)
        } else {
            userIconTv.text = ""
            menuLayout.background = null
        }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId) {
        //super.onOptionsItemSelected(item)

        R.id.currentUser -> {
            Log.d(LOG_MSG, "MENU CLICKED!!")
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

//    fun showPopup(v: View) {
//        val popup = PopupMenu(this, v)
//        val inflater: MenuInflater = popup.menuInflater
//        inflater.inflate(R.menu.user_profile_menu, popup.menu)
//        popup.show()
//    }

    fun calcLiked(id: Int) : Map<String, Int> {

        val likedList = mutableMapOf<String, Int>()

        return likedList
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

    fun authScreenUpdate(){

        if(mFirebaseViewModel?.user != null){
            displayNameTv.visibility = View.VISIBLE
            displayNameEt.visibility = View.VISIBLE
            emailTv.visibility = View.VISIBLE
            emailEt.visibility = View.VISIBLE
            passwordTv.visibility = View.VISIBLE
            passwordEt.visibility = View.VISIBLE

            signInBtn.visibility = View.VISIBLE
            signUpBtn.visibility = View.VISIBLE
            signOutBtn.visibility = View.GONE
            changeEmailBtn.visibility = View.GONE
            setNewPasswordBtn.visibility = View.GONE
            sendPasswordResetBtn.visibility = View.GONE
            deleteUserBtn.visibility = View.GONE
        }

        mFirebaseViewModel?.user?.observe(this, Observer { signedUser ->

            if(signedUser != null){
                displayNameTv.visibility = View.GONE
                displayNameEt.visibility = View.GONE
                emailTv.visibility = View.GONE
                emailEt.visibility = View.GONE
                passwordTv.visibility = View.GONE
                passwordEt.visibility = View.GONE

                signInBtn.visibility = View.GONE
                signUpBtn.visibility = View.GONE
                signOutBtn.visibility = View.VISIBLE
                changeEmailBtn.visibility = View.VISIBLE
                setNewPasswordBtn.visibility = View.VISIBLE
                sendPasswordResetBtn.visibility = View.VISIBLE
                deleteUserBtn.visibility = View.VISIBLE
            } else {
                displayNameTv.visibility = View.GONE
                displayNameEt.visibility = View.GONE

                emailTv.visibility = View.VISIBLE
                emailEt.visibility = View.VISIBLE
                passwordTv.visibility = View.VISIBLE
                passwordEt.visibility = View.VISIBLE

                signInBtn.visibility = View.VISIBLE
                signUpBtn.visibility = View.VISIBLE
                signOutBtn.visibility = View.GONE
                changeEmailBtn.visibility = View.GONE
                setNewPasswordBtn.visibility = View.GONE
                sendPasswordResetBtn.visibility = View.GONE
                deleteUserBtn.visibility = View.GONE
            }

        })

    }

    companion object {
        private const val LOG_MSG = "MAIN LOG: "
    }

    private fun showToastMessage(message: String){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}