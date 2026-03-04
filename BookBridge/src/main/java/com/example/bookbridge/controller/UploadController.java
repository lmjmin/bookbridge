package com.example.bookbridge.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/api/upload")
public class UploadController {
    private final Path uploadDir = Paths.get("uploads").toAbsolutePath().normalize();

    @PostMapping("/image")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file){
        try{
            Files.createDirectories(uploadDir);
            String orig = file.getOriginalFilename();
            String clean = (orig==null)? "file": StringUtils.cleanPath(orig);
            String ext = "";
            int i = clean.lastIndexOf('.');
            if(i>-1 && i<clean.length()-1) ext = clean.substring(i);
            String name = UUID.randomUUID() + ext;
            Path to = uploadDir.resolve(name);
            Files.copy(file.getInputStream(), to, StandardCopyOption.REPLACE_EXISTING);
            return ResponseEntity.ok(Map.of("ok",true,"url","/uploads/"+name));
        }catch(Exception e){
            return ResponseEntity.internalServerError().body(Map.of("ok",false,"message",e.getMessage()));
        }
    }
}
