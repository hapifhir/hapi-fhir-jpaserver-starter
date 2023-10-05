package ca.uhn.fhir.jpa.starter.model;

import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.sql.Date;

@Entity
@Table(name = "map_cache", indexes = {
	@Index(columnList = "org_id, date, category_id", name = "org_date_cat")
})
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)

public class MapCacheEntity {
	@Id
	@Column(name = "id", nullable = false)
	private String id;

	@Column(name = "org_id", nullable = false)
	private String orgId;

	@Column(name = "date", nullable = false)
	private Date date;

	@Column(name = "lat", nullable = false)
	private Double lat;

	@Column(name = "lng", nullable = false)
	private Double lng;

	@Column(name = "weight", nullable = false)
	private int weight;

	@Column(name = "category_id", nullable = false)
	private String categoryId;

	@Column(name = "lastUpdated", nullable = true)
	private Date lastUpdated;

	@Column(name = "plusCode", nullable = true)
	private String plusCode;


	public MapCacheEntity() {
	}

	public MapCacheEntity(String id,String orgId, Date date, String categoryId, Double lat, Double lng, String plusCode, int weight, Date lastUpdated) {
		this.id = id;
		this.orgId = orgId;
		this.date = date;
		this.categoryId = categoryId;
		this.lat = lat;
		this.lng = lng;
		this.plusCode = plusCode;
		this.weight = weight;
		this.lastUpdated = lastUpdated;
	}

	public String getId() {
		return id;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Double getLat() {
		return lat;
	}

	public void setLat(Double lat) {
		this.lat = lat;
	}

	public Double getLng() {
		return lng;
	}

	public void setLng(Double lng) {
		this.lng = lng;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public String getPlusCode() { return plusCode;}

	public void setPlusCode(String plusCode) { this.plusCode = plusCode;}

}