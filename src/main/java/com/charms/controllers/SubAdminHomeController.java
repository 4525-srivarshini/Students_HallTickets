package com.charms.controllers;

import com.charms.beans.Student;
import com.charms.services.SubAdminServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    @PostMapping("/uploadSubjects")
    public String addSubjects(@RequestParam String subCode, @RequestParam String subject, @RequestParam String semester, @RequestParam String department) {
        return subAdminService.createSubjects(subject, subCode,  semester, department);
    }

    @PostMapping("/generateHallTicket")
    public ResponseEntity<byte[]> generateHallTicket(@RequestParam String semester, @RequestParam String department) throws IOException {
        List<Student> students = subAdminService.fetchStudents(semester, department);
        String htmlContent = generateHtmlFromStudents(students);
        byte[] pdfBytes = subAdminService.convertHtmlToPdf(htmlContent);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "hall_ticket.pdf");
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    private String generateHtmlFromStudents(List<Student> students) {
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<html><head><title>Hall Ticket</title></head><body>");
        htmlBuilder.append("<h1>Hall Ticket</h1>");
        htmlBuilder.append("<h2>Student Information</h2>");
        htmlBuilder.append("<ul>");
        for (Student student : students) {
            htmlBuilder.append("<li>");
            htmlBuilder.append("<p>Name: ").append(student.getName()).append("</p>");
            htmlBuilder.append("<p>Roll No: ").append(student.getRegistrationNo()).append("</p>");
            htmlBuilder.append("<p>Department: ").append(student.getDepartment()).append("</p>");
            htmlBuilder.append("<p>Semester: ").append(student.getSemester()).append("</p>");
            htmlBuilder.append("<p>Subjects: ").append(student.getSubjects()).append("</p>");
            htmlBuilder.append("</li>");
        }
        htmlBuilder.append("</ul></body></html>");
        return htmlBuilder.toString();
    }

}
