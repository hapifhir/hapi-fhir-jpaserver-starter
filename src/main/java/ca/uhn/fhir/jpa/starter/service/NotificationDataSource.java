package ca.uhn.fhir.jpa.starter.service;

import java.io.File;
import java.sql.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import org.springframework.context.annotation.Import;

import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.model.ComGenerator;
import ca.uhn.fhir.jpa.starter.model.ComGenerator.MessageStatus;

public class NotificationDataSource {
		
	Configuration conf;
	SessionFactory sf;
	Session session;
	Transaction tx;
	private static NotificationDataSource notificationDataSource;
	 
	private NotificationDataSource() {}
	
	public static NotificationDataSource getInstance() {
		if(notificationDataSource == null) {
			notificationDataSource = new NotificationDataSource();
		}
		return notificationDataSource;
	}
	
	public void configure(String filePath) {
		conf = new Configuration().configure(new File(filePath)).addAnnotatedClass(ComGenerator.class);
		sf = conf.buildSessionFactory();
	}
	
	public void insert(ComGenerator comGenerator) {
		session = sf.openSession();
		tx = session.beginTransaction();
		session.save(comGenerator);
		tx.commit();
		session.close();
		
	}
	
	public void update(ComGenerator comGenerator) {
		session = sf.openSession();
		tx = session.beginTransaction();
		session.update(comGenerator);
		tx.commit();
		session.close();
	}
	
	public void delete(ComGenerator comGenerator) {
		session = sf.openSession();
		tx = session.beginTransaction();
		session.delete(comGenerator);
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
	
	
	public void deleteRecordsByTimePeriod(Date date) {
		session = sf.openSession();
		tx = session.beginTransaction();
		Query query = session.createQuery("DELETE ComGenerator WHERE scheduledDate < :param1");
		query.setParameter("param1", date);
		query.executeUpdate();
		tx.commit();
		session.close();
	}
	
}
