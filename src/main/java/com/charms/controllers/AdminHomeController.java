package com.charms.controllers;

import com.charms.beans.AdminCreate;
import com.charms.beans.Student;
import com.charms.services.AdminServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        }
        else {
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
            return "redirect:/login";
        }
    }
    @GetMapping("/homePage")
    public String showHomePage() {
        return "homePage";
    }

    @PostMapping("/createSubAdmin")
    public String createSubAdmin(@RequestParam String sname, @RequestParam String email,@RequestParam String employee_Id, @RequestParam String department) {
        boolean isCreateSubAdmin =  adminService.createSubAdmin(sname, email, employee_Id, department);
        return "hi";
    }
    @PostMapping("/editSubAdmin")
    public String editSubAdmin(@RequestParam String sname, @RequestParam String email,@RequestParam String employee_Id, @RequestParam String department) {
        boolean isEditSubAdmin =  adminService.editSubAdmin(sname, email, employee_Id, department);
        return "hi";
    }
    @PostMapping("/deleteSubAdmin")
    public String deleteSubAdmin(@RequestParam String email) {
        boolean isDeleteAdmin =  adminService.deleteSubAdmin(email);
        return "hi";
    }

    @GetMapping("/viewSubAdmins")
    public String viewSubAdmin(Model model) {
        List<AdminCreate> subAdmins = adminService.getAllSubAdmins();
        model.addAttribute("subAdmins", subAdmins);
        return "viewSubAdmins";
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
        return "viewStudents";
    }

    @GetMapping("/viewSubjects")
    public String viewSubjects(Model model) {
        List<Student> students = adminService.getSubjects();
        model.addAttribute("students", students);
        return "viewStudents";
    }

}
