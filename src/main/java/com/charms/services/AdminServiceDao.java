package com.charms.services;

import com.charms.beans.AdminCreate;

public interface AdminServiceDao {
    public String createAdmin(AdminCreate adminCreate);
    public boolean loginAdmin(String username, String password);
    public String createSubAdmin(String sname, String email, String EmplId, String Department);
    public String editSubAdmin(String sname, String email, String EmplId, String Department);
    public String deleteSubAdmin(String email);


    }
