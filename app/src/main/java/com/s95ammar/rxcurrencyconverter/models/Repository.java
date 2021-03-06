package com.s95ammar.rxcurrencyconverter.models;


import com.s95ammar.rxcurrencyconverter.models.data.Conversion;
import com.s95ammar.rxcurrencyconverter.models.data.Currency;
import com.s95ammar.rxcurrencyconverter.models.retrofit.ApiService;
import com.s95ammar.rxcurrencyconverter.models.retrofit.ConversionResponse;
import com.s95ammar.rxcurrencyconverter.models.room.CurrencyDao;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

import static com.s95ammar.rxcurrencyconverter.models.Result.Status.LOADING;
import static com.s95ammar.rxcurrencyconverter.models.Result.Status.SUCCESS;
import static com.s95ammar.rxcurrencyconverter.models.Result.Status.WARNING;
import static com.s95ammar.rxcurrencyconverter.util.Util.USD;

@Singleton
public class Repository {

	private CurrencyDao dao;
	private ApiService api;

	@Inject
	public Repository(CurrencyDao dao, ApiService api) {
		this.dao = dao;
		this.api = api;
	}

	public Observable<Result<List<Currency>>> getUsdRatesToAll() {
		return Observable.create(emitter -> new NetworkBoundResource<List<Currency>, ConversionResponse>(emitter) {
			@Override
			protected Single<ConversionResponse> createCall() {
				return api.getRatesToAll(USD);
			}

			@Override
			protected Completable saveCallResult(ConversionResponse response) {
				return dao.insertCurrencies(response.toCurrenciesListByUsd());
			}

			@Override
			protected Single<List<Currency>> loadFromDb() {
				return dao.getAllCurrencies();
			}
		});
	}

	//	Any conversion uses USD rate as a mediator for more efficient data storing (x/y = (USD/y) / (USD/x)).
	public Observable<Result<Conversion>> getRate(String from, String to, double amount) {
		return Observable.zip(
				getUsdRateTo(from),
				getUsdRateTo(to),
				(resultFrom, resultTo) -> {
					if (resultFrom.status == LOADING || resultTo.status == LOADING)
						return Result.loading();
					if (resultFrom.status == SUCCESS && resultTo.status == SUCCESS)
						return Result.success(
								new Conversion(resultFrom.data, resultTo.data, amount, System.currentTimeMillis())
						);
					if (resultFrom.status == WARNING || resultTo.status == WARNING)
						return Result.warning(
								new Conversion(resultFrom.data, resultTo.data, amount,
										Math.min(resultFrom.data.getLastUpdated(), resultTo.data.getLastUpdated())),
								resultFrom.message
						);
					return Result.error(resultFrom.message);

				}
		);
	}

	private Observable<Result<Currency>> getUsdRateTo(String code) {
		return Observable.create(emitter -> new NetworkBoundResource<Currency, ConversionResponse>(emitter) {

			@Override
			protected Single<ConversionResponse> createCall() {
				return api.getRate(USD, code);
			}

			@Override
			protected Completable saveCallResult(ConversionResponse response) {
				return dao.updateCurrency(response.toSingleCurrencyByUsd());
			}

			@Override
			protected Single<Currency> loadFromDb() {
				return dao.getCurrencyByCode(code);
			}
		});
	}

}
