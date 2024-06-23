package com.charms.services;

import com.charms.beans.Exam;
import com.charms.beans.Student;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.StringTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Service
public class SubAdminServiceImpl implements SubAdminServiceDao {
    @Value("${barcode.storage.path}")
    private String barcodeStoragePath;
    private final Map<String, Student> studentMap = new HashMap<>();

    private final String INSERT_STUDENTS_DATA = "INSERT INTO DS_STUDENTS (name, registrationNo, department, semester) VALUES (?, ?, ?, ?)";
    private final String INSERT_SUBJECTS_DATA = "INSERT INTO DS_SEMESTER_DEPARTMENTS (subjectCode, subjectName, semester, department, examDate, timing) VALUES (?, ?, ?, ?,?, ?)";
    JdbcTemplate jdbcTemplate;

    public SubAdminServiceImpl(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Boolean uploadStudentsData(MultipartFile file) throws IOException {
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
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
    public Boolean uploadSubjectsData(MultipartFile file) throws IOException {
        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            String subjectCode = row.getCell(0).getStringCellValue();
            String subjectName = row.getCell(1).getStringCellValue();
            int semester = (int) row.getCell(2).getNumericCellValue();
            String department = row.getCell(3).getStringCellValue();
            String examDate = row.getCell(4).getStringCellValue();
            String timing = row.getCell(5).getStringCellValue();

            try {
                int counter = jdbcTemplate.update(INSERT_SUBJECTS_DATA, subjectCode, subjectName, semester,department, examDate, timing);
                if (counter <= 0) {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public String createSubjects(String subject, String subCode, String semester, String department, String examDate, String timing, String reg_supp) {
        try {
            int count = jdbcTemplate.update("INSERT INTO DS_SEMESTER_DEPARTMENTS (subjectName, subjectCode, semester, department, examDate, timing, Reg_Supp) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    subject, subCode, semester, department, examDate, timing, reg_supp);
            if (count > 0) {
                return "Subject created successfully";
            } else {
                return "Failed to create subject";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error occurred";
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

    public String generateHtmlFromStudents(List<Student> students) throws Exception {
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<html><head><title>Hall Ticket</title>");
        htmlBuilder.append("<style>");
        htmlBuilder.append("body { font-family: Arial, sans-serif; margin: 0; padding: 0; }");
        htmlBuilder.append(".page { page-break-before: always; padding: 20px; border: 1px solid black; margin: 20px; }");
        htmlBuilder.append(".header { text-align: center; margin-bottom: 20px; }");
        htmlBuilder.append(".header img { float: left; }");
        htmlBuilder.append(".header h4 { margin: 0; }");
        htmlBuilder.append(".details, .subjects { margin-bottom: 20px; }");
        htmlBuilder.append(".subjects table { width: 100%; border-collapse: collapse; }");
        htmlBuilder.append(".subjects th, .subjects td { border: 1px solid black; padding: 5px; text-align: center; }");
        htmlBuilder.append("</style>");
        htmlBuilder.append("</head><body>");

        for (Student student : students) {
            htmlBuilder.append("<div class='page'>");

            // Header section
            htmlBuilder.append("<div class='header'>");
            htmlBuilder.append("<img src='https://upload.wikimedia.org/wikipedia/en/0/04/Jntuk-logo.png' alt='Logo' height='100'/>");
            htmlBuilder.append("<p>Directorate of Institute of Science and Technology</p>");
            htmlBuilder.append("<h4>JAWAHARLAL NEHRU TECHNOLOGICAL UNIVERSITY KAKINADA</h4>");
            htmlBuilder.append("<p>").append(student.getDepartment()).append(" ").append(student.getSemester()).append(" SEMESTER REGULAR/SUPPLEMENTARY EXAMINATIONS</p>");
            htmlBuilder.append("</div>");
            htmlBuilder.append("<hr></hr>");

            // Student details section
            htmlBuilder.append("<div class='details'>");
            htmlBuilder.append("<p><strong>College Name: </strong> ").append("JNTUK-KAKINADA").append("</p>");
            htmlBuilder.append("<p><strong>Exam Center Name: </strong> ").append("JNTUK-KAKINADA").append("</p>");
            htmlBuilder.append("<p><strong>Hall Ticket No:</strong> ").append(student.getRegistrationNo()).append("</p>");
            htmlBuilder.append("<p><strong>Name:</strong> ").append(student.getName()).append("</p>");
            htmlBuilder.append("<p><strong>Department:</strong> ").append(student.getDepartment()).append("</p>");
            htmlBuilder.append("</div>");

            // Subjects section
            htmlBuilder.append("<div class='subjects'>");
            htmlBuilder.append("<table>");
            htmlBuilder.append("<tr>");
            htmlBuilder.append("<th>Date</th>");
            htmlBuilder.append("<th>Time</th>");
            htmlBuilder.append("<th>Subject Code</th>");
            htmlBuilder.append("<th>Subject Name</th>");
            htmlBuilder.append("</tr>");

            for (Exam exam : student.getExam()) {
                htmlBuilder.append("<tr>");
                htmlBuilder.append("<td>").append(exam.getExamDate()).append("</td>");
                htmlBuilder.append("<td>").append(exam.getTiming()).append("</td>");
                htmlBuilder.append("<td>").append(exam.getSubjectCode()).append("</td>");
                htmlBuilder.append("<td>").append(exam.getSubjectName()).append("</td>");
                htmlBuilder.append("</tr>");
            }

            htmlBuilder.append("</table>");
            htmlBuilder.append("</div>");

            htmlBuilder.append("<div class='footer'>");
            htmlBuilder.append("<div>");
            htmlBuilder.append("<img src='https://signaturely.com/wp-content/uploads/2020/04/unreadable-letters-signaturely.svg' alt='Student Signature'/>");
            htmlBuilder.append("<p>Signature of Student</p>");
            htmlBuilder.append("</div>");
            htmlBuilder.append("<div>");
            htmlBuilder.append("<img src='https://www.shutterstock.com/image-vector/fake-hand-drawn-autographs-set-260nw-2295145277.jpg' alt='Controller Signature' height='50'/>");
            htmlBuilder.append("<p>Controller of Examinations</p>");
            htmlBuilder.append("</div>");
            htmlBuilder.append("</div>");
            htmlBuilder.append("</div>");
        }

        htmlBuilder.append("</body></html>");
        return htmlBuilder.toString();
    }


    public String generateBarcode(String hallTicketNo) throws WriterException, IOException {
        String barcodeText = hallTicketNo;
        String filePath = barcodeStoragePath + hallTicketNo + ".png";
        int width = 300;
        int height = 100;
        String imageFormat = "png";

        Code128Writer barcodeWriter = new Code128Writer();
        BitMatrix bitMatrix = barcodeWriter.encode(barcodeText, BarcodeFormat.CODE_128, width, height);

        Path path = FileSystems.getDefault().getPath(filePath);
        MatrixToImageWriter.writeToPath(bitMatrix, imageFormat, path);

        return filePath;
    }


    public List<Student> fetchStudents(String semester, String department) {
        String query = "SELECT ds.name, ds.registrationNo, dsd.* FROM DS_STUDENTS ds JOIN DS_SEMESTER_DEPARTMENTS dsd ON dsd.department LIKE CONCAT('%', ds.department, '%') AND dsd.semester LIKE CONCAT('%', ds.semester, '%') WHERE ds.department = ? AND ds.semester = ? ";
        return jdbcTemplate.query(query, new Object[]{department, semester}, new ResultSetExtractor<List<Student>>() {
            @Override
            public List<Student> extractData(ResultSet rs) throws SQLException {
                if (!studentMap.isEmpty()) {
                    studentMap.clear();
                }
                while (rs.next()) {
                    mapRow(rs);
                }
                return new ArrayList<>(studentMap.values());
            }
        });
    }

    private void mapRow(ResultSet rs) throws SQLException {
        String registrationNo = rs.getString("registrationNo");

        Student student = studentMap.get(registrationNo);
        if (student == null) {
            student = new Student();
            student.setName(rs.getString("name"));
            student.setRegistrationNo(registrationNo);
            student.setDepartment(rs.getString("department"));
            student.setSemester(rs.getString("semester"));
            student.setExam(new ArrayList<>());
            studentMap.put(registrationNo, student);
        }

        Exam exam = new Exam(
                rs.getLong("id"),
                rs.getString("subjectName"),
                rs.getString("subjectCode"),
                rs.getString("semester"),
                rs.getString("department"),
                rs.getString("examDate"),
                rs.getString("timing")
        );
        student.getExam().add(exam);
    }
}
