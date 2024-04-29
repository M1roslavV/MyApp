package com.mypropertyapp.property;



import com.mypropertyapp.category.Category;
import com.mypropertyapp.category.CategoryRepository;
import com.mypropertyapp.company.Company;
import com.mypropertyapp.file.File;
import com.mypropertyapp.file.FileRepository;
import com.mypropertyapp.file.FileService;
import com.mypropertyapp.location.Location;
import com.mypropertyapp.location.LocationRepository;
import com.mypropertyapp.propertyHistory.PropertyHistory;
import com.mypropertyapp.propertyHistory.PropertyHistoryRepository;
import com.mypropertyapp.propertyHistory.TypeChanges;
import com.mypropertyapp.propertyHistory.TypeChangesRepository;
import com.mypropertyapp.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@AllArgsConstructor
@Service
public class PropertyService {
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private  final CategoryRepository categoryRepository;
    private final FileService fileService;
    private final TypeChangesRepository typeChangesRepository;
    private final PropertyHistoryRepository propertyHistoryRepository;
    private final FileRepository fileRepository;
    public Page<Property> PageAll(Integer pageNo, String email, String sortField, String sortDir, Integer size){
        Company company = userRepository.findByEmail(email).get().getCompany();
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortField);
        PageRequest pageable = PageRequest.of(pageNo -1, size, sort);
        return propertyRepository.findAllByLocation_Company_Id(company.getId(), pageable);
    }

    public Page<PropertyHistory> PageAllHistories(Integer pageNo, String filter, Long id, Integer size){
        PageRequest pageable = PageRequest.of(pageNo -1, size);
        if (Objects.equals(filter, "all"))
            return propertyHistoryRepository.findByPropertyId(id, pageable);
        else
            return propertyHistoryRepository.findByTypeChanges_NameAndPropertyId(filter, id, pageable);
    }

    public Property saveProperty(PropertyDto propertyDto, String email){
        Company company = userRepository.findByEmail(email).get().getCompany();
        String[] changedFrom = new String[6];

        Location location = locationRepository.findByNameAndCompany(propertyDto.getLocation(), company);
        if(location==null){
            location = new Location();
            location.setName(propertyDto.getLocation());
            location.setCompany(company);
            locationRepository.save(location);
        }

        Category category = categoryRepository.findByName(propertyDto.getCategory());
        if(category == null){
            category = new Category();
            category.setName(propertyDto.getCategory());
            categoryRepository.save(category);
        }

        Property property;
        if (propertyDto.getId() != null) {
            property = propertyRepository.findById(propertyDto.getId()).orElse(new Property());
            changedFrom[0] = property.getInventoryNumber();
            changedFrom[1] = property.getName();
            changedFrom[2] = property.getCategory().getName();
            changedFrom[3] = property.getLocation().getName();
            changedFrom[4] = property.getPrice() + "";
            changedFrom[5] = property.getResponsiblePeople();

        } else {
            property = new Property();
        }

        String[] change = new String[6];

        property.setInventoryNumber(propertyDto.getInventoryNumber());
        change[0] = propertyDto.getInventoryNumber();
        property.setName(propertyDto.getName());
        change[1] = propertyDto.getName();
        property.setCategory(categoryRepository.findByName(propertyDto.getCategory()));
        change[2] = categoryRepository.findByName(propertyDto.getCategory()).getName();
        property.setLocation(locationRepository.findByNameAndCompany(propertyDto.getLocation(), company));
        change[3] = locationRepository.findByNameAndCompany(propertyDto.getLocation(), company).getName();
        property.setPrice(propertyDto.getPrice());
        change[4] = propertyDto.getPrice() + "";
        if(Objects.equals(propertyDto.getResponsiblePeople(), "") || propertyDto.getResponsiblePeople() == null) {
            String responsibleUser = userRepository.findByEmail(email).get().getFirstName() + " " + userRepository.findByEmail(email).get().getLastName();
            property.setResponsiblePeople(responsibleUser);
            change[5] = responsibleUser;
        }
        else{
            property.setResponsiblePeople(propertyDto.getResponsiblePeople());
            change[5] = propertyDto.getResponsiblePeople();
        }
        propertyRepository.save(property);


        for (int i = 0;i<=5;i++){
            if(!Objects.equals(change[i], changedFrom[i]) && propertyDto.getId() != null){
                PropertyHistory propertyHistory = new PropertyHistory();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yyyy");
                LocalDateTime now = LocalDateTime.now();
                String date = now.format(formatter);
                propertyHistory.setDateTime(date);
                propertyHistory.setChanger(userRepository.findByEmail(email).get().getFirstName() + " " + userRepository.findByEmail(email).get().getLastName());
                propertyHistory.setTypeChanges(typeChangesRepository.findById(i+1).get());
                propertyHistory.setProperty(property);
                propertyHistory.setChangedFrom(changedFrom[i]);
                propertyHistory.setChangedTo(change[i]);
                propertyHistoryRepository.save(propertyHistory);
            }
        }
        return property;
    }

    public Map<String, Integer> calculatePageRange(Page<?> page, int pageNo, int maxLinks) {
        Map<String, Integer> pageRange = new HashMap<>();

        if (page.getTotalPages() > 0) {
            int startPage = Math.max(1, pageNo - maxLinks / 2);
            int endPage = Math.min(startPage + maxLinks - 1, page.getTotalPages());

            if (endPage - startPage < maxLinks - 1) {
                startPage = Math.max(1, endPage - maxLinks + 1);
            }

            pageRange.put("startPage", startPage);
            pageRange.put("endPage", endPage);
        } else {
            pageRange.put("startPage", 0);
            pageRange.put("endPage", 0);
        }

        return pageRange;
    }

    public List<Category> categories(Principal principal){
        return categoryRepository.findByPropertiesLocationCompanyId(userRepository.findByEmail(principal.getName()).get().getCompany().getId()).stream().distinct().collect(Collectors.toList());
    }

    public List<Location> locations(Principal principal){
        return locationRepository.findByCompanyId(userRepository.findByEmail(principal.getName()).get().getCompany().getId()).stream().distinct().collect(Collectors.toList());
    }

    public void removePropertyById(Long id) throws IOException {
        Optional<Property> propertyOptional = propertyRepository.findById(id);
        if (propertyOptional.isPresent()) {
            Property property = propertyOptional.get();
            for (File file : property.getFiles()) {
                file.setProperty(null);
                fileRepository.save(file);
            }
            propertyHistoryRepository.deleteAll(property.getPropertyHistories());
            propertyRepository.delete(property);
        }
    }

    public PropertyDto updatePropertyById(Long id) {
        Property property = propertyRepository.findById(id).get();
        PropertyDto propertyDto = new PropertyDto();
        propertyDto.setId(property.getId());
        propertyDto.setName(property.getName());
        propertyDto.setPrice(property.getPrice());
        propertyDto.setCategory(property.getCategory().getName());
        propertyDto.setLocation(property.getLocation().getName());
        propertyDto.setInventoryNumber(property.getInventoryNumber());
        propertyDto.setResponsiblePeople(property.getResponsiblePeople());

        return propertyDto;
    }

    public Property findById(Long id){
        return propertyRepository.findById(id).get();
    }
    public List<TypeChanges> allTypesChanges(){return typeChangesRepository.findAll();}

}
