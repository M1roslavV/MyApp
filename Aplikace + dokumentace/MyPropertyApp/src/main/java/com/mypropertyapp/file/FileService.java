package com.mypropertyapp.file;

import com.mypropertyapp.property.Property;
import com.mypropertyapp.propertyHistory.PropertyHistory;
import com.mypropertyapp.propertyHistory.PropertyHistoryRepository;
import com.mypropertyapp.propertyHistory.TypeChangesRepository;
import com.mypropertyapp.user.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class FileService {
    private final FileRepository fileRepository;
    private final TypeRepository typeRepository;
    private final PropertyHistoryRepository propertyHistoryRepository;
    private final TypeChangesRepository typeChangesRepository;
    private final UserRepository userRepository;
    public void saveFile(String companyName, MultipartFile file, Property property, String typeName, String email, int change, String from) throws IOException {
        String filename = file.getOriginalFilename();

        String fileExtension = "";
        if (filename != null && filename.contains(".")) {
            fileExtension = filename.substring(filename.lastIndexOf("."));
        }

        String filenameWithoutExtension = Objects.requireNonNull(filename).replaceAll(fileExtension + "$", "");
        String timestamp = String.valueOf(System.currentTimeMillis());
        String newFilename = filenameWithoutExtension + "_" + timestamp + fileExtension;


        Path path = Paths.get("./files/", companyName);
        if (!Files.exists(path))
            Files.createDirectories(path);
        Path filePath = path.resolve(Objects.requireNonNull(newFilename));
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        File fileProperty = new File();
        Type type = new Type();
        if (typeRepository.findByName(typeName) != null)
            type = typeRepository.findByName(typeName);
        else{
            type.setName(typeName);
            typeRepository.save(type);
        }

        if(from != null){
            PropertyHistory propertyHistory = new PropertyHistory();
            propertyHistory.setChanger(userRepository.findByEmail(email).get().getFirstName() + " " + userRepository.findByEmail(email).get().getLastName());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yyyy");
            LocalDateTime now = LocalDateTime.now();
            String date = now.format(formatter);
            propertyHistory.setDateTime(date);
            propertyHistory.setProperty(property);
            propertyHistory.setTypeChanges(typeChangesRepository.findById(change).get());
            propertyHistory.setChangedFrom(from);
            propertyHistory.setChangedTo(newFilename);
            propertyHistoryRepository.save(propertyHistory);
        }

        fileProperty.setAction("ok");
        fileProperty.setName(newFilename);
        fileProperty.setPath(filePath.toString());
        fileProperty.setProperty(property);
        fileProperty.setType(type);
        fileRepository.save(fileProperty);
        deleteFiles();
    }


    public void File(String companyName, String file, HttpServletResponse response, String action) throws IOException {
        Path fileStorageLocation = Paths.get("./files/" + companyName).toAbsolutePath().normalize();
        Path filePath = fileStorageLocation.resolve(file).normalize();
        Resource resource = new UrlResource(filePath.toUri());
        if(resource.exists() && resource.isReadable()) {
            String mimeType = Files.probeContentType(filePath);
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
            response.setContentType(mimeType);
            String encodedFilename = URLEncoder.encode(Objects.requireNonNull(resource.getFilename()), StandardCharsets.UTF_8).replace("+", "%20");
            String contentDisposition = String.format("%s; filename=\"%s\"; filename*=UTF-8''%s", action, encodedFilename, encodedFilename);
            response.setHeader("Content-Disposition", contentDisposition);
            response.setHeader("Content-Length", String.valueOf(resource.contentLength()));
            StreamUtils.copy(resource.getInputStream(), response.getOutputStream());
        } else {
            throw new FileNotFoundException("Soubor nebyl nalezen " + file);
        }
    }

    public void deleteFiles() throws IOException {
        List<File> files = fileRepository.findByProperty(null);
        for (File file : files) {
            Path filePath = Paths.get(file.getPath()).toAbsolutePath().normalize();
            try (Stream<Path> paths = Files.walk(filePath)) {
                paths.forEach(path -> {
                    try {
                        Files.delete(path);
                        fileRepository.delete(file);
                    } catch (IOException e) {
                        System.err.println("Nelze smazat soubor: " + path);
                    }
                });
            }
        }
    }


    public List<File> findByPropertyIdAndTypeName(Long id, String type){
        return fileRepository.findByPropertyIdAndTypeName(id,type);
    }

    public void changeFile(Long id, String type){
        List<File> change = findByPropertyIdAndTypeName(id, type);
        for (File file : change){
            file.setAction(null);
            fileRepository.save(file);
        }
    }

    public String from(Long id, String type) {
        List<File> files = fileRepository.findByActionAndPropertyIdAndTypeName(null, id, type);
        if (!files.isEmpty()) {
            File file = files.getLast();
            file.setProperty(null);
            fileRepository.save(file);
            return file.getName();
        } else {
            return null;
        }
    }

}
