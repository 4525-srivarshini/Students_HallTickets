package com.charms.services;

import com.charms.beans.AdminCreate;
import com.charms.beans.Student;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.sql.DataSource;
import java.util.Date;
import java.util.List;

@Service
@Component
public class AdminServiceImpl implements AdminServiceDao{
    JdbcTemplate jdbcTemplate;
    private static final long EXPIRATION_TIME = 86400000;
    public AdminServiceImpl(DataSource dataSource){
        jdbcTemplate = new JdbcTemplate(dataSource);
    }
    private static final String INSERT_ADMIN_SQL = "INSERT INTO DS_SSO_CREDENTIALS (name, email, conf_password,employee_id, department, roles) VALUES (?, ?, ?, ?, ?,?)";
    private static final String LOGIN_ADMIN_SQL = "SELECT COUNT(*) from DS_SSO_CREDENTIALS where email = ? and conf_password = ?";

    private static final String UPDATE_ADMIN_SQL = "UPDATE DS_SSO_CREDENTIALS SET name = ?, email = ?, department = ? , employee_id = ? WHERE email = ?";

    private static final String DELETE_ADMIN_SQL = "DELETE FROM DS_SSO_CREDENTIALS WHERE email= ?";

    @Override
    public boolean createAdmin(AdminCreate adminCreate) {
        String name = adminCreate.getsName();
        String email = adminCreate.getEmail();
        String password = adminCreate.getPassword();
        String confPassword = adminCreate.getConfPassword();
        String employeeId = adminCreate.getEmployeeId();
        String department = adminCreate.getDepartment();
        String roles = "ADMIN";
        try {
            if (!password.equals(confPassword)) {
                return false;
            }
            int counter = jdbcTemplate.update(INSERT_ADMIN_SQL, name, email, password,employeeId, department, roles);
            if (counter > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean loginAdmin(String email, String password) {
        try {
            int count = jdbcTemplate.queryForObject(LOGIN_ADMIN_SQL, Integer.class, email, password);
            if (count > 0) {
                String jwtToken = generateToken(email);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String generateToken(String mobileNo) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder().setSubject(mobileNo).setIssuedAt(now).setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, generateSecureKey()).compact();
    }
    private SecretKey generateSecureKey() {
        return Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }

    public boolean createSubAdmin(String sName, String email, String employeeId, String department){
        String password = sName.trim()+"33";
        String roles = "SUB_ADMIN";
        try {
            int counter = jdbcTemplate.update(INSERT_ADMIN_SQL, sName, email, password,employeeId, department, roles);
            if (counter > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean editSubAdmin(String sName, String email, String employeeId, String department) {
        try {
            int counter = jdbcTemplate.update(UPDATE_ADMIN_SQL, sName, email, department, employeeId, email);
            if (counter > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean deleteSubAdmin(String email) {
        try {
            int counter = jdbcTemplate.update(DELETE_ADMIN_SQL, email);
            if (counter > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public List<AdminCreate> getAllSubAdmins() {
        String sql = "SELECT * FROM DS_SSO_CREDENTIALS where roles = 'SUB_ADMIN'";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            AdminCreate adminCreate = new AdminCreate();
            adminCreate.setEmployeeId(rs.getString("employee_id"));
            adminCreate.setsName(rs.getString("name"));
            adminCreate.setEmail(rs.getString("email"));
            adminCreate.setDepartment(rs.getString("department"));
            adminCreate.setRoles(rs.getString("roles"));
            return adminCreate;
        });
    }

    public List<Student> getAllSemesters() {
        String sql = "SELECT DISTINCT Department, Semester FROM DS_STUDENTS";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Student student = new Student();
            student.setDepartment(rs.getString("department"));
            student.setSemester(rs.getString("semester"));
            return student;
        });
    }


    public List<Student> getStudentsByDepartmentAndSemester(String department, String semester) {
        String sql = "SELECT * FROM DS_STUDENTS WHERE department = ? AND semester = ?";
        return jdbcTemplate.query(sql, new Object[]{department, semester}, (rs, rowNum) -> {
            Student student = new Student();
            student.setRegistrationNo(rs.getString("registrationNo"));
            student.setName(rs.getString("name"));
            student.setDepartment(rs.getString("department"));
            student.setSemester(rs.getString("semester"));
            return student;
        });
    }

    public List<Student> getSubjects() {
        String sql = "SELECT * FROM DS_STUDENTS";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Student student = new Student();
            student.setRegistrationNo(rs.getString("registrationNo"));
            student.setName(rs.getString("name"));
            student.setDepartment(rs.getString("department"));
            student.setSemester(rs.getString("semester"));
            return student;
        });
    }



}
