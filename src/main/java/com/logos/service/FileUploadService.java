package com.logos.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;


@Slf4j
@Service
public class FileUploadService {

    @Value("${upload.path}")
    private String uploadPath;

    public String uploadImage(MultipartFile file) {
        try {
            log.info("Начало загрузки файла: {}, размер: {} байт", file.getOriginalFilename(), file.getSize());
            log.info("upload.path = {}", uploadPath);
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path uploadDir = Paths.get(uploadPath);
            log.info("Абсолютный путь к папке: {}", uploadDir.toAbsolutePath());
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                log.info("Папка создана: {}", uploadDir);
            }
            Path filePath = uploadDir.resolve(filename);
            Files.write(filePath, file.getBytes());
            log.info("Файл сохранён: {}", filePath);
            return "/uploads/" + filename; // относительный URL
        } catch (IOException e) {
            log.error("Ошибка при сохранении файла", e);
            throw new RuntimeException("Не удалось загрузить изображение: ", e);
        }
    }
}