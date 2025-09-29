package com.kica.ess.batch.repository;

import com.kica.ess.batch.entity.TargetServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TargetServerRepository extends JpaRepository<TargetServer, Long> {

  List<TargetServer> findByEnabledTrue();

  List<TargetServer> findByEnvironment(String environment);

  List<TargetServer> findByEnabledTrueAndEnvironment(String environment);

  @Query("SELECT t FROM TargetServer t WHERE t.enabled = true AND " +
      "(t.environment = :environment OR t.environment IS NULL)")
  List<TargetServer> findActiveServersByEnvironment(@Param("environment") String environment);

  @Query("SELECT COUNT(t) FROM TargetServer t WHERE t.enabled = true")
  long countActiveServers();

  @Query("SELECT COUNT(t) FROM TargetServer t WHERE t.enabled = true AND t.environment = :environment")
  long countActiveServersByEnvironment(@Param("environment") String environment);

  List<TargetServer> findByNameContainingIgnoreCase(String name);

  List<TargetServer> findByUrlContainingIgnoreCase(String url);

  boolean existsByNameAndEnvironment(String name, String environment);
}