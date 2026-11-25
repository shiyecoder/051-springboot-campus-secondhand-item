package com.grad.logistics.controller;

import com.grad.logistics.common.BizException;
import com.grad.logistics.common.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传：货物照片、回单凭证、头像统一落盘到项目根目录 uploads/，
 * 数据库只保存相对路径 /uploads/yyyyMM/xxx.jpg，不保存外部 URL。
 */
@Slf4j
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    /** 允许的扩展名；前端校验之外后端再校验一次 */
    private static final List<String> ALLOWED = List.of("jpg", "jpeg", "png", "webp", "pdf");

    @Value("${app.upload-dir:./uploads}")
    private String uploadDir;

    @PostMapping("/upload")
    public R<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(400, "请选择要上传的文件");
        }
        String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String ext = original.contains(".")
                ? original.substring(original.lastIndexOf('.') + 1).toLowerCase() : "";
        if (!ALLOWED.contains(ext)) {
            throw new BizException(400, "仅支持 " + String.join("/", ALLOWED) + " 格式");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BizException(400, "单文件不超过 10MB");
        }
        String folder = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String name = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        try {
            Path dir = Paths.get(uploadDir, folder).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(name).toFile());
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BizException(500, "文件保存失败，请重试");
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("path", "/uploads/" + folder + "/" + name);
        data.put("name", original);
        data.put("size", file.getSize());
        return R.ok(data);
    }
}
