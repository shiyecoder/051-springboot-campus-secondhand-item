package com.grad.secondhand.controller;

import com.grad.secondhand.common.BizException;
import com.grad.secondhand.common.R;
import com.grad.secondhand.common.UserContext;
import com.grad.secondhand.entity.SysFile;
import com.grad.secondhand.mapper.SysFileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 文件上传：按 uploads/{bizType}/{yyyyMM}/{uuid}.{ext} 保存，返回相对路径。
 * 校验扩展名、MIME 与文件头（需求 7.5），前端只用返回路径，禁止填写外链。
 */
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private static final Set<String> IMAGE_EXT = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> DOC_EXT = Set.of("pdf", "docx", "xlsx");
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysFileMapper fileMapper;

    @Value("${app.upload-dir:./uploads}")
    private String uploadDir;

    @PostMapping("/upload")
    public R<Map<String, Object>> upload(@RequestParam("file") MultipartFile file,
                                         @RequestParam(defaultValue = "goods") String bizType) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BizException(4000, "请选择要上传的文件");
        }
        String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String ext = original.contains(".")
                ? original.substring(original.lastIndexOf('.') + 1).toLowerCase() : "";
        boolean image = IMAGE_EXT.contains(ext);
        boolean doc = DOC_EXT.contains(ext);
        if (!image && !doc) {
            throw new BizException(4000, "只允许 jpg/jpeg/png/webp 图片或 pdf/docx/xlsx 文档");
        }
        long limit = image ? 5L * 1024 * 1024 : 20L * 1024 * 1024;
        if (file.getSize() > limit) {
            throw new BizException(4000, image ? "图片不能超过 5MB" : "文档不能超过 20MB");
        }
        // 文件头校验：图片必须是 JPEG/PNG/WEBP
        byte[] head = new byte[12];
        try (var in = file.getInputStream()) {
            int read = in.read(head);
            if (image && read >= 4) {
                boolean jpeg = (head[0] & 0xFF) == 0xFF && (head[1] & 0xFF) == 0xD8;
                boolean png = (head[0] & 0xFF) == 0x89 && head[1] == 'P' && head[2] == 'N' && head[3] == 'G';
                boolean webp = read >= 12 && head[8] == 'W' && head[9] == 'E' && head[10] == 'B' && head[11] == 'P';
                if (!jpeg && !png && !webp) {
                    throw new BizException(4000, "文件内容与扩展名不符，请重新选择图片");
                }
            }
        }
        String month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String name = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path dir = Paths.get(uploadDir, bizType, month).toAbsolutePath().normalize();
        Files.createDirectories(dir);
        Path target = dir.resolve(name);
        file.transferTo(target.toFile());

        String rel = "/" + uploadDir.replace("./", "").replaceAll("^/+", "") + "/" + bizType + "/" + month + "/" + name;
        SysFile f = new SysFile();
        f.setFileName(original);
        f.setFilePath(rel);
        f.setFileSize(file.getSize());
        f.setFileType(ext);
        f.setBizType(bizType);
        f.setUploaderId(UserContext.userId());
        f.setCreateTime(LocalDateTime.now().format(FMT));
        f.setDeleted(0);
        f.setVersion(0);
        fileMapper.insert(f);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("fileName", original);
        data.put("filePath", rel);
        data.put("fileSize", file.getSize());
        data.put("fileType", ext);
        return R.ok(data);
    }
}
