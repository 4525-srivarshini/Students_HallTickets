package com.charms.services;

import com.charms.beans.AdminCreate;

public interface AdminServiceDao {
    public boolean createAdmin(AdminCreate adminCreate);
    public boolean loginAdmin(String username, String password);
    public boolean createSubAdmin(String sname, String email, String EmplId, String Department);
    public boolean editSubAdmin(String sname, String email, String EmplId, String Department);
    public boolean deleteSubAdmin(String email);


    }
