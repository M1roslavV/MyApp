package com.mypropertyapp.propertyHistory;

import com.mypropertyapp.property.Property;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "propertyHistory")
public class PropertyHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String dateTime;
    private String changer;
    private String changedFrom;
    private String changedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "typeChange_id")
    private TypeChanges typeChanges;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id")
    private Property property;

}
