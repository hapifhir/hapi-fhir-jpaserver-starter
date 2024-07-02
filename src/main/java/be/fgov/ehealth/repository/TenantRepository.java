package be.fgov.ehealth.repository;

import be.fgov.ehealth.entities.Tenants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TenantRepository extends JpaRepository<Tenants, Integer> {
	Tenants getTenantByApiKey(String s);
}
