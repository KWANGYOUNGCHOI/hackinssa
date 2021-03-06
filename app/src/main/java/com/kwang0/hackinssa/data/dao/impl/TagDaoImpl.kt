package com.kwang0.hackinssa.data.dao.impl

import android.content.Context
import com.kwang0.hackinssa.data.InssaDatabase
import com.kwang0.hackinssa.data.dao.TagDao
import com.kwang0.hackinssa.data.models.Tag
import io.reactivex.Completable
import io.reactivex.Flowable

class TagDaoImpl(private val context: Context): TagDao {
    var database: InssaDatabase
    var tagDao: TagDao

    init {
        database = InssaDatabase.getInstance(context)
        tagDao = database.tagDao()
    }

    override fun getTags(): Flowable<List<Tag>> {
        return tagDao.getTags()
    }

    override fun getTagById(friendId: String): Flowable<List<Tag>> {
        return tagDao.getTagById(friendId)
    }

    override fun getTagByName(tagName: String): Flowable<List<Tag>> {
        return tagDao.getTagByName(tagName)
    }

    override fun insertTag(tag: Tag): Completable {
        return tagDao.insertTag(tag)
    }

    override fun insertTags(tagList: List<Tag>): Completable {
        return tagDao.insertTags(tagList)
    }

    override fun deleteTagById(friendId: String): Completable {
        return tagDao.deleteTagById(friendId)
    }

    override fun deleteTagByNames(tagNames: List<String>): Completable {
        return tagDao.deleteTagByNames(tagNames)
    }

    override fun deleteTag(friendId: String, tagName: String): Completable {
        return tagDao.deleteTag(friendId, tagName)
    }

}