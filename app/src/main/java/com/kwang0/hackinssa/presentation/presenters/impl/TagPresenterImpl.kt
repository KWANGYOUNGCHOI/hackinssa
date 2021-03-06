package com.kwang0.hackinssa.presentation.presenters.impl

import android.content.Context
import com.kwang0.hackinssa.data.dao.impl.TagDaoImpl
import com.kwang0.hackinssa.data.repository.TagRepository
import com.kwang0.hackinssa.data.repository.impl.TagRepositoryImpl
import com.kwang0.hackinssa.presentation.presenters.TagContract
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class TagPresenterImpl(context: Context, private var view: TagContract.View)
    : TagContract.Presenter {
    private val TAG = TagPresenterImpl::class.simpleName
    private val OPERATION_CLEAR = 0
    private val OPERATION_QUERY = 1

    private var tagRepository: TagRepository
    private var tagDisposable: Disposable? = null
    private var tagDeleteDisposable: Disposable? = null

    private var currentOperation: Int? = null
    private var tagName: String = ""

    init {
        tagRepository = TagRepositoryImpl(TagDaoImpl(context))
    }

    // 입력값을 기반으로 국가 정보 검색 (태그)
    override fun searchByTagName(tagName: String) {
        this.currentOperation = OPERATION_QUERY
        this.tagName = tagName

        tearDown()

        tagDisposable = tagRepository.getTagByName(tagName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ tags ->
                    view.addResultsToList(tags.toMutableList().sortedBy { it.tagCreated }
                            .distinctBy { it.tagName }
                            .toMutableList()) }, { throwable -> view.handleError(throwable) })
    }

    override fun deleteByTagNames(tagNames: List<String>) {
        tagDeleteDisposable = tagRepository.deleteTagByNames(tagNames)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ view.finishDelete() }, { throwable -> view.handleError(throwable) })
    }

    // 해체 작업
    override fun tearDown() {
        if (tagDisposable?.isDisposed?.not() == true)
            tagDisposable?.dispose()
    }

    // 비어있는 페이지 호출
    override fun clear() {
        this.currentOperation = OPERATION_CLEAR
        tagName = ""
        view.handleEmpty()
    }

    // 데이터 복구
    override fun restoreData() {
        if(currentOperation == null) return
        when (currentOperation) {
            OPERATION_CLEAR -> clear()
            OPERATION_QUERY -> searchByTagName(tagName)
            else -> clear()
        }
    }
}