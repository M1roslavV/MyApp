package com.mypropertyapp.propertyHistory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropertyHistoryRepository extends JpaRepository<PropertyHistory, Long> {
    Page<PropertyHistory> findByTypeChanges_NameAndPropertyId(String filter, Long id, Pageable pageable);

    Page<PropertyHistory> findByPropertyId(Long id, Pageable pageable);
}
