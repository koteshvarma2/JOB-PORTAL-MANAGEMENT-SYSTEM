package com.example.jobportal.repository;

import com.example.jobportal.entity.Application;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    /**
     * Fetch applications for a given applicant, eagerly loading job and
     * job.postedBy so templates can access app.job.title, app.job.location,
     * app.job.postedBy.name without a LazyInitializationException.
     */
    @Query("SELECT a FROM Application a " +
           "JOIN FETCH a.job j " +
           "JOIN FETCH j.postedBy " +
           "WHERE a.applicant.id = :applicantId")
    List<Application> findByApplicantId(@Param("applicantId") Long applicantId);

    /**
     * Fetch applications for a given job, eagerly loading the applicant (User)
     * so templates can access app.applicant.name, email, skills, resumeUrl, id.
     */
    @Query("SELECT a FROM Application a " +
           "JOIN FETCH a.applicant " +
           "WHERE a.job.id = :jobId")
    List<Application> findByJobId(@Param("jobId") Long jobId);

    boolean existsByApplicantIdAndJobId(Long applicantId, Long jobId);

    /**
     * findAll override that eagerly loads both applicant and job so the
     * admin dashboard can safely render recent applications.
     * Sort parameter is accepted but ignored — JPQL JOIN FETCH does not
     * support database-level ordering; sort in the service/controller if needed.
     */
    @Override
    @Query("SELECT a FROM Application a JOIN FETCH a.applicant JOIN FETCH a.job")
    @NonNull
    List<Application> findAll(@NonNull Sort sort);
}
