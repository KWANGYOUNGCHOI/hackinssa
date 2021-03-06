package com.kwang0.hackinssa.presentation.presenters

import com.kwang0.hackinssa.data.models.Friend
import com.kwang0.hackinssa.data.models.Tag

interface FriendInfoContract {
    interface Presenter {
        fun onCreate()
        fun onStart()
        fun onStop()
        fun onPhoneSelect()
        fun onEmailSelect()
        fun onEditSelect()
    }

    interface View {
        fun addFriendResult(friend: Friend)
        fun addTagResultsToList(tagList: List<Tag>)
        fun clearTags()
        fun handleError(throwable: Throwable?)
        fun setAvatar(path: String)
        fun setNameText(name: String)
        fun setPhoneText(phone: String?)
        fun setEmailText(email: String?)
        fun setCountryFlag(code: String)
    }
}