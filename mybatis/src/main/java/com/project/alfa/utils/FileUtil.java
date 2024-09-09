package com.project.alfa.utils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.project.alfa.entities.UploadFile;
import com.project.alfa.services.dto.AttachmentResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Profile("!test")
@Component
@RequiredArgsConstructor
public class FileUtil {

//    @Value("${file.upload.location}")
//    private String fileDir;
    
    @Value("${aws.s3.bucket}")
    private String bucket;
    @Value("${aws.s3.upload-dir}")
    private String uploadDir;
    
    private final AmazonS3 amazonS3;
    
    /**
     * 다중 파일 업로드
     *
     * @param multipartFiles
     * @return 업로드 파일 정보 목록
     */
    public List<UploadFile> storeFiles(final List<MultipartFile> multipartFiles) {
        List<UploadFile> uploadFiles = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles)
            if (!multipartFile.isEmpty())
                uploadFiles.add(storeFile(multipartFile));
        return uploadFiles;
    }
    
    /**
     * 단일 파일 업로드
     *
     * @param multipartFile
     * @return 업로드 파일 정보
     */
    public UploadFile storeFile(final MultipartFile multipartFile) {
        if (multipartFile.isEmpty())
            return null;
        
        String originalFilename = multipartFile.getOriginalFilename();
        String storeFilename    = generateStoreFilename(originalFilename);
        String today            = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        //로컬 저장
//        String storeFilePath = getUploadPath(today) + File.separator + storeFilename;
//        File   uploadFile    = new File(storeFilePath);
        
        //AWS S3 저장
        String storeFilePath = uploadDir + "/" + today + "/" + storeFilename;
        
        try {
            //로컬 저장
//            multipartFile.transferTo(uploadFile);
            
            //AWS S3 저장
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(multipartFile.getContentType());
            metadata.setContentLength(multipartFile.getSize());
            amazonS3.putObject(new PutObjectRequest(bucket, storeFilePath, multipartFile.getInputStream(), metadata));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        return new UploadFile(originalFilename, storeFilename, storeFilePath, multipartFile.getSize()) {};
    }
    
    /**
     * 업로드 파일 다중 삭제
     *
     * @param uploadFiles - 업로드 파일 정보 목록
     */
    public void deleteFiles(final List<UploadFile> uploadFiles) {
        if (uploadFiles.isEmpty())
            return;
        for (UploadFile uploadFile : uploadFiles) {
            //로컬 삭제
//            String uploadedDate = uploadFile.getCreatedDate().toLocalDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
//            deleteFile(uploadedDate, uploadFile.getStoreFilename());
            
            //AWS S3 삭제
            deleteFile(uploadFile);
        }
    }
    
    /**
     * 업로드 파일 단일 삭제
     *
     * @param uploadFile - 업로드 파일 정보
     */
    public void deleteFile(final UploadFile uploadFile) {
        if (uploadFile.getStoreFilePath() == null || uploadFile.getStoreFilePath().trim().isEmpty())
            return;
        
        //로컬 삭제
//        String uploadedDate = uploadFile.getCreatedDate().toLocalDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
//        deleteFile(uploadedDate, uploadFile.getStoreFilename());
        
        //AWS S3 삭제
        amazonS3.deleteObject(bucket, uploadFile.getStoreFilePath());
    }
    
    /**
     * 첨부파일 정보 DTO 리소스 변환
     *
     * @param dto - 업로드 파일 정보
     * @return
     */
    public Resource readAttachmentFileAsResource(final AttachmentResponseDto dto) {
        //로컬 리소스
//        String uploadedDate  = dto.getCreatedDate().toLocalDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
//        String storeFilename = dto.getStoreFilename();
//        Path   filePath      = Paths.get(fileDir, uploadedDate, storeFilename);
//
//        try {
//            UrlResource resource = new UrlResource(filePath.toUri());
//            if (!resource.exists() || !resource.isFile())
//                throw new RuntimeException("File not found: " + filePath.toString());
//
//            return resource;
//        } catch (MalformedURLException e) {
//            throw new RuntimeException("File not found: " + filePath.toString(), e);
//        }
        
        //AWS S3 리소스
        String uploadedDate  = dto.getCreatedDate().toLocalDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String storeFilename = dto.getStoreFilename();
        String filePath      = uploadDir + "/" + uploadedDate + "/" + storeFilename;
        
        if (!amazonS3.doesObjectExist(bucket, filePath))
            throw new RuntimeException("File not found: " + filePath);
        S3Object            s3Object = amazonS3.getObject(bucket, filePath);
        InputStreamResource resource = new InputStreamResource(s3Object.getObjectContent());
        
        return resource;
    }
    
    /**
     * 저장 파일명 생성
     *
     * @param filename - 원본 파일명
     * @return 저장 파일명
     */
    private String generateStoreFilename(final String filename) {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        String ext  = StringUtils.getFilenameExtension(filename);
        return uuid + "." + ext;
    }
    
    /**
     * 업로드 경로
     *
     * @return 업로드 경로
     */
//    private String getUploadPath() {
//        return makeDirectories(fileDir);
//    }
    
    /**
     * 업로드 경로
     *
     * @param addPath - 추가 경로
     * @return 업로드 경로
     */
//    private String getUploadPath(final String addPath) {
//        return makeDirectories(fileDir + File.separator + addPath);
//    }
    
    /**
     * 업로드 경로 폴더 생성
     *
     * @param path - 대상 업로드 경로
     * @return 업로드 경로
     */
//    private String makeDirectories(final String path) {
//        File dir = new File(path);
//        if (!dir.exists())
//            dir.mkdirs();
//        return dir.getPath();
//    }
    
    /**
     * 파일 삭제
     *
     * @param addPath  - 추가 경로
     * @param filename - 저장 파일명
     */
//    private void deleteFile(final String addPath, final String filename) {
//        String filePath = Paths.get(fileDir, addPath, filename).toString();
//        deleteFile(filePath);
//    }
    
    /**
     * 파일 삭제
     *
     * @param filePath - 업로드 파일 경로
     */
//    private void deleteFile(final String filePath) {
//        File file = new File(filePath);
//        if (file.exists())
//            file.delete();
//    }
    
}
