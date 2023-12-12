package com.example.stock.repository;

import com.example.stock.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * 편의성을 위해서 Stock Entity를 사용할 건데 실무에서는 별도의 JDBC를 사용해야 한다.
 */
public interface LockRepository extends JpaRepository<Stock, Long> {
    @Query(value = "select get_lock(:key, 3000)", nativeQuery = true)
    void getLock(String key);

    @Query(value = "select release_lock(:key)", nativeQuery = true)
    void releaseLock(String key);
}
