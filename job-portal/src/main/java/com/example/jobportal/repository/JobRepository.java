package com.example.jobportal.repository;

import com.example.jobportal.entity.Job;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    /**
     * Override findAll(Sort) to eagerly load postedBy so templates can safely
     * access job.postedBy.name without a LazyInitializationException.
     * Sort parameter is accepted but not applied — JOIN FETCH is incompatible
     * with JPQL-level ordering when combined with pagination.
     */
    @Override
    @Query("SELECT j FROM Job j JOIN FETCH j.postedBy")
    @NonNull
    List<Job> findAll(@NonNull Sort sort);

    @Query("SELECT j FROM Job j JOIN FETCH j.postedBy WHERE j.id = :id")
    java.util.Optional<Job> findByIdWithPostedBy(@Param("id") Long id);

    /**
     * Search by category, title, or location with postedBy eagerly loaded.
     */
    @Query("SELECT DISTINCT j FROM Job j JOIN FETCH j.postedBy WHERE " +
           "LOWER(j.category) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.title)    LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.location) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Job> searchByKeyword(@Param("keyword") String keyword);

    /**
     * Load jobs by employer with postedBy eagerly so templates
     * can access job.postedBy without a LazyInitializationException.
     */
    @Query("SELECT j FROM Job j JOIN FETCH j.postedBy WHERE j.postedBy.id = :userId")
    List<Job> findByPostedById(@Param("userId") Long userId);

    Job findByTitle(String title);
}
