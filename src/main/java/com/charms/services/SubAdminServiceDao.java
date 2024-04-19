package com.charms.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface SubAdminServiceDao {
    public String uploadStudentsData(MultipartFile file) throws IOException;
}
