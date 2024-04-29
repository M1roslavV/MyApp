package com.mypropertyapp.property;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PropertyDto {
    private Long id;
    private String inventoryNumber;
    private String name;
    private String responsiblePeople;
    private double price;
    private String category;
    private String location;
}
