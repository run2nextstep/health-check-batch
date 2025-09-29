package com.kica.ess.batch.repository;

import com.kica.ess.batch.entity.ExecutionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExecutionLogRepository extends JpaRepository<ExecutionLog, Long> {

  Page<ExecutionLog> findByOrderByExecutionTimeDesc(Pageable pageable);

  Page<ExecutionLog> findByTargetServerIdOrderByExecutionTimeDesc(Long targetServerId, Pageable pageable);

  Page<ExecutionLog> findByServerNameContainingIgnoreCaseOrderByExecutionTimeDesc(String serverName, Pageable pageable);

  Page<ExecutionLog> findBySuccessOrderByExecutionTimeDesc(Boolean success, Pageable pageable);

  Page<ExecutionLog> findByExecutionTimeBetweenOrderByExecutionTimeDesc(
      LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

  @Query("SELECT e FROM ExecutionLog e WHERE e.executionTime >= :since ORDER BY e.executionTime DESC")
  List<ExecutionLog> findRecentLogs(@Param("since") LocalDateTime since);

  @Query("SELECT e FROM ExecutionLog e WHERE e.success = false AND e.executionTime >= :since ORDER BY e.executionTime DESC")
  List<ExecutionLog> findRecentFailures(@Param("since") LocalDateTime since);

  @Query("SELECT e FROM ExecutionLog e WHERE e.elapsedTimeMs > :thresholdMs AND e.executionTime >= :since ORDER BY e.executionTime DESC")
  List<ExecutionLog> findSlowResponses(@Param("thresholdMs") Long thresholdMs, @Param("since") LocalDateTime since);

  @Query("SELECT COUNT(e) FROM ExecutionLog e WHERE e.success = true AND e.executionTime >= :since")
  long countSuccessfulExecutions(@Param("since") LocalDateTime since);

  @Query("SELECT COUNT(e) FROM ExecutionLog e WHERE e.success = false AND e.executionTime >= :since")
  long countFailedExecutions(@Param("since") LocalDateTime since);

  @Query("SELECT AVG(e.elapsedTimeMs) FROM ExecutionLog e WHERE e.success = true AND e.executionTime >= :since")
  Double getAverageResponseTime(@Param("since") LocalDateTime since);

  @Query("SELECT e.targetServerId, COUNT(e) as execCount, " +
      "AVG(e.elapsedTimeMs) as avgTime, " +
      "SUM(CASE WHEN e.success = true THEN 1 ELSE 0 END) as successCount " +
      "FROM ExecutionLog e WHERE e.executionTime >= :since " +
      "GROUP BY e.targetServerId")
  List<Object[]> getExecutionStatsByServer(@Param("since") LocalDateTime since);

  @Query("SELECT e FROM ExecutionLog e WHERE e.batchExecutionId = :batchId ORDER BY e.executionTime")
  List<ExecutionLog> findByBatchExecutionId(@Param("batchId") String batchExecutionId);

  void deleteByExecutionTimeBefore(LocalDateTime cutoffTime);
}