package com.s95ammar.rxcurrencyconverter.models.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "currencies")
public class Currency {

	@PrimaryKey
	@NonNull
	private String code;

	private String name;

	@ColumnInfo(name = "usd_rate")
	private double usdRate;

	@ColumnInfo(name = "last_updated")
	private long lastUpdated;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getUsdRate() {
		return usdRate;
	}

	public void setUsdRate(double usdRate) {
		this.usdRate = usdRate;
	}

	public long getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(long lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public Currency(String code, String name, double usdRate, long lastUpdated) {
		this.code = code;
		this.name = name;
		this.usdRate = usdRate;
		this.lastUpdated = lastUpdated;
	}

	@Override
	public String toString() {
		return "Currency{" +
				"code='" + code + '\'' +
				", name='" + name + '\'' +
				", usdRate=" + usdRate +
				", lastUpdated=" + lastUpdated +
				'}';
	}
}
