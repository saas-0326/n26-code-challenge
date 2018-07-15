package com.n26.challenge.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.time.Instant;

import org.easymock.EasyMock;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.n26.challenge.AbstractGenericMockTest;
import com.n26.challenge.api.model.ApiTransaction;
import com.n26.challenge.api.model.StatisticsResult;
import com.n26.challenge.service.IStatisticsService;

/**
 * Test class for {@link StatisticsController} using mocks.
 *
 * @author Santiago Alzate S.
 * @version 1.0.0
 * @since 1.0.0
 */
public class StatisticsControllerMockTest extends AbstractGenericMockTest {

	/**
	 * Class under test
	 */
	private StatisticsController controller;

	/**
	 * The statistics service mock
	 */
	private IStatisticsService statisticsServiceMock;

	/**
	 * Creates the set up for the test cases
	 */
	@BeforeClass
	public void setUp() {

		statisticsServiceMock = EasyMock.createMock(IStatisticsService.class);
		registerMocks(statisticsServiceMock);

		controller = new StatisticsController(statisticsServiceMock);
	}

	/**
	 * Test case for {@link StatisticsController#registerTransaction(ApiTransaction)} method with null transaction
	 */
	@Test(description = "Test case for registerTransaction method with null transaction",
			expectedExceptions = IllegalArgumentException.class,
			expectedExceptionsMessageRegExp = "The transaction can not be null")
	public void registerTransactionTestNullTransaction() {

		controller.registerTransaction(null);

		fail("Exception should have been thrown");
	}

	/**
	 * Test case for {@link StatisticsController#registerTransaction(ApiTransaction)} method without amount parameter
	 */
	@Test(description = "Test case for registerTransaction method without amount parameter",
			expectedExceptions = IllegalArgumentException.class,
			expectedExceptionsMessageRegExp = "The transaction's amount can not be null")
	public void registerTransactionTestWithoutAmount() {

		final ApiTransaction transaction = new ApiTransaction();
		transaction.setTimestamp(Instant.now().toEpochMilli());

		controller.registerTransaction(transaction);

		fail("Exception should have been thrown");
	}

	/**
	 * Test case for {@link StatisticsController#registerTransaction(ApiTransaction)} method without time stamp parameter
	 */
	@Test(description = "Test case for registerTransaction method without time stamp parameter",
			expectedExceptions = IllegalArgumentException.class,
			expectedExceptionsMessageRegExp = "The transaction's time stamp can not be null")
	public void registerTransactionTestWithoutTimeStamp() {

		final ApiTransaction transaction = new ApiTransaction();
		transaction.setAmount(123D);

		controller.registerTransaction(transaction);

		fail("Exception should have been thrown");
	}

	/**
	 * Test case for {@link StatisticsController#registerTransaction(ApiTransaction)} method with successfully
	 */
	@Test(description = "Test case for registerTransaction method successfully")
	public void registerTransactionTestSuccess() {

		final ApiTransaction transaction = new ApiTransaction();
		transaction.setTimestamp(Instant.now().toEpochMilli());
		transaction.setAmount(123D);

		resetMocks();
		EasyMock.expect(statisticsServiceMock.createTransaction(transaction.getTimestamp(), transaction.getAmount()))
				.andReturn(0L);
		replayMocks();

		controller.registerTransaction(transaction);

		verifyMocks();
	}

	/**
	 * Test case for {@link StatisticsController#queryStats()} method with successful results
	 */
	@Test(description = "Test case for queryStats method successfully")
	public void queryStatsTestSuccess() {

		final ApiTransaction transaction = new ApiTransaction();
		transaction.setTimestamp(Instant.now().toEpochMilli());
		transaction.setAmount(123D);

		resetMocks();
		EasyMock.expect(statisticsServiceMock.getStatistics()).andReturn(new StatisticsResult(100, 10, 200, 50, 1000));
		replayMocks();

		StatisticsResult results = controller.queryStats();

		verifyMocks();
		assertThat(results.getAvg()).isEqualByComparingTo(100d);
		assertThat(results.getCount()).isEqualByComparingTo(10L);
		assertThat(results.getMax()).isEqualByComparingTo(200d);
		assertThat(results.getMin()).isEqualByComparingTo(50d);
		assertThat(results.getSum()).isEqualByComparingTo(1000d);
	}

}
