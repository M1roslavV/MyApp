package com.mypropertyapp.propertyHistory;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "typeChanges")
public class TypeChanges {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;

    @OneToMany(mappedBy = "typeChanges", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<PropertyHistory> propertyHistories;
}
