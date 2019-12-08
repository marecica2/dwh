package org.bmsource.dwh.domain.repository;

import org.bmsource.dwh.domain.model.RawFact;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

@Component
public interface FactRawRepository extends PagingAndSortingRepository<RawFact, BigInteger> {

}