package com.s95ammar.rxcurrencyconverter.views.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.lifecycle.ViewModelProvider;

import com.s95ammar.rxcurrencyconverter.R;
import com.s95ammar.rxcurrencyconverter.models.Result;
import com.s95ammar.rxcurrencyconverter.viewModels.MainViewModel;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.support.DaggerAppCompatActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends DaggerAppCompatActivity {
	private final String t = "log_" + getClass().getSimpleName();

	private CompositeDisposable disposables = new CompositeDisposable();

	@Inject
	ViewModelProvider.Factory factory;
	MainViewModel viewModel;

	@BindView(R.id.spinner_from)
	Spinner spinnerFrom;

	@BindView(R.id.spinner_to)
	Spinner spinnerTo;

	@BindView((R.id.progressBar))
	ProgressBar progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		viewModel = new ViewModelProvider(this, factory).get(MainViewModel.class);
		ButterKnife.bind(this);
		viewModel.getOnDatabasePopulation().observe(this, this::observeDatabasePopulation);
	}

	private void observeDatabasePopulation(Result<Void> result) {
		switch (result.status) {
			case LOADING:
				setLoading(true);
				break;
			case SUCCESS:
				setLoading(false);
				setUpSpinners();
				break;

			case ERROR:
				setLoading(false);
				viewModel.checkSavedData();
				viewModel.getOnSavedDataChecked().observe(this, this::observeSavedDataCheck);
				break;
		}
	}

	private void observeSavedDataCheck(Result<Void> result) {
		switch (result.status) {
			case LOADING:
				setLoading(true);
				break;
			case SUCCESS:
				setLoading(false);
				setUpSpinners();
//				TODO: SHOW WARNING
				break;
			case ERROR:
				setLoading(false);
//				TODO: SHOW ERROR
				break;
		}

	}

	private void setUpSpinners() {
		final Context activity = this;
		disposables.add(
				viewModel.getAllCurrencies()
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(currencies -> {
							List<String> rows = viewModel.getCurrenciesNamesList(currencies);
							spinnerFrom.setAdapter(new ArrayAdapter<>(activity, R.layout.spinner_row, rows));
							spinnerTo.setAdapter(new ArrayAdapter<>(activity, R.layout.spinner_row, rows));
						})
		);
	}

	private void setLoading(boolean isLoading) {
		if (isLoading) {
			progressBar.setVisibility(View.VISIBLE);
		} else {
			progressBar.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		disposables.clear();
	}
}
