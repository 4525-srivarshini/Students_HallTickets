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
        return subAdminService.uploadStudentsData(file);
    }

   /* @GetMapping("/uploadSubjects")
    public String showUploadForm() {
        return "uploadSubjects";
    }

    @PostMapping("/uploadSubjects")
    public String addSubjects(
            @RequestParam String subCode,
            @RequestParam String subject,
            @RequestParam String semester,
            @RequestParam String department,
            @RequestParam String timing,
            @RequestParam String examDate,
            @RequestParam String reg_supp,
            Model model) {
        String result = subAdminService.createSubjects(subject, subCode, semester, department, examDate, timing, reg_supp);
        model.addAttribute("result", result);
        return "uploadSubjects.html";
    }*/

    @PostMapping("/generateHallTicket")
    public ResponseEntity<byte[]> generateHallTicket(@RequestParam String semester, @RequestParam String department) throws IOException, WriterException {
        List<Student> students = subAdminService.fetchStudents(semester, department);
        String htmlContent = subAdminService.generateHtmlFromStudents(students);
        byte[] pdfBytes = subAdminService.convertHtmlToPdf(htmlContent);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "hall_ticket.pdf");
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }


}
