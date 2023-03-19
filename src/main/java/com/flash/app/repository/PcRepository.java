package com.flash.app.repository;

import com.flash.app.domain.Pc;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the Pc entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PcRepository extends MongoRepository<Pc, String> {}
