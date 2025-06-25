package ca.uhn.fhir.jpa.starter.service;

import java.io.File;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.uhn.fhir.jpa.starter.anonymization.ISANONYMIZED;
import ca.uhn.fhir.jpa.starter.model.*;
import ca.uhn.fhir.jpa.starter.model.ComGenerator.MessageStatus;
import com.iprd.fhir.utils.PatientIdentifierStatus;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;

public class NotificationDataSource {

	private static final Logger logger = LoggerFactory.getLogger(NotificationDataSource.class);

	Configuration conf;
	SessionFactory sf;
	private static NotificationDataSource notificationDataSource;
	private Class<?> aClass;

	private NotificationDataSource() {
	}

	public static NotificationDataSource getInstance() {
		if (notificationDataSource == null) {
			notificationDataSource = new NotificationDataSource();
		}
		return notificationDataSource;
	}

	public void configure(String filePath) {
		conf = new Configuration().configure(new File(filePath)).addAnnotatedClass(ComGenerator.class)
				.addAnnotatedClass(OrgHierarchy.class).addAnnotatedClass(OrgIndicatorAverageResult.class)
				.addAnnotatedClass(CacheEntity.class).addAnnotatedClass(MapCacheEntity.class).addAnnotatedClass(ApiAsyncTaskEntity.class)
				.addAnnotatedClass(EncounterIdEntity.class).addAnnotatedClass(PatientIdentifierEntity.class)
				.addAnnotatedClass(LastSyncEntity.class).addAnnotatedClass(SMSInfo.class)
				.addAnnotatedClass(EmailScheduleEntity.class);
		sf = conf.buildSessionFactory();
	}

	public void insert(Object object) {
		Session session = sf.openSession();
		Transaction transaction = session.beginTransaction();
		try {
			session.save(object);
			transaction.commit();
		} catch (Exception e) {
			logger.warn(ExceptionUtils.getStackTrace(e));
			transaction.rollback();
		} finally {
			session.close();
		}
	}

//	public void insertObjects(ArrayList<CacheEntity> cacheEntities) {
//		Session  session = sf.openSession();
//	    Transaction transaction = session.beginTransaction();
//	    try {
//	    	 SessionImplementor sessionImpl = (SessionImplementor) session;
//	         Connection conn = sessionImpl.connection();
//	         String query = "INSERT INTO cache (org_id, indicator, date, value, lastUpdated,id) VALUES (?,?, ?, ?, ?,?)";
//	         PreparedStatement ps = conn.prepareStatement(query);
//	        
//	        for (CacheEntity entity : cacheEntities) {
//	            ps.setString(1, entity.getOrgId());
//	            ps.setString(2, entity.getIndicator());
//	            ps.setDate(3, new java.sql.Date(entity.getDate().getTime()));
//	            ps.setDouble(4, entity.getValue());
//	            ps.setDate(5, new java.sql.Date(entity.getLastUpdated().getTime()));
//	            ps.setString(6, UUID.randomUUID().toString());
//	            ps.addBatch();
//	        }
//	        
//	        ps.executeBatch();
//	        transaction.commit();
//	        conn.close();
//	    } catch (Exception e) {
//	        e.printStackTrace();
//	        transaction.rollback();
//	    } finally {
//	        session.close();
//	    }
//	}

	public void persist(Object object) {
		Session session = sf.openSession();
		Transaction transaction = session.beginTransaction();
		try {
			session.persist(object);
			transaction.commit();
		} catch (PersistenceException ex) {
			// PersistenceException internally throws UniqueConstraintViolationError
			logger.info("Duplicate entry. Entity " + object.toString() + "Already exists");
			transaction.rollback();
		} finally {
			session.close();
		}
	}

	public void update(Object object) {
		Session session = sf.openSession();
		Transaction transaction = session.beginTransaction();
		try {
			session.update(object);
			transaction.commit();
		} catch (Exception e) {
			logger.warn(ExceptionUtils.getStackTrace(e));
			transaction.rollback();
		}

		session.close();
	}
	
	public void insertObjects(ArrayList<?> cacheEntities) {
		StatelessSession session = sf.openStatelessSession();
		Transaction transaction = session.beginTransaction();
		try {

			for (int i = 0; i < cacheEntities.size(); i++) {
				try {
					session.insert(cacheEntities.get(i));	
				}catch (ConstraintViolationException e) {
					//do nothing
				}
				
			}
			transaction.commit();
		} catch (Exception e) {
			logger.warn(ExceptionUtils.getStackTrace(e));
			transaction.rollback();
		} finally {
			session.close();
		}
	}

	public void insertObjectsWithListNative(List<?> cacheEntities) {
		StatelessSession session = sf.openStatelessSession();
		Transaction transaction = session.beginTransaction();

		try {
			for (int i = 0; i < cacheEntities.size(); i++) {
				Object entity = cacheEntities.get(i);

				// Create a native SQL query
				String sqlQuery = "INSERT IGNORE INTO organization_structure (id, orgId, level, countryParent, stateParent, lgaParent, wardParent) VALUES (?, ?, ?, ?, ?, ?, ?)";

				// Use a native SQL query to insert the data
				session.createSQLQuery(sqlQuery)
					.setParameter(1, ((OrgHierarchy) entity).getId())
					.setParameter(2, ((OrgHierarchy) entity).getOrgId()) // Assuming getOrgId() exists in your OrgHierarchy class
					.setParameter(3, ((OrgHierarchy) entity).getLevel()) // Similarly, use the corresponding getter methods
					.setParameter(4, ((OrgHierarchy) entity).getCountryParent())
					.setParameter(5, ((OrgHierarchy) entity).getStateParent())
					.setParameter(6, ((OrgHierarchy) entity).getLgaParent())
					.setParameter(7, ((OrgHierarchy) entity).getWardParent())
					.executeUpdate();
			}

			transaction.commit();
		} catch (Exception e) {
			logger.warn(ExceptionUtils.getStackTrace(e));
			transaction.rollback();
			logger.warn("in notification");
			// Rethrow the caught exception
			throw e;
		} finally {
			session.close();
		}
	}

	public void updateObjects(ArrayList<?> cacheEntities) {
		StatelessSession statelessSession = sf.openStatelessSession();
		Transaction transaction = statelessSession.beginTransaction();
		try {

			for (int i = 0; i < cacheEntities.size(); i++) {
				statelessSession.update(cacheEntities.get(i));
			}
			transaction.commit();
		} catch (Exception e) {
			logger.warn(ExceptionUtils.getStackTrace(e));
			transaction.rollback();
		} finally {
			statelessSession.close();
		}
	}


	public void updateObjectsWithSession(ArrayList<?> cacheEntities) {
		Session session = sf.openSession();
		Transaction transaction = session.beginTransaction();
		try {
			for (int i = 0; i < cacheEntities.size(); i++) {
				session.update(cacheEntities.get(i));
			}
			transaction.commit();
		} catch (Exception e) {
			logger.warn(ExceptionUtils.getStackTrace(e));
			transaction.rollback();
		} finally {
			session.close();
		}
	}

//	public void updateObjects(ArrayList<CacheEntity> cacheEntities) {
//	    Session session = sf.openSession();
//	    Transaction transaction = session.beginTransaction();
//	    try {
//	    	 SessionImplementor sessionImpl = (SessionImplementor) session;
//	         Connection conn = sessionImpl.connection();
//	        String query = "UPDATE cache SET org_id = ?, indicator = ?, date = ?, value = ?, lastUpdated = ? WHERE id = ?";
//	        PreparedStatement ps =  conn.prepareStatement(query);
//
//	        for (CacheEntity entity : cacheEntities) {
//	            ps.setString(1, entity.getOrgId());
//	            ps.setString(2, entity.getIndicator());
//	            ps.setDate(3, new java.sql.Date(entity.getDate().getTime()));
//	            ps.setDouble(4, entity.getValue());
//	            ps.setDate(5, new java.sql.Date(entity.getLastUpdated().getTime()));
//	            ps.setString(6, entity.getId());
//	            ps.addBatch();
//	        }
//	        
//	        ps.executeBatch();
//	        transaction.commit();
//	        conn.close();
//	    } catch (Exception e) {
//	        e.printStackTrace();
//	        transaction.rollback();
//	    } finally {
//	        session.close();
//	    }
//	}

	public void delete(Object object) {
		Session session = sf.openSession();
		Transaction transaction = session.beginTransaction();
		session.delete(object);
		transaction.commit();
		session.close();
	}

	public List<String> getPatientIdWithIdentifier(String patientId, String identifier) {
		Session session = sf.openSession();
		Query query = session.createQuery("SELECT patientId FROM PatientIdentifierEntity WHERE patientId !=:param1 AND patientIdentifier=:param2");
		query.setParameter("param1", patientId);
		query.setParameter("param2", identifier);
		List<String> resultList = query.getResultList();
		session.close();
		if(resultList.isEmpty()) {
			return Collections.emptyList();
		}
		return resultList;
	}

	public List<String> getRecordsByDistinctPatientId() {
		Session session = sf.openSession();
		Query query = session.createQuery("SELECT DISTINCT patientId FROM PatientIdentifierEntity");
		List<String> resultList = query.getResultList();
		session.close();
		if(resultList.isEmpty()) {
			return Collections.emptyList();
		}
		return resultList;
	}

	public void updateRecordsByPatientId(String orgId, String patientId) {
		try (Session session = sf.openSession()) {
			Transaction transaction = session.beginTransaction();

			Query query = session.createQuery("UPDATE PatientIdentifierEntity SET orgId=:param1 WHERE patientId=:param2");
			query.setParameter("param1", orgId);
			query.setParameter("param2", patientId);
			query.executeUpdate();
			transaction.commit();
		} catch (Exception e) {
			// Handle exceptions, log errors, and return a meaningful response
			logger.warn(ExceptionUtils.getStackTrace(e));
		}
	}

	public List<PatientIdentifierEntity> getPatientIdentifierEntityByPatientIdAndIdentifier(String patientId, String identifier) {
		Session session = sf.openSession();
		Query query = session.createQuery("FROM PatientIdentifierEntity WHERE patientIdentifier=:param1 AND patientId=:param2", PatientIdentifierEntity.class);
		query.setParameter("param1", identifier);
		query.setParameter("param2", patientId);
		List<PatientIdentifierEntity> resultList = query.getResultList();
		session.close();
		if(resultList.isEmpty()) {
			return Collections.emptyList();
		}
		return resultList;
	}
	
	public List<PatientIdentifierEntity> getPatientInfoResourceEntityDataBeyondLastUpdated(Long lastUpdated){
		Session session = sf.openSession();
		Query query = session.createQuery("SELECT p FROM PatientIdentifierEntity p WHERE p.updatedTime > :param1", PatientIdentifierEntity.class);
		query.setParameter("param1", lastUpdated);
		List<PatientIdentifierEntity> result = query.getResultList();
		if(result.isEmpty()) {
			return Collections.emptyList();
		}
		return result;
	}

	public List<PatientIdentifierEntity> getExistingEntryWithPatientIdAndIdentifier(String patientId, String patientIdentifier){
		Session session = sf.openSession();
		Query query = session.createQuery("SELECT p FROM PatientIdentifierEntity p WHERE p.patientId=:param1 AND p.patientIdentifier=:param2", PatientIdentifierEntity.class);
		query.setParameter("param1", patientId);
		query.setParameter("param2", patientIdentifier);
		List<PatientIdentifierEntity> result = query.getResultList();
		if(result.isEmpty()){
			return Collections.emptyList();
		}
		return result;
	}

	public List<PatientIdentifierEntity> getExistingEntriesWithPatientId(String patientId){
		Session session = sf.openSession();
		Query query = session.createQuery("SELECT p FROM PatientIdentifierEntity p WHERE p.patientId=:param1 AND p.orgId IS NULL", PatientIdentifierEntity.class);
		query.setParameter("param1", patientId);
		List<PatientIdentifierEntity> result = query.getResultList();
		if(result.isEmpty()){
			return Collections.emptyList();
		}
		return result;
	}

	public PatientIdentifierEntity getPatientIdentifierEntityWithDuplicateStatus(String patientId, String patientIdentifier) {
		// select * from patientIdentifierEntity where identifier = oldOclId and patientId != patientId and status = duplicate order by createdTime limit 1
		Session session = sf.openSession();
		Query query = session.createQuery("SELECT p FROM PatientIdentifierEntity p WHERE p.patientId !=:param1 AND p.patientIdentifier=:param2 AND p.status=:param3 ORDER BY createdTime", PatientIdentifierEntity.class);
		query.setParameter("param1", patientId);
		query.setParameter("param2", patientIdentifier);
		query.setParameter("param3", PatientIdentifierStatus.DUPLICATE.name());
		query.setMaxResults(1);
		List<PatientIdentifierEntity> result = query.getResultList();
		if(result.isEmpty()){
			return null;
		}
		return result.get(0);
	}

	public List<SMSInfo> fetchSMSRecordsByResourceId(String resourceId){
		Session session = sf.openSession();
		Query query = session
			.createQuery("FROM SMSInfo WHERE resourceId=:param1");
		query.setParameter("param1", resourceId);
		List<SMSInfo> resultList = query.getResultList();
		session.close();
		return resultList;
	}

	public List<ComGenerator> fetchRecordsByScheduledDateAndStatus(Date date, MessageStatus status) {
		Session session = sf.openSession();
		Query query = session
				.createQuery("FROM ComGenerator WHERE communicationStatus=:param1 AND scheduledDate=:param2");
		query.setParameter("param1", status.name());
		query.setParameter("param2", date);
		List<ComGenerator> resultList = query.getResultList();
		session.close();
		return resultList;
	}

	public List<ComGenerator> fetchCOMGenRecordsById(String resourceId, MessageStatus status) {
		Session session = sf.openSession();
		Query query = session
			.createQuery("FROM ComGenerator WHERE communicationStatus=:param1 AND resource_id=:param2");
		query.setParameter("param1", status.name());
		query.setParameter("param2", resourceId);
		List<ComGenerator> resultList = query.getResultList();
		session.close();
		return resultList;
	}

	public List<EncounterIdEntity> fetchAllFromEncounterIdEntity() {
		Session session = sf.openSession();
		Query query = session.createQuery("From EncounterIdEntity");
		List<EncounterIdEntity> encounterIds = query.getResultList();
		session.close();
		return encounterIds;
	}

	public ArrayList<ApiAsyncTaskEntity> fetchStatus(String uuid) {
		Session session = sf.openSession();
		Query query = session.createQuery("FROM ApiAsyncTaskEntity WHERE uuid=:param1 AND isAnonymousEntry=:param2");
		query.setParameter("param1", uuid);
		query.setParameter("param2", ISANONYMIZED.NO.name());
		ArrayList<ApiAsyncTaskEntity> resultList = (ArrayList<ApiAsyncTaskEntity>) query.getResultList();
		session.close();
		return resultList;
	}

	public ArrayList<ApiAsyncTaskEntity> fetchAnonymousData(String dateRangeUuid) {
		Session session = sf.openSession();
		Query query = session.createQuery("FROM ApiAsyncTaskEntity WHERE uuid LIKE :param1 AND isAnonymousEntry=:param2");
		query.setParameter("param1", dateRangeUuid);
		query.setParameter("param2",ISANONYMIZED.YES.name());
		ArrayList<ApiAsyncTaskEntity> resultList = (ArrayList<ApiAsyncTaskEntity>) query.getResultList();
		session.close();
		return resultList;
	}

	public ArrayList<ApiAsyncTaskEntity> fetchApiAsyncTaskEntityList(List<String> uuid) {
		Session session = sf.openSession();
		Query query = session.createQuery("FROM ApiAsyncTaskEntity WHERE uuid IN :param1");
		query.setParameter("param1", uuid);
		ArrayList<ApiAsyncTaskEntity> resultList = (ArrayList<ApiAsyncTaskEntity>) query.getResultList();
		session.close();
		return resultList;
	}


	public List<LastSyncEntity> getEntitiesByOrgEnvStatus(String orgId, String env, String status) {
		Session session = sf.openSession();
		Query query = session
			.createQuery("FROM LastSyncEntity WHERE org_id=:param1 AND envs=:param2 AND status=:param3 ORDER BY start_date_time DESC");
		query.setParameter("param1", orgId);
		query.setParameter("param2", env);
		query.setParameter("param3", status);
		query.setMaxResults(1); // Limit the result to 1 row
		List<LastSyncEntity> resultList = query.getResultList();
		session.close();
		return resultList;
	}

	public List<LastSyncEntity> fetchLastSyncEntitiesByOrgs(List<String> orgId, String env, String status, Timestamp startDateTime) {
		Session session = sf.openSession();
		Query query = session
			.createQuery("FROM LastSyncEntity WHERE org_id IN (:param1) AND envs=:param2 AND status=:param3 AND start_date_time > :param4 AND end_date_time IS NOT NULL");
		query.setParameter("param1", orgId);
		query.setParameter("param2", env);
		query.setParameter("param3", status);
		query.setParameter("param4", startDateTime);
		List<LastSyncEntity> resultList = query.getResultList();
		session.close();
		return resultList;
	}

	public List<CacheEntity> getCacheByDateIndicatorAndOrgId(Date date, String indicator, String orgId) {
		Session session = sf.openSession();
		Query query = session
				.createQuery("FROM CacheEntity WHERE date=:param1 AND indicator=:param2 AND org_id=:param3");
		query.setParameter("param1", date);
		query.setParameter("param2", indicator);
		query.setParameter("param3", orgId);
		List<CacheEntity> resultList = query.getResultList();
		session.close();
		return resultList;
	}

	public OrgHierarchy getOrganizationalHierarchyItem(String orgId) {
		try (Session session = sf.openSession()) {
			Query query = session.createQuery("FROM OrgHierarchy WHERE orgId=:param1");
			query.setParameter("param1", orgId);
			OrgHierarchy result = (OrgHierarchy) query.uniqueResult();
			return result;
		} catch (Exception e) {
			// Handle exceptions, log errors, and return a meaningful response
			logger.warn(ExceptionUtils.getStackTrace(e));
			return null; // or throw an exception
		}
	}

	public List<OrgHierarchy> getOrganizationalHierarchyList(String orgId) {
		try (Session session = sf.openSession()) {
			String sqlQuery =
				"SELECT * FROM organization_structure WHERE orgId = :param1 " +
					"UNION " +
					"SELECT * FROM organization_structure WHERE countryParent = :param2 OR stateParent = :param3 OR lgaParent = :param4 OR wardParent = :param5";

			List<OrgHierarchy> resultList = session
				.createNativeQuery(sqlQuery, OrgHierarchy.class)
				.setParameter("param1", orgId)
				.setParameter("param2", orgId)
				.setParameter("param3", orgId)
				.setParameter("param4", orgId)
				.setParameter("param5", orgId)
				.list();

			return resultList;
		} catch (Exception e) {
			// Handle exceptions, log errors, and return an empty list or throw an exception
			logger.warn(ExceptionUtils.getStackTrace(e));
			return Collections.emptyList(); // or throw an exception
		}
	}

	public List<OrgIndicatorAverageResult> getCacheValueAverageWithZeroByDateRangeIndicatorAndMultipleOrgIdForScorecard(List<String> orgIds, List<String> indicators, Date startDate, Date endDate) {
		Session session = sf.openSession();
		Transaction transaction = session.beginTransaction();
		List<OrgIndicatorAverageResult> orgIndicatorAverageResults = new ArrayList<>();

		try {
			Query query = session
				.createQuery(
					"SELECT new OrgIndicatorAverageResult(orgId, indicator, ROUND(AVG(value), 2) AS averageValue) " +
						"FROM CacheEntity " +
						"WHERE value <> -1 " + // Exclude rows where value is -1
						"AND orgId IN :param1 " +
						"AND indicator IN :param2 " +
						"AND date >= :param3 " +
						"AND date <= :param4 " +
						"GROUP BY orgId, indicator");

				query.setParameter("param1", orgIds)
						.setParameter("param2", indicators)
						.setParameter("param3", startDate)
						.setParameter("param4", endDate);

			orgIndicatorAverageResults = query.getResultList();
			transaction.commit();
		} catch (Exception e) {
			logger.warn(ExceptionUtils.getStackTrace(e));
			transaction.rollback();
		} finally {
			session.close();
		}

		return orgIndicatorAverageResults;

	}

	public List<OrgIndicatorAverageResult> getCacheValueAverageWithoutZeroByDateRangeIndicatorAndMultipleOrgIdForScorecard(List<String> orgIds, List<String> indicators, Date startDate, Date endDate) {
		Session session = sf.openSession();
		Transaction transaction = session.beginTransaction();
		List<OrgIndicatorAverageResult> orgIndicatorAverageResults = new ArrayList<>();

		try {
			Query query = session
				.createQuery(
					"SELECT new OrgIndicatorAverageResult(orgId, indicator, ROUND(AVG(value), 2) AS averageValue) " +
						"FROM CacheEntity " +
						"WHERE (value <> 0 AND value <> -1) " +
						"AND orgId IN :param1 " +
						"AND indicator IN :param2 " +
						"AND date >= :param3 " +
						"AND date <= :param4 " +
						"GROUP BY orgId, indicator");

			query.setParameter("param1", orgIds)
				.setParameter("param2", indicators)
				.setParameter("param3", startDate)
				.setParameter("param4", endDate);

			orgIndicatorAverageResults = query.getResultList();
			transaction.commit();
		} catch (Exception e) {
			logger.warn(ExceptionUtils.getStackTrace(e));
			transaction.rollback();
		} finally {
			session.close();
		}

		return orgIndicatorAverageResults;

	}

		public Double getCacheValueSumByDateRangeIndicatorAndMultipleOrgId(Date from, Date to, String indicator,
			List<String> orgIds) {
		Session session = sf.openSession();
		Query query = session.createQuery(
				"SELECT SUM(value) FROM CacheEntity WHERE value <> -1 AND date BETWEEN :param1 AND :param2 AND indicator=:param3 AND org_id IN (:param4)");
		query.setParameter("param1", from);
		query.setParameter("param2", to);
		query.setParameter("param3", indicator);
		query.setParameterList("param4", orgIds);
		List resultList = query.getResultList();
		session.close();
		if (resultList.isEmpty() || resultList.get(0) == null) {
			return 0.0;
		}

		return (Double) resultList.get(0);
	}

	public Double getCacheValueAverageWithoutZeroByDateRangeIndicatorAndMultipleOrgId(Date from, Date to, String indicator,
																							 List<String> orgIds) {
		Session session = sf.openSession();
		Query query = session.createQuery(
			"SELECT AVG(value) FROM CacheEntity " +
				"WHERE (value <> 0 AND value <> -1) " +
				"AND date BETWEEN :param1 AND :param2 " +
				"AND indicator = :param3 " +
				"AND org_id IN (:param4)");
		query.setParameter("param1", from);
		query.setParameter("param2", to);
		query.setParameter("param3", indicator);
		query.setParameterList("param4", orgIds);
		List resultList = query.getResultList();
		session.close();
		if (resultList.isEmpty() || resultList.get(0) == null) {
			return 0.0;
		}

		return (Double) resultList.get(0);
	}

	public Double getCacheValueAverageWithZeroByDateRangeIndicatorAndMultipleOrgId(Date from, Date to, String indicator,
																												 List<String> orgIds) {
		Session session = sf.openSession();
		Query query = session.createQuery(
			"SELECT AVG(value) FROM CacheEntity " +
				"WHERE value <> -1 " + // Exclude rows where value is -1
				"AND date BETWEEN :param1 AND :param2 " +
				"AND indicator = :param3 " +
				"AND org_id IN (:param4)");
		query.setParameter("param1", from);
		query.setParameter("param2", to);
		query.setParameter("param3", indicator);
		query.setParameterList("param4", orgIds);
		List resultList = query.getResultList();
		session.close();
		if (resultList.isEmpty() || resultList.get(0) == null) {
			return 0.0;
		}

		return (Double) resultList.get(0);
	}

	public List<Double> getCacheValueSumByDateRangeIndicatorsAndMultipleOrgIds(Date from, Date to,
			List<String> indicators, List<String> orgIds) {
		Session session = sf.openSession();
		Query query = session.createQuery(
				"SELECT SUM(value) FROM CacheEntity WHERE date BETWEEN :param1 AND :param2 AND indicator=:param3 AND org_id IN (:param4) GROUP BY org_id");
		query.setParameter("param1", from);
		query.setParameter("param2", to);
		query.setParameterList("param3", indicators);
		query.setParameterList("param4", orgIds);
		List<Double> resultList = query.getResultList();
		session.close();
		if (resultList.isEmpty()) {
			return Collections.emptyList();
		}
		return resultList;
	}

	public Double getCacheValueSumByDateRangeIndicatorAndOrgId(Date from, Date to, String indicator, String orgId) {
		Session session = sf.openSession();
		Query query = session.createQuery(
				"SELECT SUM(value) FROM CacheEntity WHERE date BETWEEN :param1 AND :param2 AND indicator=:param3 AND org_id=:param4");
		query.setParameter("param1", from);
		query.setParameter("param2", to);
		query.setParameter("param3", indicator);
		query.setParameter("param4", orgId);
		List resultList = query.getResultList();
		session.close();
		if (resultList.isEmpty() || resultList.get(0) == null) {
			return 0.0;
		}
		return (Double) resultList.get(0);
	}

	public Double getCacheValueAverageByDateRangeIndicatorAndOrgId(Date from, Date to, String indicator, String orgId) {
		Session session = sf.openSession();
		Query query = session.createQuery(
			"SELECT AVG(value) FROM CacheEntity WHERE date BETWEEN :param1 AND :param2 AND indicator=:param3 AND org_id=:param4");
		query.setParameter("param1", from);
		query.setParameter("param2", to);
		query.setParameter("param3", indicator);
		query.setParameter("param4", orgId);
		List resultList = query.getResultList();
		session.close();
		if (resultList.isEmpty() || resultList.get(0) == null) {
			return 0.0;
		}
		return (Double) resultList.get(0);
	}

	public List<Date> getDatesPresent(Date from, Date to, List<String> indicatorMD5List, List<String> facilityIds) {
		Session session = sf.openSession();
		Query query = session.createQuery(
				"SELECT DISTINCT(date) FROM CacheEntity WHERE date BETWEEN :param1 AND :param2 AND indicator IN (:param3) AND org_id IN (:param4)");
		query.setParameter("param1", from);
		query.setParameter("param2", to);
		query.setParameterList("param3", indicatorMD5List);
		query.setParameterList("param4", facilityIds);
		List resultList = query.getResultList();
		session.close();
		return resultList;
	}

	public List<String> getIndicatorsPresent(Date from, Date to) {
		Session session = sf.openSession();
		Query query = session
				.createQuery("SELECT DISTINCT(indicator) FROM CacheEntity WHERE date BETWEEN :param1 AND :param2");
		query.setParameter("param1", from);
		query.setParameter("param2", to);
		List<String> resultList = query.getResultList();
		session.close();
		return resultList;
	}

	public void deleteRecordsByTimePeriod(Date date) {
		Session session = sf.openSession();
		Transaction transaction = session.beginTransaction();
		Query query = session
				.createQuery("DELETE ComGenerator WHERE scheduledDate < :param1 AND communicationStatus=:param2");
		query.setParameter("param1", date);
		query.setParameter("param2", MessageStatus.SENT.name());
		query.executeUpdate();
		transaction.commit();
		session.close();
	}

	public List<MapCacheEntity> getMapCacheByDateOrgIdAndCategory(Date date, String orgId, String categoryId) {
		Session session = sf.openSession();
		Query query = session.createQuery("FROM MapCacheEntity WHERE date=:param1 AND org_id=:param2 AND category_id=:param3");
		query.setParameter("param1", date);
		query.setParameter("param2", orgId);
		query.setParameter("param3", categoryId);
		List<MapCacheEntity> resultList = query.getResultList();
		session.close();
		return resultList;
	}

	public void clearAsyncTable() {
		Session session = sf.openSession();
		Transaction transaction = session.beginTransaction();
		Query query = session.createQuery("DELETE ApiAsyncTaskEntity");
		query.executeUpdate();
		transaction.commit();
		session.close();
	}

	public void clearLastSyncStatusTable(Timestamp date) {
		Session session = sf.openSession();
		Transaction transaction = session.beginTransaction();
		Query query = session.createQuery("DELETE LastSyncEntity WHERE start_date_time < :param1");
		query.setParameter("param1", date);
		query.executeUpdate();
		transaction.commit();
		session.close();
	}

	public void deleteFromEncounterIdEntityByEncounterId(String encounterId) {
		Session session = sf.openSession();
		Transaction transaction = session.beginTransaction();
		Query query = session.createQuery("DELETE EncounterIdEntity WHERE encounterId = :param1");
		query.setParameter("param1", encounterId);
		query.executeUpdate();
		session.close();
	}

	public List<MapCacheEntity> getMapCacheByKeyIdList(List<String> mapDataCacheKeyList) {
		Session session = sf.openSession();
		Query query = session.createQuery("FROM MapCacheEntity WHERE id IN (:param1)");
		query.setParameter("param1", mapDataCacheKeyList);
		List<MapCacheEntity> resultList = query.getResultList();
		session.close();
		return resultList;
	}

	public List<MapCacheEntity> getMapCacheByKeyId(String mapDataCacheKey) {
		Session session = sf.openSession();
		Query query = session.createQuery("FROM MapCacheEntity WHERE id=:param1");
		query.setParameter("param1", mapDataCacheKey);
		List<MapCacheEntity> resultList = query.getResultList();
		session.close();
		return resultList;
	}

	public List<MapCacheEntity> getMapDataByOrgIdAndDateRange(List<String> allClinics, Date from, Date to) {
		Session session = sf.openSession();
		Query query = session.createQuery(
			"FROM MapCacheEntity WHERE date BETWEEN :param2 AND :param3 AND org_id IN (:param1) ORDER BY lat, lng");
		query.setParameter("param1", allClinics);
		query.setParameter("param2", from);
		query.setParameter("param3", to);
		List<MapCacheEntity> resultList = query.getResultList();
		session.close();
		return resultList;
	}

	public List<String> insertEmailSchedules(List<EmailScheduleEntity> emailSchedules) {
		StatelessSession session = sf.openStatelessSession();
		Transaction transaction = session.beginTransaction();
		List<String> skippedRecords = new ArrayList<>();
		try {
			for (EmailScheduleEntity emailSchedule : emailSchedules) {
				try {
					session.insert(emailSchedule);
				} catch (ConstraintViolationException e) {
					skippedRecords.add("Duplicate entry skipped for email schedule: " + emailSchedule.getRecipientEmail());
					logger.warn("Duplicate entry skipped for email schedule: " + emailSchedule.getRecipientEmail());
				}
			}
			transaction.commit();
		} catch (Exception e) {
			logger.warn(ExceptionUtils.getStackTrace(e));
			transaction.rollback();
			throw e;
		} finally {
			session.close();
		}
		return skippedRecords;
	}

	public void updateEmailSchedules(List<EmailScheduleEntity> emailSchedules) {
		StatelessSession session = sf.openStatelessSession();
		Transaction transaction = session.beginTransaction();
		try {
			for (EmailScheduleEntity emailSchedule : emailSchedules) {
				session.update(emailSchedule);
			}
			transaction.commit();
		} catch (Exception e) {
			logger.warn(ExceptionUtils.getStackTrace(e));
			transaction.rollback();
		} finally {
			session.close();
		}
	}

	public void delete(EmailScheduleEntity emailSchedule) {
		StatelessSession session = sf.openStatelessSession();
		Transaction transaction = session.beginTransaction();
		try {
			session.delete(emailSchedule);
			transaction.commit();
		} catch (Exception e) {
			logger.warn(ExceptionUtils.getStackTrace(e));
			transaction.rollback();
			throw e;
		} finally {
			session.close();
		}
	}

	public void deleteByRecipientEmail(String recipientEmail) {
		StatelessSession session = sf.openStatelessSession();
		Transaction transaction = session.beginTransaction();
		try {
			Query<?> query = session.createQuery("DELETE FROM EmailScheduleEntity WHERE recipientEmail = :param1");
			query.setParameter("param1", recipientEmail);
			query.executeUpdate();
			transaction.commit();
		} catch (Exception e) {
			logger.warn("Failed to delete email schedule for recipient {}: {}", recipientEmail, ExceptionUtils.getStackTrace(e));
			transaction.rollback();
			throw e;
		} finally {
			session.close();
		}
	}

	public EmailScheduleEntity getEmailScheduleById(Integer id) {
		Session session = sf.openSession();
		try {
			Query<EmailScheduleEntity> query = session.createQuery(
				"FROM EmailScheduleEntity WHERE id = :param1",
				EmailScheduleEntity.class);
			query.setParameter("param1", id);
			return query.getSingleResult();
		} catch (Exception e) {
			logger.warn(ExceptionUtils.getStackTrace(e));
			return null;
		} finally {
			session.close();
		}
	}

	public List<EmailScheduleEntity> getEmailSchedulesByOrgId(String orgId) {
		Session session = sf.openSession();
		try {
			String hql = orgId == null
				? "FROM EmailScheduleEntity"
				: "FROM EmailScheduleEntity WHERE orgId = :param1";
			Query<EmailScheduleEntity> query = session.createQuery(hql, EmailScheduleEntity.class);
			if (orgId != null) {
				query.setParameter("param1", orgId);
			}
			return query.getResultList();
		} catch (Exception e) {
			logger.warn(ExceptionUtils.getStackTrace(e));
			return Collections.emptyList();
		} finally {
			session.close();
		}
	}

	public EmailScheduleEntity getEmailScheduleByRecipientEmail(String recipientEmail) {
		Session session = sf.openSession();
		try {
			Query<EmailScheduleEntity> query = session.createQuery(
				"FROM EmailScheduleEntity WHERE recipientEmail = :param1",
				EmailScheduleEntity.class);
			query.setParameter("param1", recipientEmail);
			return query.getSingleResult();
		} catch (Exception e) {
			logger.warn(ExceptionUtils.getStackTrace(e));
			return null;
		} finally {
			session.close();
		}
	}

	public List<EmailScheduleEntity> getAllEmailSchedules() {
		Session session = sf.openSession();
		try {
			Query<EmailScheduleEntity> query = session.createQuery("FROM EmailScheduleEntity", EmailScheduleEntity.class);
			List<EmailScheduleEntity> resultList = query.getResultList();
			return resultList;
		} catch (Exception e) {
			logger.warn("Failed to fetch email schedules: {}", ExceptionUtils.getStackTrace(e));
			return Collections.emptyList();
		} finally {
			session.close();
		}
	}

	// Get email schedules filtered by admin organization
	public List<EmailScheduleEntity> getEmailSchedulesByAdminOrg(String adminOrg) {
		Session session = sf.openSession();
		try {
			Query<EmailScheduleEntity> query = session.createQuery(
				"FROM EmailScheduleEntity WHERE adminOrg = :adminOrg",
				EmailScheduleEntity.class
			);
			query.setParameter("adminOrg", adminOrg);
			List<EmailScheduleEntity> resultList = query.getResultList();
			return resultList;
		} catch (Exception e) {
			logger.warn("Failed to fetch email schedules for adminOrg {}: {}", adminOrg, ExceptionUtils.getStackTrace(e));
			return Collections.emptyList();
		} finally {
			session.close();
		}
	}

}
