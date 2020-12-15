package com.technology.mycow.kotlinpractice

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

//sealed class Result<out R> {
//    data class Success<out T>(val data : T) : Result<T>()
//    data class Error(val exception: Exception) : Result<Nothing>()
//}

class FirebaseRepository {

    private val firebaseDB = FirebaseDatabase.getInstance()
    private val firebaseRef = Firebase.database.reference
    private lateinit var firebaseAuth : FirebaseAuth
    private var currUser : User? = null
    val currUserLiveData : MutableLiveData<User> by lazy { MutableLiveData<User>()}
    val emailChangedStatus : MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val signedUpStatus : MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val signedInStatus : MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    val user : LiveData<User>? get() = currUserLiveData
    val isEmailChanged : LiveData<Boolean>? get() = emailChangedStatus
    val isSignedUp : LiveData<Boolean>? get() = signedUpStatus
    val isSignedIn : LiveData<Boolean>? get() = signedInStatus


    val isToiletCommentUploaded: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val toiletLikedList: MutableLiveData<MutableMap<String, Int>> by lazy { MutableLiveData<MutableMap<String, Int>>() }


    suspend fun authSignInUser(email: String, password: String){
        authSetFirebaseAuth()
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    Log.d(LOG_MSG, "USER SIGNED IN")
                    currUser = User(firebaseAuth.currentUser!!.uid, firebaseAuth.currentUser!!.displayName!!)
                    currUserLiveData.postValue(currUser)
                    signedInStatus.postValue(true)
                } else {
                    Log.d(LOG_MSG, "USER SIGNED FAILED")
                    signedInStatus.postValue(false)
                    //throw FirebaseAuthException("","")
                }
            }
    }

    suspend fun authSignUpUser(email: String, password: String, displayName: String){
        authSetFirebaseAuth()
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->

                //val isNewUser = task.getResult()?.additionalUserInfo?.isNewUser

                if(task.isSuccessful){
                    Log.d(LOG_MSG, "USER CREATED" + firebaseAuth.currentUser!!.uid)

                    val userProfile = authUpdateUserProfile(displayName)
                    authUpdateUser(userProfile)
                    signedUpStatus.postValue(true)
                    signedInStatus.postValue(true)

                } else {
                    Log.d(LOG_MSG, "USER CREATE FAILED: " + task.exception)
                    signedUpStatus.postValue(false)
                    //throw FirebaseAuthException("","")
                }
            }
    }

    suspend fun authSignOutUser(){
        firebaseAuth.let {
            firebaseAuth.signOut()
            currUserLiveData.postValue(null)
        }
    }

    suspend fun authChangeEmailAddress(email: String) {
        authSetFirebaseAuth()
        firebaseAuth.currentUser.let {
            firebaseAuth.currentUser!!.updateEmail(email).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    Log.d(LOG_MSG, "EMAIL CHANGED")
                    emailChangedStatus.postValue(true)
                } else {
                    Log.d(LOG_MSG, "EMAIL CHANGE FAILED")
                    //throw FirebaseAuthException("","")
                    emailChangedStatus.postValue(false)
                }
            }
        }
    }

    suspend fun authSetNewPassword(newPassword: String){
        authSetFirebaseAuth()
        firebaseAuth.currentUser.let {
            firebaseAuth.currentUser!!.updatePassword(newPassword).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    Log.d(LOG_MSG, "PASSWORD CHANGED")
                } else {
                    Log.d(LOG_MSG, "PASSWORD CHANGE FAILED")
                    throw FirebaseAuthException("","")
                }
            }
        }
    }

    suspend fun authSendPasswordReset(email: String){
        authSetFirebaseAuth()
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if(task.isSuccessful){
                Log.d(LOG_MSG, "PASSWORD RESET EMAIL SENT")
            } else {
                Log.d(LOG_MSG, "PASSWORD RESET EMAIL SEND FAILED")
                throw FirebaseAuthException("","")
            }
        }
    }

    suspend fun authDeleteUser(){
        authSetFirebaseAuth()
        firebaseAuth.currentUser.let {
            firebaseAuth.currentUser!!.delete().addOnCompleteListener { task ->
                if(task.isSuccessful){
                    Log.d(LOG_MSG, "USER DELETED")
                } else {
                    Log.d(LOG_MSG, "USER DELETE FAILED")
                    throw FirebaseAuthException("","")
                }
            }
        }
    }

    private fun authUpdateUserProfile(userName: String): UserProfileChangeRequest {
        val profileUpdates = userProfileChangeRequest {
            displayName = userName
        }
        return profileUpdates
    }

    private fun authUpdateUser(profile: UserProfileChangeRequest){
        firebaseAuth.currentUser!!.updateProfile(profile).addOnCompleteListener { task ->
            if(task.isSuccessful){
                Log.d(LOG_MSG, "UPDATE USER NAME")
//                currUser = User(firebaseAuth.currentUser!!.uid, firebaseAuth.currentUser!!.displayName!!
                currUser = User(firebaseAuth.currentUser!!.uid, firebaseAuth.currentUser!!.displayName!!
                )
                currUserLiveData.postValue(currUser)

                //WRITE USER IN FIREBASE
                writeNewUser(currUser!!)
            }
        }
    }

    private suspend fun authSetFirebaseAuth(){
        firebaseAuth = Firebase.auth
    }

    private fun writeNewUser(user: User) {
        firebaseRef.child(ConstantCollection.FIREBASE_USERS)
            .child(user.userId)
            .child(ConstantCollection.FIREBASE_USER_NAME)
            .setValue(user.displayName)
            .addOnCompleteListener { task ->
            if(task.isSuccessful) {
                Log.d(LOG_MSG, "WRITE USER DONE")
            } else {
                Log.d(LOG_MSG, "WRITE USER FAILED")
            }
        }

//        firebaseRef.child(ConstantCollection.FIREBASE_USERS).push().setValue(user).addOnCompleteListener { task ->
//            if(task.isSuccessful) {
//                Log.d(LOG_MSG, "WRITE USER DONE")
//            } else {
//                Log.d(LOG_MSG, "WRITE USER FAILED")
//            }
//        }
    }

    data class UpdateComm( val comment: String, val postDate: Long, val liked: Boolean){
        fun toMap(): Map<String, Any>{return mapOf(
            ConstantCollection.FIREBASE_ATTR_TOILETCOMMENT to comment,
            ConstantCollection.FIREBASE_ATTR_POSTDATE to postDate,
            ConstantCollection.FIREBASE_ATTR_LIKED to liked)}
    }

    suspend fun addToiletComment(toildetId: String, comment: Comment) {

        val commendOne = firebaseRef.child(ConstantCollection.FIREBASE_COMMENTS).child("TOILET_ID").child("-MOCCtM4yow9hEzE_pFZ")
        val comm = UpdateComm("UPDATED!!",1600000000000,false)
        commendOne.apply {
            this.updateChildren(comm.toMap()).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    Log.d(LOG_MSG, "UPPDAT COMMENT DONE")
                } else {
                    Log.d(LOG_MSG, "UPPDAT COMMENT FAILED")
                }
            }
        }

//        firebaseRef.child(ConstantCollection.FIREBASE_COMMENTS).child(toildetId).push().setValue(comment).addOnCompleteListener { task ->
//            if(task.isSuccessful){
//                Log.d(LOG_MSG, "ADD COMMENT DONE")
//                isToiletCommentUploaded.postValue(true)
//
//                setNumberOfCommentsAndLiked("TOILET_ID")
//
//            } else {
//                Log.d(LOG_MSG, "ADD COMMENT FAILED")
//                isToiletCommentUploaded.postValue(false)
//            }
//        }
    }

    private fun setNumberOfCommentsAndLiked(toiletId: String){

        val a = firebaseRef.child("info").child("TOILET_ID").key

        val toiletCommentList = object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val resp: MutableMap<String, Comment> = snapshot.value as MutableMap<String, Comment>
                var likedCounter = 0
                resp.map { item ->
                    val commentData = item.value as Map<String, String>
                    if(commentData[ConstantCollection.FIREBASE_ATTR_LIKED] as Boolean){
                        likedCounter++
                    }
                }
                val toiletLikedListMap = mutableMapOf(
                    ConstantCollection.FIREBASE_TOILETCOMMENT_TOTAL to resp.size,
                    ConstantCollection.FIREBASE_TOILETCOMMENT_LIKED_TOTAL to likedCounter)
                toiletLikedList.postValue(toiletLikedListMap)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d(LOG_MSG, "TOILET COMMENT ADDED CANCELED")
            }
        }
        firebaseRef.child(ConstantCollection.FIREBASE_COMMENTS).child(toiletId).addValueEventListener(toiletCommentList)
    }

    suspend fun readToiletInfo(){

    }

    suspend fun writeToiletInfo(){

    }

    suspend fun readToiletComments(){

    }

    suspend fun writeToiletComment(){

    }

    suspend fun writeLikeToToilet(){

    }

    suspend fun readLikeFromToilet(){

    }

    companion object{
        private const val LOG_MSG = "FirebaseRepository: "
        private var sInstance: FirebaseRepository? = null
        fun getInstance(): FirebaseRepository? {
            if(sInstance == null){
                synchronized(FirebaseRepository::class.java){
                    if(sInstance == null){
                        sInstance = FirebaseRepository()
                    }
                }
            }
            return sInstance
        }
    }

}