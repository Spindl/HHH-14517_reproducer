package org.hibernate.bugs;

import static org.junit.Assert.assertNotEquals;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the three parameter locate method of the JPA criteria API. The third
 * parameter can be used to set a starting index for the locate function. This
 * could be used e.g. to find the nth occurrence of a pattern in a string
 * column.
 *
 * @author Roland Spindelbalker-Davila
 */
public class CriteriaLocateWithFromParamTest {

	private EntityManagerFactory h2Factory;
	private EntityManagerFactory oracleFactory;

	@Before
	public void init() {
		h2Factory = Persistence.createEntityManagerFactory("h2PU");
		oracleFactory = Persistence.createEntityManagerFactory("oraclePU");
	}

	@After
	public void destroy() {
		h2Factory.close();
		oracleFactory.close();
	}

	/**
	 * Tests using the JPA Criteria API locate() method to locate the 1st, 2nd and
	 * 3rd occurrence of a pattern in a string property using a H2 database.
	 */
	@Test
	public void locateWithFromParamH2() throws Exception {
		EntityManager entityManager = h2Factory.createEntityManager();
		entityManager.getTransaction().begin();
		entityManager.persist(new TestEntity("Finding not the first () but the second () occurrence of ()."));

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Integer> query = cb.createQuery(Integer.class);
		Root<TestEntity> from = query.from(TestEntity.class);

		Integer firstOccurrence = nthOccurrenceIndex(entityManager, cb, query, from, 0);
		Integer secondOccurrence = nthOccurrenceIndex(entityManager, cb, query, from, 1);
		Integer thirdOccurrence = nthOccurrenceIndex(entityManager, cb, query, from, 2);

		entityManager.getTransaction().commit();
		entityManager.close();

		assertNotEquals(firstOccurrence, secondOccurrence);
		assertNotEquals(secondOccurrence, thirdOccurrence);
		assertNotEquals(firstOccurrence, thirdOccurrence);
	}

	/**
	 * Tests using the JPA Criteria API locate() method to locate the 1st, 2nd and
	 * 3rd occurrence of a pattern in a string property using a Oracle database.
	 */
	@Test
	public void locateWithFromParamOracle() throws Exception {
		EntityManager entityManager = oracleFactory.createEntityManager();
		entityManager.getTransaction().begin();
		entityManager.persist(new TestEntity("Finding not the first () but the second () occurrence of ()."));

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Integer> query = cb.createQuery(Integer.class);
		Root<TestEntity> from = query.from(TestEntity.class);

		Integer firstOccurrence = nthOccurrenceIndex(entityManager, cb, query, from, 0);
		Integer secondOccurrence = nthOccurrenceIndex(entityManager, cb, query, from, 1);
		Integer thirdOccurrence = nthOccurrenceIndex(entityManager, cb, query, from, 2);

		entityManager.getTransaction().commit();
		entityManager.close();

		assertNotEquals(firstOccurrence, secondOccurrence);
		assertNotEquals(secondOccurrence, thirdOccurrence);
		assertNotEquals(firstOccurrence, thirdOccurrence);
	}

	/**
	 * Queries the database for the index of the nth occurrence of a pattern in the
	 * string property of the test entity.
	 */
	private Integer nthOccurrenceIndex(EntityManager entityManager, CriteriaBuilder cb, CriteriaQuery<Integer> query,
			Root<TestEntity> from, int occurrence) {
		return entityManager
				.createQuery(query.select(
						findNthOccurrenceExpression(cb, from.get("stringProperty"), cb.literal("()"), occurrence)))
				.getSingleResult();
	}

	/**
	 * Recursively nests a locate() expression, which would e.g. be needed to find
	 * the n-th occurrence of a pattern. <br>
	 * The idea is like this: <br>
	 * 1st occurrence: locate(column, pattern, 0) <br>
	 * 2nd occurrence: locate(column, pattern, locate(1st) + 1) <br>
	 * 3rd occurrence: locate(column, pattern, locate(2nd) + 1) <br>
	 */
	private Expression<Integer> findNthOccurrenceExpression(CriteriaBuilder cb, Path<String> from,
			Expression<String> pattern, int occurrence) {
		if (occurrence == 0) {
			return cb.locate(from, pattern);
		}
		return cb.locate(from, pattern, cb.sum(findNthOccurrenceExpression(cb, from, pattern, occurrence - 1), 1));
	}
}
