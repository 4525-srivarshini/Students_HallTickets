package com.charms.controllers;

import com.charms.beans.AdminCreate;
import com.charms.beans.Exam;
import com.charms.beans.Student;
import com.charms.beans.Subject;
import com.charms.services.AdminServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class AdminHomeController {
    @Autowired
    AdminServiceImpl adminService;

    @GetMapping("/registerAdmin")
    public String showRegisterAdminForm(Model model) {
        model.addAttribute("adminCreate", new AdminCreate());
        return "registerAdmin";
    }

    @PostMapping("/registerAdmin")
    public String register(@ModelAttribute("adminCreate") AdminCreate account) {
        boolean registrationSuccess = adminService.createAdmin(account);
        if (registrationSuccess) {
            return "redirect:/homePage";
        } else {
            return "redirect:/registerAdmin";
        }
    }

    @GetMapping("/loginAdmin")
    public String showLoginAdminForm() {
        return "loginAdmin";
    }

    @PostMapping("/loginAdmin")
    public String login(@RequestParam String username, @RequestParam String password) {
        boolean loginSuccess = adminService.loginAdmin(username, password);
        if (loginSuccess) {
            return "redirect:/homePage";
        } else {
            return "redirect:/loginAdmin";
        }
    }

    @GetMapping("/homePage")
    public String showHomePage() {
        return "homePage";
    }

    @PostMapping("/createSubAdmin")
    public String createSubAdmin(@RequestParam String sname, @RequestParam String email, @RequestParam String employee_Id, @RequestParam String department) {
        boolean isCreateSubAdmin = adminService.createSubAdmin(sname, email, employee_Id, department);
        return "redirect:/viewSubAdmins";
    }

    @PostMapping("/editSubAdmin")
    public String editSubAdmin(@RequestParam String sname, @RequestParam String email, @RequestParam String employee_Id, @RequestParam String department) {
        boolean isEditSubAdmin = adminService.editSubAdmin(sname, email, employee_Id, department);
        return "redirect:/viewSubAdmins";
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
        List<AdminCreate> subAdminDetails = adminService.getSubAdminDetails(employeeId);
        return subAdminDetails;
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
        List<Subject> subjects = adminService.getAllSubjects();
        model.addAttribute("students", students);
        model.addAttribute("subjects", subjects);
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
}
