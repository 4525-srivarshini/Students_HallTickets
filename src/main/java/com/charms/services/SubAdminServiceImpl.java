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

    public String createSubjects(String subject, String subCode, String semester, String department) {
        try {
            int count = jdbcTemplate.update("INSERT INTO DS_SUBJECTS (SubjectName, subjectCode, semester, department) VALUES (?, ?, ?, ?)",
                    subject, subCode, semester, department);
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
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(100, 700);
                contentStream.showText(htmlContent);
                contentStream.endText();
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            document.save(byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }
 }


    public List<Student> fetchStudents(String semester, String department) {
        String query = "SELECT ds.*, " +
                "(SELECT GROUP_CONCAT(CONCAT(subjectname, ' - ', subjectCode)) " +
                " FROM DS_SUBJECTS " +
                " WHERE department LIKE CONCAT('%', ds.department, '%') AND semester LIKE CONCAT('%', ds.semester, '%')) AS subjects " +
                "FROM DS_STUDENTS ds " +
                "WHERE ds.department = ? AND ds.semester = ?";
        return jdbcTemplate.query(query, new Object[]{department, semester}, new BeanPropertyRowMapper<>(Student.class));
    }
}
