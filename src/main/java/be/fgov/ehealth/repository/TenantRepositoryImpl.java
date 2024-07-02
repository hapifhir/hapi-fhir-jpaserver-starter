package be.fgov.ehealth.repository;

import be.fgov.ehealth.entities.Tenants;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;


@Component
public class TenantRepositoryImpl implements TenantRepository{
	@PersistenceContext(unitName = "testserverDs")
	EntityManager em;

	public Tenants getTenantByApiKey(String apiKey){
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Tenants> criteriaQuery = criteriaBuilder.createQuery(Tenants.class);
		Root<Tenants> model = criteriaQuery.from(Tenants.class);
		criteriaQuery.where(criteriaBuilder.equal(model.get("tenant_api_key"), apiKey));
		Tenants tenant;
		try {
			 tenant = em.createQuery(criteriaQuery).getSingleResult();
		} catch(RuntimeException e) {
			tenant = null;
		}
		return tenant;
	}

	@Override
	public void flush() {

	}

	@Override
	public <S extends Tenants> S saveAndFlush(S entity) {
		return null;
	}

	@Override
	public <S extends Tenants> List<S> saveAllAndFlush(Iterable<S> entities) {
		return null;
	}

	@Override
	public void deleteAllInBatch(Iterable<Tenants> entities) {

	}

	@Override
	public void deleteAllByIdInBatch(Iterable<Integer> integers) {

	}

	@Override
	public void deleteAllInBatch() {

	}

	@Override
	public Tenants getOne(Integer integer) {
		return null;
	}

	@Override
	public Tenants getById(Integer integer) {
		return null;
	}

	@Override
	public Tenants getReferenceById(Integer integer) {
		return null;
	}

	@Override
	public <S extends Tenants> Optional<S> findOne(Example<S> example) {
		return Optional.empty();
	}

	@Override
	public <S extends Tenants> List<S> findAll(Example<S> example) {
		return null;
	}

	@Override
	public <S extends Tenants> List<S> findAll(Example<S> example, Sort sort) {
		return null;
	}

	@Override
	public <S extends Tenants> Page<S> findAll(Example<S> example, Pageable pageable) {
		return null;
	}

	@Override
	public <S extends Tenants> long count(Example<S> example) {
		return 0;
	}

	@Override
	public <S extends Tenants> boolean exists(Example<S> example) {
		return false;
	}

	@Override
	public <S extends Tenants, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
		return null;
	}

	@Override
	public <S extends Tenants> S save(S entity) {
		return null;
	}

	@Override
	public <S extends Tenants> List<S> saveAll(Iterable<S> entities) {
		return null;
	}

	@Override
	public Optional<Tenants> findById(Integer integer) {
		return Optional.empty();
	}

	@Override
	public boolean existsById(Integer integer) {
		return false;
	}

	@Override
	public List<Tenants> findAll() {
		return null;
	}

	@Override
	public List<Tenants> findAllById(Iterable<Integer> integers) {
		return null;
	}

	@Override
	public long count() {
		return 0;
	}

	@Override
	public void deleteById(Integer integer) {

	}

	@Override
	public void delete(Tenants entity) {

	}

	@Override
	public void deleteAllById(Iterable<? extends Integer> integers) {

	}

	@Override
	public void deleteAll(Iterable<? extends Tenants> entities) {

	}

	@Override
	public void deleteAll() {

	}

	@Override
	public List<Tenants> findAll(Sort sort) {
		return null;
	}

	@Override
	public Page<Tenants> findAll(Pageable pageable) {
		return null;
	}
}
