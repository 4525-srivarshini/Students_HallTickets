package com.charms.controllers;

import com.charms.beans.Student;
import com.charms.services.SubAdminServiceImpl;
import com.google.zxing.WriterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
public class SubAdminHomeController {
    @Autowired
    SubAdminServiceImpl subAdminService;

    @PostMapping("/uploadStudentsData")
    public String uploadStudentsData(@RequestParam("file") MultipartFile file) throws IOException {
        boolean log = subAdminService.uploadStudentsData(file);
        if (true){
            return "successMessage";
        }
        else {
            return "successMessage.html";
        }
    }

    @PostMapping("/uploadSubjectsData")
    public String uploadSubjectsData(@RequestParam("file") MultipartFile file) throws IOException {
        boolean log = subAdminService.uploadSubjectsData(file);
        if (true){
            return "successMessage";
        }
        else {
            return "successMessage.html";
        }
    }

    @PostMapping("/generateHallTicket")
    public ResponseEntity<byte[]> generateHallTicket(@RequestParam String semester, @RequestParam String department) throws Exception {
        List<Student> students = subAdminService.fetchStudents(semester, department);
        String htmlContent = subAdminService.generateHtmlFromStudents(students);
        byte[] pdfBytes = subAdminService.convertHtmlToPdf(htmlContent);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "hall_ticket.pdf");
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }


}
