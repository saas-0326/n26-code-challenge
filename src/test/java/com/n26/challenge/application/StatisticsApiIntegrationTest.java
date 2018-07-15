package com.n26.challenge.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.n26.challenge.api.model.ApiTransaction;
import com.n26.challenge.api.model.StatisticsResult;
import com.n26.challenge.model.Transaction;
import com.n26.challenge.service.StatisticsService;

/**
 * Test class for the whole Statistics API
 *
 * @author Santiago Alzate S.
 * @version 1.0.0
 * @since 1.0.0
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class StatisticsApiIntegrationTest extends AbstractTestNGSpringContextTests {

	/**
	 * Test rest template to send requests to the API
	 */
	@Autowired
	private TestRestTemplate restTemplate;

	/**
	 * The Giga Space service to reset the transactions before each test
	 */
	private GigaSpace gigaSpace;

	/**
	 * Creates the set up for the test cases
	 */
	@BeforeClass
	public void setUp() {

		gigaSpace = new GigaSpaceConfigurer(new UrlSpaceConfigurer("/./bankTransactions")).gigaSpace();
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
	@Test(description = "Test case inserting a transaction when the time stamp is from an old date")
	public void createTransactionTestOldDate() {

		// November 3rd 2016 time stamp
		final long timeStamp = 1478192204000L;

		final ResponseEntity<Void> entity = postTransaction(123D, timeStamp);

		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(entity.getBody()).isNull();
		assertThat(gigaSpace.count(new Transaction())).isEqualTo(0);
	}

	/**
	 * Test case registering a transaction successfully
	 */
	@Test(description = "Test case registering a transaction successfully")
	public void createTransactionTestSuccess() {

		final ResponseEntity<Void> entity = postTransaction(123D, Instant.now().toEpochMilli());

		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(entity.getBody()).isNull();
		assertThat(gigaSpace.count(new Transaction())).isEqualTo(1);
	}

	/**
	 * Test case registering a transaction from 30 seconds in the past
	 */
	@Test(description = "Test case registering a transaction from 30 seconds in the past")
	public void createTransactionTest30Seconds() {

		// Current Time stamp minus 30 seconds
		final long timeStamp = Instant.now().toEpochMilli() - 30_000;

		final ResponseEntity<Void> entity = postTransaction(123D, timeStamp);

		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(entity.getBody()).isNull();
		assertThat(gigaSpace.count(new Transaction())).isEqualTo(1);
	}

	/**
	 * Test case registering multiple transactions
	 */
	@Test(description = "Test case registering multiple transactions")
	public void createTransactionTestMultipleTransactions() {

		// Current Time stamp minus 30 seconds
		final long timeStamp = Instant.now().toEpochMilli() - 30_000;
		final int transactionsNumber = 3;

		for (int i = 0; i < transactionsNumber; i++) {
			final ResponseEntity<Void> entity = postTransaction(123D, timeStamp);
			assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
			assertThat(entity.getBody()).isNull();
		}

		assertThat(gigaSpace.count(new Transaction()))
				.as("There should be exactly %s transactions stored", transactionsNumber).isEqualTo(transactionsNumber);
	}

	/**
	 * Test case validating transaction expiration
	 *
	 * @throws InterruptedException
	 *             if the thread is interrupter
	 */
	@Test(description = "Test case validating transaction expiration")
	public void createTransactionTestExpiringTransactions() throws InterruptedException {

		// Create 2 transactions with current time stamp minus 30 seconds
		// Create 1 transaction with a time stamp about to be expired (current -
		// 59 seconds)
		final long currentTimeStamp = Instant.now().toEpochMilli();
		final long timeStamp = currentTimeStamp - 30_000;
		final long expiredTimeStamp = currentTimeStamp - 59_000;

		postTransaction(123D, timeStamp);
		postTransaction(123D, timeStamp);
		postTransaction(123D, expiredTimeStamp);

		Assert.assertEquals(gigaSpace.count(new Transaction()), 3, "There should be exactly 3 transactions stored");

		TimeUnit.SECONDS.sleep(2);
		Assert.assertEquals(gigaSpace.count(new Transaction()), 2,
				"There should be exactly 2 transactions stored after a couple of seconds");
	}

	/**
	 * Test case getting statistics when no transactions have been registered
	 */
	@Test(description = "Test case getting statistics when no transactions have been registered")
	public void getStatisticsTestEmptyTransactions() {

		final ResponseEntity<StatisticsResult> entity = this.restTemplate.getForEntity("/statistics", StatisticsResult.class);

		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(entity.getBody()).isNotNull();

		assertThat(entity.getBody().getAvg()).isEqualByComparingTo(0d);
		assertThat(entity.getBody().getCount()).isEqualByComparingTo(0L);
		assertThat(entity.getBody().getMax()).isEqualByComparingTo(0d);
		assertThat(entity.getBody().getMin()).isEqualByComparingTo(0d);
		assertThat(entity.getBody().getSum()).isEqualByComparingTo(0d);
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
		postTransaction(100d, timeStamp);
		postTransaction(200d, timeStamp);
		for (int i = 2; i < totalTransactions; i++) {
			// Random between 100 and 200
			final double amount = Math.random() * 100 + 100;
			totalAmount += amount;
			postTransaction(amount, timeStamp);
		}

		final ResponseEntity<StatisticsResult> entity = this.restTemplate.getForEntity("/statistics", StatisticsResult.class);

		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(entity.getBody()).isNotNull();

		assertThat(entity.getBody().getAvg()).isCloseTo(totalAmount / totalTransactions, within(0.1));
		assertThat(entity.getBody().getCount()).isEqualTo(totalTransactions);
		assertThat(entity.getBody().getMax()).isEqualByComparingTo(200d);
		assertThat(entity.getBody().getMin()).isEqualByComparingTo(100d);
		assertThat(entity.getBody().getSum()).isCloseTo(totalAmount, within(0.1));
	}

	/**
	 * Sends a POST request to the API to register a transaction
	 *
	 * @param amount
	 *            the transaction's amount
	 * @param timeStamp
	 *            the transaction's time stamp
	 * @return The response entity from the API
	 */
	private ResponseEntity<Void> postTransaction(final double amount, final long timeStamp) {

		final ApiTransaction transaction = new ApiTransaction();
		transaction.setTimestamp(timeStamp);
		transaction.setAmount(amount);

		return this.restTemplate.postForEntity("/transactions", transaction, Void.class);
	}

}
