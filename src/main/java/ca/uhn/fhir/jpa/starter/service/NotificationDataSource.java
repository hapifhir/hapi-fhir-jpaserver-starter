package ca.uhn.fhir.jpa.starter.service;

import java.io.File;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.uhn.fhir.jpa.starter.model.*;
import com.iprd.fhir.utils.PatientIdentifierStatus;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import ca.uhn.fhir.jpa.starter.model.ComGenerator.MessageStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;

public class NotificationDataSource {

	private static final Logger logger = LoggerFactory.getLogger(ResourceMapperService.class);

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
				.addAnnotatedClass(CacheEntity.class).addAnnotatedClass(ApiAsyncTaskEntity.class)
				.addAnnotatedClass(EncounterIdEntity.class).addAnnotatedClass(PatientIdentifierEntity.class);
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
	
	public void insertObjects(ArrayList<CacheEntity> cacheEntities) {
		StatelessSession session = sf.openStatelessSession();
		Transaction transaction = session.beginTransaction();
		try {

			for (int i = 0; i < cacheEntities.size(); i++) {
				session.insert(cacheEntities.get(i));
			}
			transaction.commit();
		} catch (Exception e) {
			logger.warn(ExceptionUtils.getStackTrace(e));
			transaction.rollback();
		} finally {
			session.close();
		}
	}
	
	public void updateObjects(ArrayList<CacheEntity> cacheEntities) {
		StatelessSession session = sf.openStatelessSession();
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

	public List<EncounterIdEntity> fetchAllFromEncounterIdEntity() {
		Session session = sf.openSession();
		Query query = session.createQuery("From EncounterIdEntity");
		List<EncounterIdEntity> encounterIds = query.getResultList();
		session.close();
		return encounterIds;
	}

	public ArrayList<ApiAsyncTaskEntity> fetchStatus(String uuid) {
		Session session = sf.openSession();
		Query query = session.createQuery("FROM ApiAsyncTaskEntity WHERE uuid=:param1");
		query.setParameter("param1", uuid);
		ArrayList<ApiAsyncTaskEntity> resultList = (ArrayList<ApiAsyncTaskEntity>) query.getResultList();
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

	public Double getCacheValueSumByDateRangeIndicatorAndMultipleOrgId(Date from, Date to, String indicator,
			List<String> orgIds) {
		Session session = sf.openSession();
		Query query = session.createQuery(
				"SELECT SUM(value) FROM CacheEntity WHERE date BETWEEN :param1 AND :param2 AND indicator=:param3 AND org_id IN (:param4)");
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
		List resultList = query.getResultList();
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

	public void clearAsyncTable() {
		Session session = sf.openSession();
		Transaction transaction = session.beginTransaction();
		Query query = session.createQuery("DELETE ApiAsyncTaskEntity");
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

}
