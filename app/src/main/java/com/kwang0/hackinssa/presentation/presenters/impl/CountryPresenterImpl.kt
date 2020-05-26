package com.kwang0.hackinssa.presentation.presenters.impl

import android.util.Log
import com.kwang0.hackinssa.data.models.Country
import com.kwang0.hackinssa.data.remote.impl.CountryRepositoryRemoteImpl
import com.kwang0.hackinssa.data.repository.CountryRepository
import com.kwang0.hackinssa.data.repository.impl.CountryRepositoryImpl
import com.kwang0.hackinssa.presentation.presenters.CountryPresenter
import com.kwang0.hackinssa.presentation.presenters.CountryPresenterView
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function3
import io.reactivex.schedulers.Schedulers
import java.util.stream.StreamSupport

class CountryPresenterImpl(view: CountryPresenterView) : CountryPresenter {
    private val TAG = CountryPresenterImpl::class.simpleName

    private var view: CountryPresenterView? = view
    private var countryRepository: CountryRepository? = null
    private var countrySubscription: Disposable? = null

    private val OPERATION_QUERY = 0

    private var currentOperation = 0
    private var query: String? = null

    init {
        countryRepository = CountryRepositoryImpl(CountryRepositoryRemoteImpl())
    }

    override fun search(query: String?) {
        currentOperation = OPERATION_QUERY
        this.query = query

        val langRequest = countryRepository?.getByLang(query)?.onErrorReturn { e -> mutableListOf<Country?>() }
        val nameRequest = countryRepository?.getByName(query)?.onErrorReturn { e -> mutableListOf<Country?>() }
        val callingRequest = countryRepository?.getByCalling(query?.toIntOrNull())?.onErrorReturn { e -> mutableListOf<Country?>() }


//        countrySubscription = langRequest
//                ?.subscribeOn(Schedulers.io())
//                ?.observeOn(AndroidSchedulers.mainThread())
//                ?.doOnComplete({ view?.adapterNotifyChanges() })
//                ?.subscribe({ countries -> view?.addResultsToList(countries) }, { throwable -> view?.handleError(throwable) })
        countrySubscription = Flowable.zip(langRequest, nameRequest, callingRequest,
                Function3<MutableList<Country?>?, MutableList<Country?>?, MutableList<Country?>?, MutableList<Country?>?>
                { lang, name, calling ->
                    val allCountries = lang
                    allCountries.addAll(name)
                    allCountries.addAll(calling)
                    return@Function3 allCountries.distinctBy { it?.getName() }.sortedBy { it?.getName() }.toMutableList()
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                ?.doOnComplete({ view?.adapterNotifyChanges() })
                ?.subscribe({ countries -> view?.addResultsToList(countries) }, { throwable -> view?.handleError(throwable) })
    }

    override fun clear() {
        view?.handleEmpty()
    }

    override fun restoreData() {
        when (currentOperation) {
            OPERATION_QUERY -> search(query)
            else -> search(query)
        }
    }
}