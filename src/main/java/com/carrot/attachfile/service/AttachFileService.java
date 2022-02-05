package com.carrot.attachfile.service;

import com.carrot.attachfile.domain.AttachFile;
import com.carrot.attachfile.repository.AttachFileRepository;
import com.carrot.common.fileservice.FileService;
import com.carrot.exception.custom.AttachFileStorageException;
import com.carrot.article.domain.Article;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
public class AttachFileService {

    private final FileService fileService;

    private final AttachFileRepository attachFileRepository;

    Logger log = LoggerFactory.getLogger(getClass());

    @Value("${attachFileLocation}")
    private String attachFileLocation;

    @Value("${user.home}")
    private String uploadDir;


    public void simpleFileUpload(MultipartFile multipartFile) {
        Path serverPath = Paths.get(
                uploadDir +
                        File.separator +
                        StringUtils.cleanPath(multipartFile.getOriginalFilename()));
        log.info("path : {} \n {}",serverPath.toString(),serverPath.toAbsolutePath());
        try {
            Files.copy(multipartFile.getInputStream(), serverPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("fail to store file : name={}, exception={}",
                      multipartFile.getOriginalFilename(),
                      e.getMessage());
            throw new AttachFileStorageException("fail to store file");
        }
    }

    //@note : 파일 업로드에 시간이 많이 걸리면 >> Transactional 이 DB Conn 을 길게 잡고 있어서 >> DB Conn pool 이 말라서 에러가 발생할수 있음
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void saveAttach(MultipartFile uploadedFile, Article article) {

        String originalName = uploadedFile.getOriginalFilename();
        String validName = "";

        try {
            if (!StringUtils.hasLength(originalName)) {
                validName = fileService.uploadFile(attachFileLocation, originalName, uploadedFile.getBytes());
            }
        } catch (Exception e) {
            throw new AttachFileStorageException("saveAttach : 업로드 실패" + e.getMessage());
        }

        //
        AttachFile attachFileEntity = AttachFile
                .builder()
                .originalFileName(originalName)
                .verifiedFileName(validName)
                .article(article)
                .build();
        attachFileRepository.save(attachFileEntity);
    }


    public void updateAttach(Long attachFileId, MultipartFile itemImgFile) throws Exception {

        if (!itemImgFile.isEmpty()) { // 변경점 없으면 그대로 통과
            AttachFile saved = attachFileRepository
                    .findById(attachFileId)
                    .orElseThrow(EntityNotFoundException::new);

            if (!StringUtils.hasLength(saved.getOriginalFileName())) {
                // 삭제는 파일시스템에 저장된 이름으로
                fileService.deleteFile(attachFileLocation + "/" + saved.getVerifiedFileName());
            }

            String originalName = itemImgFile.getOriginalFilename();
            String validName = fileService.uploadFile(attachFileLocation, originalName, itemImgFile.getBytes());
            saved.updateAttachFile(originalName, validName);
        }
    }
}
