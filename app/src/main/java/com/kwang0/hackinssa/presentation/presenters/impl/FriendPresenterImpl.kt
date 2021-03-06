package com.kwang0.hackinssa.presentation.presenters.impl

import android.content.Context
import com.kwang0.hackinssa.data.dao.impl.FriendDaoImpl
import com.kwang0.hackinssa.data.models.Friend
import com.kwang0.hackinssa.data.repository.FriendRepository
import com.kwang0.hackinssa.data.repository.impl.FriendRepositoryImpl
import com.kwang0.hackinssa.presentation.presenters.FriendContract
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function3
import io.reactivex.schedulers.Schedulers

class FriendPresenterImpl(context: Context, private var view: FriendContract.View)
    : FriendContract.Presenter {
    private val TAG = FriendPresenterImpl::class.simpleName
    private val OPERATION_ALL = 0
    private val OPERATION_QUERY = 1
    private val OPERATION_TAG = 2

    private var friendRepository: FriendRepository
    private var friendDisposable: Disposable? = null
    private var friendDeleteDisposable: Disposable? = null

    private var currentOperation: Int? = null
    private var query: String = ""
    private var tagName: String = ""

    init {
        friendRepository = FriendRepositoryImpl(FriendDaoImpl(context))
    }

    // 입력값이 없을 때 친구 정보 검색
    override fun search() {
        this.currentOperation = OPERATION_ALL
        friendDisposable = friendRepository.getFriends()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ friends ->
                    view.addResultsToList(friends.toMutableList()) }, { throwable -> view.handleError(throwable) })
    }

    // 입력값을 기반으로 국가 정보 검색 (이름, 번호, 이메일)
    override fun search(query: String) {
        this.currentOperation = OPERATION_QUERY
        this.query = query

        tearDown()

        val nameRequest = friendRepository.getFriendsFromName(query).onErrorReturn { e -> listOf<Friend>() }
        val phoneRequest = friendRepository.getFriendsFromPhone(query).onErrorReturn { e -> listOf<Friend>() }
        val emailRequest = friendRepository.getFriendsFromEmail(query).onErrorReturn { e -> listOf<Friend>() }

        friendDisposable = Flowable.zip(nameRequest, phoneRequest, emailRequest,
                Function3<List<Friend>, List<Friend>, List<Friend>, List<Friend>> {
                    name, phone, email ->
                    val allFriends = name.toMutableList().plus(phone.toMutableList()).plus(email.toMutableList())
                    return@Function3 allFriends.distinctBy { it.friendId }.toMutableList()
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ friends -> view.addResultsToList(friends.toMutableList()) }, { throwable -> view.handleError(throwable) })
    }

    // 입력값을 기반으로 국가 정보 검색 (태그)
    override fun searchByTag(tagName: String) {
        this.currentOperation = OPERATION_TAG
        this.tagName = tagName

        tearDown()

        friendDisposable = friendRepository.getFriendsFromTagName(tagName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ friends ->
                    view.addResultsToList(friends.toMutableList()) }, { throwable -> view.handleError(throwable) })
    }

    override fun deleteFriend(friendId: String) {
        friendDeleteDisposable = friendRepository.deleteFriend(friendId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ view.finishDelete() }, { throwable -> view.handleError(throwable) })
    }

    // 해체 작업
    override fun tearDown() {
        if (friendDisposable?.isDisposed?.not() == true)
            friendDisposable?.dispose()
    }

    // 데이터 복구
    override fun restoreData() {
        if(currentOperation == null) return
        when (currentOperation) {
            OPERATION_ALL -> search()
            OPERATION_QUERY -> search(query)
            OPERATION_TAG -> searchByTag(tagName)
            else -> search()
        }
    }
}