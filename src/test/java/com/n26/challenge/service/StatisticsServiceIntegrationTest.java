package com.n26.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assertions.within;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.n26.challenge.api.model.StatisticsResult;
import com.n26.challenge.model.Transaction;

/**
 * Test class for {@link StatisticsService} deploying the in memory data grid.
 *
 * @author Santiago Alzate S. (santiago.alzate@payulatam.com)
 * @version 1.0.0
 * @since 1.0.0
 */
public class StatisticsServiceIntegrationTest {

	/**
	 * Class under test
	 */
	private StatisticsService service;

	/**
	 * The Giga Space service
	 */
	private GigaSpace gigaSpace;

	/**
	 * Creates the set up for the test cases
	 */
	@BeforeClass
	public void setUp() {

		gigaSpace = new GigaSpaceConfigurer(new UrlSpaceConfigurer("/./bankTransactionsTest")).gigaSpace();

		service = new StatisticsService(gigaSpace);
	}

	/**
	 * Cleans the space after each test runs
	 */
	@AfterMethod
	public void cleanEnvironment() {

		gigaSpace.clear(new Transaction());
	}

	/**
	 * Test case inserting a transaction when the time stamp is from an old date
	 */
	@Test(description = "Test case inserting a transaction when the time stamp is from an old date",
			expectedExceptions = IllegalArgumentException.class,
			expectedExceptionsMessageRegExp =
				"The transaction timestamp can not be older than 60 seconds nor in the future.")
	public void createTransactionTestOldDate() {

		// November 3rd 2016 time stamp
		final long timeStamp = 1478192204000L;

		service.createTransaction(timeStamp, 123d);
		fail("An exception should have been thrown");
	}

	/**
	 * Test case for {@link StatisticsService#createTransaction(long, double)}
	 * method inserting a transaction from 30 seconds in the past
	 */
	@Test(description = "Test case for createTransaction method inserting a transaction from 30 seconds in the past")
	public void createTransactionTest30Seconds() {

		// Current Time stamp minus 30 seconds
		final long timeStamp = Instant.now().toEpochMilli() - 30_000;

		final long expiration = service.createTransaction(timeStamp, 123d);

		// Transaction life time vs real expiration should equals.
		// With 100 ms as delta in case the test takes a while to run
		assertThat(expiration).as("Transaction life time should be 60 seconds").isCloseTo(timeStamp + 60_000,
				within(100L));
	}

	/**
	 * Test case for {@link StatisticsService#createTransaction(long, double)}
	 * method inserting multiple transactions
	 */
	@Test(description = "Test case for createTransaction method inserting multiple transactions")
	public void createTransactionTestMultipleTransactions() {

		// Current Time stamp minus 30 seconds
		final long timeStamp = Instant.now().toEpochMilli() - 30_000;
		service.createTransaction(timeStamp, 123d);
		service.createTransaction(timeStamp, 123d);
		service.createTransaction(timeStamp, 123d);

		assertThat(gigaSpace.count(new Transaction())).as("There should be exactly 3 transactions stored").isEqualTo(3);
	}

	/**
	 * Test case for {@link StatisticsService#createTransaction(long, double)}
	 * method validating transaction expiration
	 *
	 * @throws InterruptedException
	 *             if the thread is interrupter
	 */
	@Test(description = "Test case for createTransaction method validating transaction expiration")
	public void createTransactionTestExpiringTransactions() throws InterruptedException {

		// Create 2 transactions with current time stamp minus 30 seconds
		// Create 1 transaction with a time stamp about to be expired (current -
		// 59 seconds)
		final long currentTimeStamp = Instant.now().toEpochMilli();
		final long timeStamp = currentTimeStamp - 30_000;
		final long expiredTimeStamp = currentTimeStamp - 59_000;

		service.createTransaction(timeStamp, 123d);
		service.createTransaction(timeStamp, 123d);
		service.createTransaction(expiredTimeStamp, 123d);

		assertThat(gigaSpace.count(new Transaction())).as("There should be exactly 3 transactions stored").isEqualTo(3);

		TimeUnit.SECONDS.sleep(2);
		assertThat(gigaSpace.count(new Transaction()))
				.as("There should be exactly 2 transactions stored after a couple of seconds").isEqualTo(2);
	}

	/**
	 * Test case for {@link StatisticsService#getStatistics()} method when no
	 * transactions have been registered
	 */
	@Test(description = "Test case for getStatistics method when no transactions have been registered")
	public void getStatisticsTestEmptyTransactions() {

		final StatisticsResult statistics = service.getStatistics();

		assertThat(statistics.getAvg()).isEqualByComparingTo(0d);
		assertThat(statistics.getCount()).isEqualByComparingTo(0L);
		assertThat(statistics.getMax()).isEqualByComparingTo(0d);
		assertThat(statistics.getMin()).isEqualByComparingTo(0d);
		assertThat(statistics.getSum()).isEqualByComparingTo(0d);
	}

	/**
	 * Test case for {@link StatisticsService#getStatistics()} method after
	 * registering some transactions
	 */
	@Test(description = "Test case for getStatistics method after registering some transactions")
	public void getStatisticsTestMultipleTransactions() {

		// Current Time stamp
		final long timeStamp = Instant.now().toEpochMilli();
		final int totalTransactions = 10;

		// Insert 10 records (1 max amount, 1 min amount & 8 random values)
		double totalAmount = 300;
		service.createTransaction(timeStamp, 100d);
		service.createTransaction(timeStamp, 200d);
		for (int i = 2; i < totalTransactions; i++) {
			// Random between 100 and 200
			final double amount = Math.random() * 100 + 100;
			totalAmount += amount;
			service.createTransaction(timeStamp, amount);
		}

		final StatisticsResult statistics = service.getStatistics();

		assertThat(statistics.getAvg()).isCloseTo(totalAmount / totalTransactions, within(0.1));
		assertThat(statistics.getCount()).isEqualTo(totalTransactions);
		assertThat(statistics.getMax()).isEqualByComparingTo(200d);
		assertThat(statistics.getMin()).isEqualByComparingTo(100d);
		assertThat(statistics.getSum()).isCloseTo(totalAmount, within(0.1));
	}

}
