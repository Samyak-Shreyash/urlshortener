package com.systemdesign.urlshortener.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.systemdesign.urlshortener.model.UrlMapping;

@Repository
public interface UrlRepository extends JpaRepository<UrlMapping, Long> {
    
    // Find by short code (for redirects)
    Optional<UrlMapping> findByShortCode(String shortCode);
    
    // Find by URL hash (for duplicate detection)
    Optional<UrlMapping> findByLongUrlHash(String longUrlHash);
    
    // Check if short code exists
    boolean existsByShortCode(String shortCode);
    
    // Custom query for efficient lookup
    @Query("SELECT u.shortCode FROM UrlMapping u WHERE u.longUrlHash = :hash")
    Optional<String> findShortCodeByLongUrlHash(@Param("hash") String longUrlHash);
}