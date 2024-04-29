package com.charms.services;

import com.charms.beans.Student;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.ss.usermodel.*;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.StringTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class SubAdminServiceImpl implements SubAdminServiceDao{

    private final String INSERT_STUDENTS_DATA = "INSERT INTO DS_STUDENTS (name, registrationNo, department, semester) VALUES (?, ?, ?, ?)";
    private final String INSERT_SUBJECTS_DATA = "INSERT INTO DS_SUBJECTS (SubjectName, subjectCode, semester, department) VALUES (?, ?, ?, ?)";

    JdbcTemplate jdbcTemplate;

    public SubAdminServiceImpl(DataSource dataSource){
        jdbcTemplate = new JdbcTemplate(dataSource);
    }
    @Override
    public String uploadStudentsData(MultipartFile file) throws IOException {
        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            String name = row.getCell(0).getStringCellValue();
            String rollNo = row.getCell(1).getStringCellValue();
            String department = row.getCell(2).getStringCellValue();
            int semester = (int) row.getCell(3).getNumericCellValue();
            try {
                int counter = jdbcTemplate.update(INSERT_STUDENTS_DATA, name, rollNo, department, semester);
                if (counter <= 0) {
                    return "Failed to create students";
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "Error occurred";
            }
        }
        return "Data uploaded successfully";
    }

    public String createSubjects(String subject, String subCode, String semester, String department, String timing) {
        try {
            int count = jdbcTemplate.update("INSERT INTO DS_SEMESTER_DEPARTMENTS (SubjectName, subjectCode, semester, department, examTiming) VALUES (?, ?, ?, ?,?)",
                    subject, subCode, semester, department, timing);
            if (count > 0) {
                return "Subject created successfully";
            } else {
                return "Failed to create Subject";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error Occurred";
        }
    }


    public String populateTemplate(String semester, String department) {
        try {
            List<Student> students = jdbcTemplate.query("SELECT ds.*, " +
                            "(SELECT GROUP_CONCAT(CONCAT(subjectname, ' - ', subjectCode)) " +
                            " FROM DS_SUBJECTS " +
                            " WHERE department LIKE CONCAT('%', ds.department, '%') AND semester LIKE CONCAT('%', ds.semester, '%')) AS subjects " +
                            "FROM DS_STUDENTS ds " +
                            "WHERE ds.department = ? AND ds.semester = ?",
                    new Object[]{department, semester},
                    new BeanPropertyRowMapper<>(Student.class));
            TemplateEngine templateEngine = new TemplateEngine();
            templateEngine.setTemplateResolver(new StringTemplateResolver());
            Context context = new Context();
            context.setVariable("students", students);
            return templateEngine.process("hallTicket", context);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error occurred";
        }
    }



    public byte[] convertHtmlToPdf(String htmlContent) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Error converting HTML to PDF", e);
        }
    }

    public String generateHtmlFromStudents(List<Student> students) {
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<html><head><title>Hall Ticket</title></head><body>");
        for (Student student : students) {
            htmlBuilder.append("<div style='page-break-before:always'>"); // Start new page for each student
            htmlBuilder.append("<h4>Directorate of Institute of Science and Technology</h4>");
            htmlBuilder.append("<h3>JAWAHARLAL NEHRU TECHNOLOGICAL UNIVERSITY, KAKINADA </h3>");
            htmlBuilder.append("<p>Regular/Supplementary Feb 2024</p>");
            htmlBuilder.append("<ul>");
            htmlBuilder.append("<li>");
            htmlBuilder.append("<p>College Name: 1S-JNTUK KAKINADA</p>");
            htmlBuilder.append("<p>EXAM CENTER: 1S-JNTUK KAKINADA</p>");
            htmlBuilder.append("<p>EXAM TIME: 10AM-1PM</p>");
            htmlBuilder.append("<p>Name: ").append(student.getName()).append("</p>");
            htmlBuilder.append("<p>Roll No: ").append(student.getRegistrationNo()).append("</p>");
            htmlBuilder.append("<p>Department: ").append(student.getDepartment()).append("</p>");
            htmlBuilder.append("<p>Semester: ").append(student.getSemester()).append("</p>");
            htmlBuilder.append("<p>Subjects: ").append(student.getSubjects()).append("</p>");
            htmlBuilder.append("</li>");
            htmlBuilder.append("</ul>");
            htmlBuilder.append("</div>");
        }
        htmlBuilder.append("</body></html>");
        return htmlBuilder.toString();
    }


    public List<Student> fetchStudents(String semester, String department) {
        String query = "SELECT ds.*, " +
                "(SELECT GROUP_CONCAT(CONCAT(subjectname, ' - ', subjectCode)) " +
                " FROM DS_SEMESTER_DEPARTMENTS " +
                " WHERE department LIKE CONCAT('%', ds.department, '%') AND semester LIKE CONCAT('%', ds.semester, '%')) AS subjects " +
                "FROM DS_STUDENTS ds " +
                "WHERE ds.department = ? AND ds.semester = ?";
        return jdbcTemplate.query(query, new Object[]{department, semester}, new BeanPropertyRowMapper<>(Student.class));
    }
}
