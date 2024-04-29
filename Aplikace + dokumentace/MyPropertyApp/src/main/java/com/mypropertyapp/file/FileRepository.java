package com.mypropertyapp.file;

import com.mypropertyapp.property.Property;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<File, Long> {
    File findByName(String fileName);

    List<File> findByPropertyIdAndTypeName(Long id, String type);
    List<File> findByActionAndPropertyIdAndTypeName(String action, Long id, String type);

    List<File> findByProperty(Property property);
}

