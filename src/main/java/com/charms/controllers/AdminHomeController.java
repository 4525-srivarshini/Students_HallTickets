package com.charms.controllers;

import com.charms.beans.AdminCreate;
import com.charms.beans.Exam;
import com.charms.beans.Student;
import com.charms.beans.Subject;
import com.charms.services.AdminServiceImpl;
import com.charms.services.SubAdminServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@Controller
public class AdminHomeController {
    @Autowired
    AdminServiceImpl adminService;

    @Autowired
    SubAdminServiceImpl subAdminService;

    @GetMapping("/registerAdmin")
    public String showRegisterAdminForm(Model model) {
        model.addAttribute("adminCreate", new AdminCreate());
        return "registerAdmin";
    }

    @PostMapping("/registerAdmin")
    public String register(@ModelAttribute("adminCreate") AdminCreate account) {
        boolean registrationSuccess = adminService.createAdmin(account);
        return registrationSuccess ? "redirect:/homePage" : "redirect:/registerAdmin";
    }

    @GetMapping("/loginAdmin")
    public String showLoginAdminForm() {
        return "loginAdmin";
    }

    @PostMapping("/loginAdmin")
    public String login(@RequestParam String username, @RequestParam String password) {
        boolean loginSuccess = adminService.loginAdmin(username, password);
        return loginSuccess ? "redirect:/homePage" : "redirect:/loginAdmin";
    }

    @GetMapping("/homePage")
    public String showHomePage() {
        return "homePage";
    }

    @PostMapping("/createSubAdmin")
    public String createSubAdmin(@RequestParam String sName, @RequestParam String email, @RequestParam String employee_Id, @RequestParam String department) {
        boolean isCreateSubAdmin = adminService.createSubAdmin(sName, email, employee_Id, department);
        return "redirect:/viewSubAdmins";
    }

    @PostMapping("/editSubAdmin")
    public String editSubAdmin(@RequestParam String sName, @RequestParam String email, @RequestParam String employee_Id, @RequestParam String department) {
        boolean isEditSubAdmin = adminService.editSubAdmin(sName, email, employee_Id, department);
        return "redirect:/viewSubAdmins";
    }

    @GetMapping("/uploadStudentsPage")
    public String uploadStudentsPage() {
        return "uploadStudentsPage";
    }

    @GetMapping("/generateHallTicketPage")
    public String generateHallTicketPage() {
        return "generateHallTicketPage";
    }

    @PostMapping("/deleteSubAdmin")
    public String deleteSubAdmin(@RequestParam String email) {
        boolean isDeleteAdmin = adminService.deleteSubAdmin(email);
        return "redirect:/viewSubAdmins";
    }

    @GetMapping("/viewSubAdmins")
    public String viewSubAdmin(Model model) {
        List<AdminCreate> subAdmins = adminService.getAllSubAdmins();
        model.addAttribute("subAdmins", subAdmins);
        return "viewSubAdmins";
    }

    @GetMapping("/getSubAdmins/{employeeId}")
    public List<AdminCreate> getSubAdmin(Model model, @PathVariable String employeeId) {
        return adminService.getSubAdminDetails(employeeId);
    }

    @GetMapping("/viewDepartments")
    public String viewDepartments(Model model) {
        List<Student> students = adminService.getAllSemesters();
        model.addAttribute("students", students);
        return "viewDepartments";
    }

    @GetMapping("/viewStudents")
    public String viewStudents(Model model, @RequestParam String semester, @RequestParam String department) {
        List<Student> students = adminService.getStudentsByDepartmentAndSemester(department, semester);
        model.addAttribute("students", students);
        return "viewDepartments";
    }

    @GetMapping("/uploadSubjects")
    public String showUploadForm() {
        return "uploadSubjects";
    }

    @PostMapping("/uploadSubjects")
    public String addSubjects(@RequestParam String subCode, @RequestParam String subject, @RequestParam String semester, @RequestParam String department, @RequestParam String timing, @RequestParam String examDate, Model model) {
        String result = adminService.createSubjects(subject, subCode, semester, department, examDate, timing);
        model.addAttribute("result", result);
        return "redirect:/viewSubjects";
    }

    @GetMapping("/viewSubjects")
    public String viewSubjects(Model model) {
        List<Exam> subjects = adminService.getSubjects();
        model.addAttribute("subjects", subjects);
        return "viewSubjects";
    }

    @GetMapping("/viewSubjectsOnSelection")
    public String viewSubjectsOnSelection(Model model, @RequestParam String semester, @RequestParam String department) {
        List<Exam> subjects = adminService.getSubjectsOnSelection(semester, department);
        model.addAttribute("subjects", subjects);
        return "viewSubjects";
    }

    @PostMapping("/editSubjects")
    public String editSubjects(@RequestParam Long id, @RequestParam String subCode, @RequestParam String subject, @RequestParam String semester, @RequestParam String department, @RequestParam String timing, @RequestParam String examDate, Model model) {
        String result = adminService.updateSubject(id, subject, subCode, semester, department, examDate, timing);
        model.addAttribute("result", result);
        return "redirect:/viewSubjects";
    }

    @GetMapping("/deleteSubject/{id}")
    public String deleteSubject(@PathVariable Long id, Model model) {
        String result = adminService.deleteSubject(id);
        model.addAttribute("result", result);
        return "redirect:/viewSubjects";
    }

    @PostMapping("/signOut")
    public String signOut(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/loginAdmin";
    }

    @PostMapping("/uploadStudentsData")
    public String uploadStudentsData(@RequestParam("file") MultipartFile file) throws IOException {
        boolean success = subAdminService.uploadStudentsData(file);
        return success ? "viewDepartments" : "errorMessage";
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
}
