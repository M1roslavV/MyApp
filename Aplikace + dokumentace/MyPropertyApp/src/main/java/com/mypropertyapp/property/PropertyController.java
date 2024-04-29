package com.mypropertyapp.property;

import com.mypropertyapp.file.File;
import com.mypropertyapp.file.FileRepository;
import com.mypropertyapp.file.FileService;
import com.mypropertyapp.propertyHistory.PropertyHistory;
import com.mypropertyapp.propertyHistory.PropertyHistoryRepository;
import com.mypropertyapp.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.*;

@AllArgsConstructor
@Controller
public class PropertyController {
    private final PropertyService propertyService;
    private final FileService fileService;
    private final UserService userService;
    private final FileRepository fileRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, Principal principal, HttpSession session) {
        Page<Property> page = propertyService.PageAll(pageNo, principal.getName(), "id", "desc", 6);
        Map<String, Integer> pageRange = propertyService.calculatePageRange(page, pageNo, 3);
        model.addAttribute("startPage", pageRange.get("startPage"));
        model.addAttribute("endPage", pageRange.get("endPage"));
        model.addAttribute("properties", page);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("file", new File());
        session.setAttribute("back","/dashboard");
        return "/home/dashboard";
    }

    @GetMapping("/dashboard/add_property")
    public String add_property(Model model, Principal principal,  HttpServletRequest request){
        String referer = request.getHeader("Referer");
        model.addAttribute("referer", referer);
        model.addAttribute("categories", propertyService.categories(principal));
        model.addAttribute("locations", propertyService.locations(principal));
        model.addAttribute("PropertyDto", new PropertyDto());
        return "/property/property_add";
    }

    @PostMapping("/dashboard/add_new_property")
    public String showAddPropertyForm(@ModelAttribute("PropertyDto") PropertyDto propertyDto, Principal principal,
                                      @RequestParam("profilePicture") MultipartFile profilePicture,
                                      @RequestParam("images") MultipartFile[] images,
                                      @RequestParam("invoicesProperty") MultipartFile[] invoices,
                                      @RequestParam("referer") String referer) throws IOException {
        long maxFileSize = 5 * 1024 * 1024;

        if (images.length>=4) {
            referer += "?much_images";
            return "redirect:" + referer;
        }

        if (invoices.length>2) {
            referer += "?much_invoices";
            return "redirect:" + referer;
        }

        Property property = propertyService.saveProperty(propertyDto, principal.getName());
        List<String> allowedImageTypes = Arrays.asList("image/jpeg", "image/png", "image/jpg");

        if (!profilePicture.isEmpty()){
            fileService.changeFile(property.getId(), "profilProperty");
        }
        if (!profilePicture.isEmpty() && profilePicture.getSize()<maxFileSize && allowedImageTypes.contains(profilePicture.getContentType())) {
            fileService.saveFile(userService.findByCompanyId(principal.getName()).get().getEmail(), profilePicture, property, "profilProperty", principal.getName(), 7,fileService.from( property.getId(), "profilProperty"));
        }

        if (images.length > 0){
            fileService.changeFile(property.getId(), "imagesProperty");
        }

        for (MultipartFile image : images) {
            if (!image.isEmpty() && image.getSize()<maxFileSize && allowedImageTypes.contains(image.getContentType())) {
                fileService.saveFile(userService.findByCompanyId(principal.getName()).get().getEmail(), image, property, "imagesProperty", principal.getName(), 8, fileService.from( property.getId(), "imagesProperty"));
            }
        }

        if (invoices.length>0){
            fileService.changeFile(property.getId(), "invoicesProperty");
        }
        for (MultipartFile invoiceP : invoices) {
            if (!invoiceP.isEmpty() && invoiceP.getSize()<maxFileSize && Objects.equals(invoiceP.getContentType(), "application/pdf")) {
                fileService.saveFile(userService.findByCompanyId(principal.getName()).get().getEmail(), invoiceP, property, "invoicesProperty", principal.getName(), 9, fileService.from( property.getId(), "invoicesProperty"));
            }
        }
        return "redirect:" + referer;
    }

    @GetMapping("/{username}/download/{filename:.+}")
    public void downloadFile(HttpServletResponse response, @PathVariable String filename, @PathVariable String username, Principal principal) throws IOException {
        String loggedInUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!username.equals(loggedInUsername)) {
            response.sendRedirect("/dashboard");
            return;
        }
        fileService.File(userService.findByCompanyId(principal.getName()).get().getEmail(), filename, response, "attachment; ");
    }

    @GetMapping("/{username}/view/{filename:.+}")
    public void viewFile(HttpServletResponse response, @PathVariable String filename, @PathVariable String username, Principal principal) throws IOException {
        String loggedInUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!username.equals(loggedInUsername)) {
            response.sendRedirect("/dashboard");
            return;
        }
        fileService.File(userService.findByCompanyId(principal.getName()).get().getEmail(), filename, response, "inline; ");
    }

    @GetMapping("/dashboard/list_of_property")
    public String list_of_property(Model model, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,@RequestParam(name = "sortField", defaultValue = "inventoryNumber") String sortField,
                                   @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir, Principal principal, HttpSession session){
        Page<Property> page = propertyService.PageAll(pageNo, principal.getName(),sortField, sortDir, 10);
        Map<String, Integer> pageRange = propertyService.calculatePageRange(page, pageNo, 5);
        model.addAttribute("startsPage", pageRange.get("startPage"));
        model.addAttribute("endsPage", pageRange.get("endPage"));
        model.addAttribute("properties", page);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("file", new File());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        session.setAttribute("back","/dashboard/list_of_property");
        return "/property/property_list";
    }

    @GetMapping("/dashboard/list_of_property/remove/{id}")
    public String removeProperty(@PathVariable(value = "id") String id) throws IOException {
        Long decrypt = Long.parseLong(userService.decrypt(id));
        propertyService.removePropertyById(decrypt);
        return "redirect:/dashboard/list_of_property";
    }

    @GetMapping("/dashboard/list_of_property/update/{id}")
    public String updateProperty(@PathVariable(value = "id") String id, Model model, HttpServletRequest request, Principal principal) {
        String referer = request.getHeader("Referer");
        Long decrypt = Long.parseLong(userService.decrypt(id));
        PropertyDto property = propertyService.updatePropertyById(decrypt);
        model.addAttribute("referer", referer);
        model.addAttribute("PropertyDto", property);
        model.addAttribute("categories", propertyService.categories(principal));
        model.addAttribute("locations", propertyService.locations(principal));
        return "/property/property_update";
    }

    @GetMapping("/dashboard/detail_property/{id}")
    public String detailProperty(@PathVariable(value = "id") String id,@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, Model model, HttpServletRequest request){
        if (userService.decrypt(id)==null)
            return "redirect:/dashboard";
        Long idProperty = Long.parseLong(userService.decrypt(id));
        Page<PropertyHistory> page = propertyService.PageAllHistories(pageNo, "all", idProperty, 6);
        Map<String, Integer> pageRange = propertyService.calculatePageRange(page, pageNo, 5);
        Property property= propertyService.findById(idProperty);
        List<File> files = fileService.findByPropertyIdAndTypeName(idProperty, "profilProperty");
        List<File> invoices = fileService.findByPropertyIdAndTypeName(idProperty, "invoicesProperty");
        List<File> images = fileService.findByPropertyIdAndTypeName(idProperty, "imagesProperty");

        if (!files.isEmpty()) {
            model.addAttribute("nameProfilProperty", files.getFirst().getName());
        } else {
            model.addAttribute("nameProfilProperty", "");
        }
        if (!invoices.isEmpty()) {
            model.addAttribute("nameInvoicesProperty", invoices);
        } else {
            model.addAttribute("nameInvoicesProperty", "");
        }
        if (!images.isEmpty()) {
            model.addAttribute("nameImagesProperty", images);
        } else {
            model.addAttribute("nameImagesProperty", "");
        }
        model.addAttribute("filteredHistories", page);
        model.addAttribute("startsPage", pageRange.get("startPage"));
        model.addAttribute("endsPage", pageRange.get("endPage"));
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("referer", request.getHeader("Referer"));
        model.addAttribute("propertyDetail", property);
        model.addAttribute("type_changes", propertyService.allTypesChanges());
        return "/property/property_detail";
    }


    @GetMapping("/dashboard/detail_property/filter_changes")
    public String filterPropertyChanges(@RequestParam("filter") String filter,@RequestParam("id") Long propertyId, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, Model model) {
        Page<PropertyHistory> page = propertyService.PageAllHistories(pageNo, filter, propertyId, 6);
        Map<String, Integer> pageRange = propertyService.calculatePageRange(page, pageNo, 5);
        model.addAttribute("filteredHistories", page);
        model.addAttribute("startsPage", pageRange.get("startPage"));
        model.addAttribute("endsPage", pageRange.get("endPage"));
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("currentPage", pageNo);
        return "property/property_detail::histories";
    }



}
