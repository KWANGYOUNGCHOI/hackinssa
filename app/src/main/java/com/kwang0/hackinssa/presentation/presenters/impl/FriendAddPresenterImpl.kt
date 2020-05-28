package com.kwang0.hackinssa.presentation.presenters.impl

import android.content.Context
import com.kwang0.hackinssa.data.dao.impl.FriendDaoImpl
import com.kwang0.hackinssa.data.dao.impl.TagDaoImpl
import com.kwang0.hackinssa.data.models.Friend
import com.kwang0.hackinssa.data.models.Tag
import com.kwang0.hackinssa.data.repository.FriendRepository
import com.kwang0.hackinssa.data.repository.TagRepository
import com.kwang0.hackinssa.data.repository.impl.FriendRepositoryImpl
import com.kwang0.hackinssa.data.repository.impl.TagRepositoryImpl
import com.kwang0.hackinssa.presentation.presenters.FriendAddPresenter
import com.kwang0.hackinssa.presentation.presenters.FriendAddPresenterView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*

class FriendAddPresenterImpl(context: Context, private var view: FriendAddPresenterView): FriendAddPresenter {
    private val TAG = FriendAddPresenterImpl::class.simpleName

    private var friendRepository: FriendRepository
    private var tagRepository: TagRepository
    private var friendAddSubscription: Disposable? = null

    private var friend: Friend? = null
    private var tag: Tag? = null

    init {
        friendRepository = FriendRepositoryImpl(FriendDaoImpl(context))
        tagRepository = TagRepositoryImpl(TagDaoImpl(context))
    }

    override fun insertOrUpdateFriend(friendId: String?,
                                      friendAvatar: String,
                                      friendName: String,
                                      friendPhone: String?,
                                      friendEmail: String?,
                                      friendCountry: String,
                                      friendCreated: Long,
                                      tagList: MutableList<Tag>) {
        val friend = Friend(friendId = if(friendId == null) UUID.randomUUID().toString() else friendId,
                friendAvatar = friendAvatar,
                friendName = friendName,
                friendPhone = friendPhone,
                friendEmail = friendEmail,
                friendCountry = friendCountry,
                friendCreated = friendCreated)

        if(friendId == null) {
            friendAddSubscription = friendRepository.insertFriend(friend)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnComplete({
                        view.finishActivity()
//                        tagRepository.deleteTagById(friend.friendId)
//                                .doOnComplete({
//                                    tagList.forEach({ tag -> tagRepository.insertTag(Tag(friend.friendId, tag.tagName, tag.tagCreated))
//                                            .doOnComplete({ view.finishActivity() }) })
//                                })
                    })
                    .subscribe({ view.addBtnEnabled() }, { throwable -> view.handleError(throwable) })
        } else {
            friendAddSubscription = friendRepository.updateFriend(friend)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnComplete({
                        view.finishActivity()
//                        tagRepository.deleteTagById(friend.friendId)
//                                .doOnComplete({
//                                    tagList.forEach({ tag -> tagRepository.insertTag(Tag(friend.friendId, tag.tagName, tag.tagCreated))
//                                            .doOnComplete({ view.finishActivity() }) })
//                                })
                    })
                    .subscribe({ view.addBtnEnabled() }, { throwable -> view.handleError(throwable) })
        }

    }

}