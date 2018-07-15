package com.n26.challenge.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.n26.challenge.api.model.ApiTransaction;
import com.n26.challenge.api.model.StatisticsResult;
import com.n26.challenge.service.IStatisticsService;

/**
 * The Statistics Rest API Controller
 *
 * @author Santiago Alzate S.
 * @version 1.0.0
 * @since 1.0.0
 */
@RestController
public class StatisticsController {

	/**
	 * The statistics service
	 */
	private final IStatisticsService statisticsService;

	/**
	 * Default {@link StatisticsController} constructor
	 *
	 * @param balanceService
	 *            an {@link IStatisticsService}
	 */
	@Autowired
	public StatisticsController(final IStatisticsService balanceService) {

		this.statisticsService = balanceService;
	}

	/**
	 * Register a new transaction
	 *
	 * @param transaction the transaction to register
	 */
	@RequestMapping(method = RequestMethod.POST, path = "transactions")
	@ResponseStatus(HttpStatus.CREATED)
	public void registerTransaction(@RequestBody final ApiTransaction transaction) {

		validateNotNull(transaction, "The transaction can not be null");
		validateNotNull(transaction.getAmount(), "The transaction's amount can not be null");
		validateNotNull(transaction.getTimestamp(), "The transaction's time stamp can not be null");

		statisticsService.createTransaction(transaction.getTimestamp(), transaction.getAmount());
	}

	/**
	 * Query the statistical information of transactions for the last 60 seconds
	 *
	 * @return a {@link StatisticsResult}
	 */
	@RequestMapping(method = RequestMethod.GET, path = "statistics")
	public StatisticsResult queryStats() {

		return statisticsService.getStatistics();
	}

	/**
	 * IllegalArgumentException handler method that returns a
	 * {@link HttpStatus#NO_CONTENT} status and empty body
	 */
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@ExceptionHandler(IllegalArgumentException.class)
	public void exceptionHandler() {
		// Empty body should be returned and HTTP status code 204
	}

	/**
	 * Exception handler method
	 *
	 * @param exception
	 *            the exception
	 * @return A {@link String} with the exception message
	 */
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	@ExceptionHandler(Exception.class)
	public String exceptionHandler(final Exception exception) {

		return exception.getMessage();
	}

	/**
	 * Validates the object in the paremeter is not null
	 *
	 * @param object the object to validate
	 * @param message the exception message if required
	 */
	public static void validateNotNull(final Object object, final String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }
}
