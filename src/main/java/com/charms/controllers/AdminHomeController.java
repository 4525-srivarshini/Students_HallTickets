package com.charms.controllers;

import com.charms.beans.AdminCreate;
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
        adminService.createAdmin(account);
        return "successMessage";
    }
    @GetMapping("/loginAdmin")
    public String showLoginAdminForm() {
        return "loginAdmin";
    }
    @PostMapping("/loginAdmin")
    public String login(@RequestParam String username, @RequestParam String password) {
        boolean loginSuccess = adminService.loginAdmin(username, password);
        if (loginSuccess) {
            return "redirect:/successMessage"; // Redirect to successMessage if login is successful
        } else {
            return "redirect:/login"; // Redirect to login page if login fails
        }
    }

    @GetMapping("/successMessage")
    public String showSuccessMessage() {
        return "successMessage"; // Return the successMessage view
    }
    @PostMapping("/createSubAdmin")
    public String createSubAdmin(@RequestParam String sname, @RequestParam String email,@RequestParam String employee_Id, @RequestParam String department) {
        return adminService.createSubAdmin(sname, email, employee_Id, department);
    }

    @PostMapping("/editSubAdmin")
    public String editSubAdmin(@RequestParam String sname, @RequestParam String email,@RequestParam String employee_Id, @RequestParam String department) {
        return adminService.editSubAdmin(sname, email, employee_Id, department);
    }

    @PostMapping("/deleteSubAdmin")
    public String deleteSubAdmin(@RequestParam String email) {
        return adminService.deleteSubAdmin(email);
    }

    @GetMapping("/viewSubAdmins")
    public String viewSubAdmin(Model model) {
        List<AdminCreate> subAdmins = adminService.getAllSubAdmins();
        model.addAttribute("subAdmins", subAdmins);
        return "viewSubAdmins";
    }



}
