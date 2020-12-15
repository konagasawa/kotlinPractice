package com.technology.mycow.kotlinpractice

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.add_commnet_fragment.*
import java.util.*

class AddCommentFragment : Fragment() {

    private var mFirebaseViewModel: FirebaseViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(mFirebaseViewModel == null){
            val factory = FirebaseViewModel.Factory(requireActivity().application)
            mFirebaseViewModel = ViewModelProvider(this, factory).get(FirebaseViewModel::class.java)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.add_commnet_fragment, container, false)
    }

    override fun onStart() {
        super.onStart()

        setListener()
        setObserver()

    }

    private fun setListener(){

        closeComment.setOnClickListener {
            fragmentManager?.beginTransaction()?.remove(this)?.commit()
            fragmentManager?.popBackStack()
        }

        postComment.setOnClickListener {
            val postDate = Date().time
            //val userObj = mFirebaseViewModel?.user?.map { item -> item.userId }
            val userId = mFirebaseViewModel?.user?.value?.userId
            val userName = mFirebaseViewModel?.user?.value?.displayName
            val comment = addComment.text.toString()
            val isLiked = mFirebaseViewModel?.likedState?.value ?: false
            val toiletComment = Comment(userId!!, userName!!, comment, postDate, isLiked)
            //val toilstCommentValue = toiletComment.toMap()

            val toiletId : String = "TOILET_ID"
            mFirebaseViewModel?.addToiletComment(toiletId, toiletComment)
        }

        likedBtn.setOnClickListener { view ->
            if(mFirebaseViewModel?.likedState?.value != null){
                if(mFirebaseViewModel?.likedState?.value!!){
                    mFirebaseViewModel?.likedState?.postValue(false)
                    likedBtn.setBackgroundResource(R.drawable.ic_baseline_favorite_border_24)
                } else {
                    mFirebaseViewModel?.likedState?.postValue(true)
                    likedBtn.setBackgroundResource(R.drawable.ic_baseline_favorite_24)
                }
            } else {
                mFirebaseViewModel?.likedState?.postValue(true)
                likedBtn.setBackgroundResource(R.drawable.ic_baseline_favorite_24)
            }
            Log.d(LOG_MSG, "PRESSED: " + view.isPressed)
        }
    }

    private fun setObserver(){
        mFirebaseViewModel?.isToiletCommentUploaded?.observe(viewLifecycleOwner, Observer { isUploaded ->
            if(isUploaded){
                Log.d(LOG_MSG, "UPLOAD SUCCEEDED")
            } else {
                Log.d(LOG_MSG, "UPLOADED FAILED")
            }
            fragmentManager?.popBackStack()
        })

        mFirebaseViewModel?.toiletLikedList?.observe(viewLifecycleOwner, Observer { likedList ->
            val commentTotal = likedList.getOrDefault(ConstantCollection.FIREBASE_TOILETCOMMENT_TOTAL, 0)
            val likedTotal = likedList.getOrDefault(ConstantCollection.FIREBASE_TOILETCOMMENT_LIKED_TOTAL, 0)
            Log.d(LOG_MSG, "COMMENT & LIKED" + commentTotal.toString() + ":" + likedTotal.toString())
        })

    }

    companion object {
        private const val LOG_MSG = "AddCommentFragment: "
        val sInstance : Fragment
            get() = AddCommentFragment()
    }

}