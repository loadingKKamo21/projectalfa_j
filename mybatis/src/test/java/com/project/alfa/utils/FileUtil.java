package com.project.alfa.utils;

import com.project.alfa.entities.UploadFile;
import com.project.alfa.services.dto.AttachmentResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Profile("test")
@Component
public class FileUtil {
    
    @Value("${file.upload.location}")
    private String fileDir;
    
    public List<UploadFile> storeFiles(final List<MultipartFile> multipartFiles) {
        List<UploadFile> uploadFiles = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles)
            if (!multipartFile.isEmpty())
                uploadFiles.add(storeFile(multipartFile));
        return uploadFiles;
    }
    
    public UploadFile storeFile(final MultipartFile multipartFile) {
        if (multipartFile.isEmpty())
            return null;
        
        String originalFilename = multipartFile.getOriginalFilename();
        String storeFilename    = generateStoreFilename(originalFilename);
        String today            = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        String storeFilePath = getUploadPath(today) + File.separator + storeFilename;
        File   uploadFile    = new File(storeFilePath);
        
        try {
            multipartFile.transferTo(uploadFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        return new UploadFile(originalFilename, storeFilename, storeFilePath, multipartFile.getSize()) {};
    }
    
    public void deleteFiles(final List<UploadFile> uploadFiles) {
        if (uploadFiles.isEmpty())
            return;
        for (UploadFile uploadFile : uploadFiles) {
            String uploadedDate = uploadFile.getCreatedDate().toLocalDate()
                                            .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            deleteFile(uploadedDate, uploadFile.getStoreFilename());
        }
    }
    
    public void deleteFile(final UploadFile uploadFile) {
        if (uploadFile.getStoreFilePath() == null || uploadFile.getStoreFilePath().trim().isEmpty())
            return;
        
        String uploadedDate = uploadFile.getCreatedDate().toLocalDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        deleteFile(uploadedDate, uploadFile.getStoreFilename());
    }
    
    public Resource readAttachmentFileAsResource(final AttachmentResponseDto dto) {
        String uploadedDate  = dto.getCreatedDate().toLocalDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String storeFilename = dto.getStoreFilename();
        Path   filePath      = Paths.get(fileDir, uploadedDate, storeFilename);
        
        try {
            UrlResource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isFile())
                throw new RuntimeException("File not found: " + filePath.toString());
            
            return resource;
        } catch (MalformedURLException e) {
            throw new RuntimeException("File not found: " + filePath.toString(), e);
        }
    }
    
    private String generateStoreFilename(final String filename) {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        String ext  = StringUtils.getFilenameExtension(filename);
        return uuid + "." + ext;
    }
    
    private String getUploadPath() {
        return makeDirectories(fileDir);
    }
    
    private String getUploadPath(final String addPath) {
        return makeDirectories(fileDir + File.separator + addPath);
    }
    
    private String makeDirectories(final String path) {
        File dir = new File(path);
        if (!dir.exists())
            dir.mkdirs();
        return dir.getPath();
    }
    
    private void deleteFile(final String addPath, final String filename) {
        String filePath = Paths.get(fileDir, addPath, filename).toString();
        deleteFile(filePath);
    }
    
    private void deleteFile(final String filePath) {
        File file = new File(filePath);
        if (file.exists())
            file.delete();
    }
    
}
