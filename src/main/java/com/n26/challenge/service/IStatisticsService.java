package com.n26.challenge.service;

import com.n26.challenge.api.model.StatisticsResult;

/**
 * Service that exposes functionalities to manage statistics information.
 *
 * @author Santiago Alzate S.
 * @version 1.0.0
 * @since 1.0.0
 */
public interface IStatisticsService {

	/**
	 * Creates a new record for the transaction statistics with the given information.
	 *
	 * @param timestamp the transaction time stamp
	 * @param amount the transaction amount
	 * @return the time stamp with the transaction expiration
	 */
	long createTransaction(long timeStamp, double amount);

	/**
	 * Returns the statistical information of the transactions
	 *
	 * @return A {@link StatisticsResult} with all the statistics of the transactions
	 */
	StatisticsResult getStatistics();

}
