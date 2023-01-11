package ca.uhn.fhir.jpa.starter.service;

import java.io.File;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import ca.uhn.fhir.jpa.starter.model.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import ca.uhn.fhir.jpa.starter.model.ComGenerator.MessageStatus;

public class NotificationDataSource {
		
	Configuration conf;
	SessionFactory sf;
	Session session;
	Transaction tx;
	private static NotificationDataSource notificationDataSource;
	private Class<?> aClass;

	private NotificationDataSource() {}
	
	public static NotificationDataSource getInstance() {
		if(notificationDataSource == null) {
			notificationDataSource = new NotificationDataSource();
		}
		return notificationDataSource;
	}
	
	public void configure(String filePath) {
		conf = new Configuration().configure(new File(filePath))
			.addAnnotatedClass(ComGenerator.class)
			.addAnnotatedClass(CacheEntity.class)
			.addAnnotatedClass(ApiAsyncTaskEntity.class)
			.addAnnotatedClass(ParentEncounterMapHelper.class);
		sf = conf.buildSessionFactory();
	}

	public void insert(Object object) {
		session = sf.openSession();
		tx = session.beginTransaction();
		session.save(object);
		tx.commit();
		session.close();
	}

	public void update(Object object) {
		session = sf.openSession();
		tx = session.beginTransaction();
		session.update(object);
		tx.commit();
		session.close();
	}

	public void delete(Object object) {
		session = sf.openSession();
		tx = session.beginTransaction();
		session.delete(object);
		tx.commit();
		session.close();
	}
	
	public List<ComGenerator> fetchRecordsByScheduledDateAndStatus(Date date, MessageStatus status) {
		session = sf.openSession();
		Query query = session.createQuery("FROM ComGenerator WHERE communicationStatus=:param1 AND scheduledDate=:param2");
		query.setParameter("param1", status.name());
		query.setParameter("param2", date);
		List<ComGenerator> resultList = query.getResultList();
		session.close();
		return resultList;
	}

	public List<ParentEncounterMapHelper> fetchAllFromPatientEncounterMapper() {
		session = sf.openSession();
		Query query = session.createQuery("From ParentEncounterMapHelper");
		List<ParentEncounterMapHelper> encounterIds = query.getResultList();
		session.close();
		return encounterIds;
	}

	public ArrayList<ApiAsyncTaskEntity> fetchStatus(String uuid) {
		session = sf.openSession();
		Query query = session.createQuery("FROM ApiAsyncTaskEntity WHERE uuid=:param1");
		query.setParameter("param1", uuid);
		ArrayList<ApiAsyncTaskEntity> resultList = (ArrayList<ApiAsyncTaskEntity>) query.getResultList();
		session.close();
		return resultList;
	}

	public List<CacheEntity> getCacheByDateIndicatorAndOrgId(Date date, String indicator, String orgId) {
		session = sf.openSession();
		Query query = session.createQuery("FROM CacheEntity WHERE date=:param1 AND indicator=:param2 AND org_id=:param3");
		query.setParameter("param1", date);
		query.setParameter("param2", indicator);
		query.setParameter("param3", orgId);
		List<CacheEntity> resultList = query.getResultList();
		session.close();
		return resultList;
	}

	public Double getCacheValueSumByDateRangeIndicatorAndMultipleOrgId(Date from, Date to, String indicator, List<String> orgIds) {
		session = sf.openSession();
		Query query = session.createQuery("SELECT SUM(value) FROM CacheEntity WHERE date BETWEEN :param1 AND :param2 AND indicator=:param3 AND org_id IN (:param4)");
		query.setParameter("param1", from);
		query.setParameter("param2", to);
		query.setParameter("param3", indicator);
		query.setParameterList("param4", orgIds);
		List resultList = query.getResultList();
		session.close();
		if(resultList.isEmpty() || resultList.get(0) == null) {
			return 0.0;
		}
		return (Double) resultList.get(0);
	}

	public List<Date> getDatesPresent(Date from, Date to, List<String> indicatorMD5List, List<String> facilityIds) {
		session = sf.openSession();
		Query query = session.createQuery("SELECT DISTINCT(date) FROM CacheEntity WHERE date BETWEEN :param1 AND :param2 AND indicator IN (:param3) AND org_id IN (:param4)");
		query.setParameter("param1",from);
		query.setParameter("param2",to);
		query.setParameterList("param3", indicatorMD5List);
		query.setParameterList("param4", facilityIds);
		List resultList = query.getResultList();
		session.close();

		return resultList;
	}

	public List<String> getIndicatorsPresent(Date from, Date to) {
		session = sf.openSession();
		Query query = session.createQuery("SELECT DISTINCT(indicator) FROM CacheEntity WHERE date BETWEEN :param1 AND :param2");
		query.setParameter("param1",from);
		query.setParameter("param2",to);
		List resultList = query.getResultList();
		session.close();
		return resultList;
	}
	
	
	public void deleteRecordsByTimePeriod(Date date) {
		session = sf.openSession();
		tx = session.beginTransaction();
		Query query = session.createQuery("DELETE ComGenerator WHERE scheduledDate < :param1 AND communicationStatus=:param2");
		query.setParameter("param1", date);
		query.setParameter("param2", MessageStatus.SENT.name());
		query.executeUpdate();
		tx.commit();
		session.close();
	}

	public void clearAsyncTable(){
		session = sf.openSession();
		tx = session.beginTransaction();
		Query query = session.createQuery("DELETE ApiAsyncTaskEntity");
		query.executeUpdate();
		tx.commit();
		session.close();
	}

	public void deleteFromParentEncounterMapHelperByEncounterId(String encounterId) {
		session = sf.openSession();
		tx = session.beginTransaction();
		Query query = session.createQuery("DELETE ParentEncounterMapHelper WHERE encounterId = :param1");
		query.setParameter("param1", encounterId);
		query.executeUpdate();
	}
	
}