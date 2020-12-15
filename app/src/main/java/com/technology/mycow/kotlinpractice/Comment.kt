package com.technology.mycow.kotlinpractice

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Comment(val userId: String, val userName: String, val comment: String, val postDate: Long, val liked: Boolean){

    @Exclude
    fun toMap(): Map<String, Any>{
        return mapOf(
            ConstantCollection.FIREBASE_ATTR_USERID to userId,
            ConstantCollection.FIREBASE_ATTR_USERNAME to userName,
            ConstantCollection.FIREBASE_ATTR_TOILETCOMMENT to comment,
            ConstantCollection.FIREBASE_ATTR_POSTDATE to postDate,
            ConstantCollection.FIREBASE_ATTR_LIKED to liked
        )
    }
}