package com.charms.services;

import com.charms.beans.Exam;
import com.charms.beans.Student;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
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
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;

@Service
public class SubAdminServiceImpl implements SubAdminServiceDao{
    @Value("${barcode.storage.path}")
    private String barcodeStoragePath;
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

    public String generateHtmlFromStudents(List<Student> students) throws IOException, WriterException {
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<html><head><title>Hall Ticket</title></head><body>");
        for (Student student : students) {
            htmlBuilder.append("<div style='page-break-before:always'>"); // Start new page for each student
            htmlBuilder.append("<table border='1' cellspacing='0' cellpadding='5'>");
            htmlBuilder.append("<tr>");
            htmlBuilder.append("<td colspan='3'>");
            htmlBuilder.append("<img src='Jntuk-logo.png' alt='Logo' style='float:left;'/>");
            htmlBuilder.append("<p style='text-align:center;'>Directorate of Institute if Science and Technology</p>");
            htmlBuilder.append("<h4 style='text-align:center;'>JAWAHARLAL NEHRU TECHNOLOGICAL UNIVERSITY KAKINADA</h4>");
            htmlBuilder.append("<p style='text-align:center;'>I METCH I SEMESTER REGULAR/SUPPLEMENTARY EXAMINATIONS</p>");
            htmlBuilder.append("</td>");
            htmlBuilder.append("</tr>");

            htmlBuilder.append("<tr>");
            htmlBuilder.append("<td colspan='2'>Hall Ticket No: ").append(student.getRegistrationNo()).append("</td>");
/*
            htmlBuilder.append("<td rowspan='4'><img src='").append(student.getImagePath()).append("' alt='Student Photo' height='100'/></td>");
*/
            htmlBuilder.append("</tr>");

            htmlBuilder.append("<tr>");
            htmlBuilder.append("<td colspan='2'>Name: ").append(student.getName()).append("</td>");
            htmlBuilder.append("</tr>");

            /*htmlBuilder.append("<tr>");
            htmlBuilder.append("<td colspan='2'><barcode>").append(generateBarcode(student.getRegistrationNo())).append("</barcode></td>");
            htmlBuilder.append("</tr>");
*/
            htmlBuilder.append("<tr>");
            htmlBuilder.append("<td colspan='2'>CSE</td>");
            htmlBuilder.append("</tr>");

            htmlBuilder.append("<tr>");
            htmlBuilder.append("<td>Date</td>");
            htmlBuilder.append("<td>Time</td>");
            htmlBuilder.append("<td>Course Code</td>");
            htmlBuilder.append("<td>Registered Courses</td>");
            htmlBuilder.append("</tr>");

            for (Exam exam : student.getExam()) {
                htmlBuilder.append("<tr>");
                htmlBuilder.append("<td>").append(exam.getExamDate()).append("</td>");
                htmlBuilder.append("<td>").append(exam.getTiming()).append("</td>");
                htmlBuilder.append("<td>").append(exam.getSubjectCode()).append("</td>");
                htmlBuilder.append("<td>").append(exam.getSubjectName()).append("</td>");
                htmlBuilder.append("</tr>");
            }

            htmlBuilder.append("<tr>");
            htmlBuilder.append("<td colspan='2'>Signature of Student</td>");
            htmlBuilder.append("<td>Controller of Examinations</td>");
            htmlBuilder.append("<td>Principal</td>");
            htmlBuilder.append("</tr>");

            htmlBuilder.append("<tr>");
            /*htmlBuilder.append("<td colspan='2'><img src='").append(student.getStudentSignaturePath()).append("' alt='Student Signature' height='30'/></td>");
            htmlBuilder.append("<td><img src='path_to_controller_signature.png' alt='Controller Signature' height='30'/></td>");
            htmlBuilder.append("<td><img src='path_to_principal_signature.png' alt='Principal Signature' height='30'/></td>");*/
            htmlBuilder.append("</tr>");

            htmlBuilder.append("</table>");
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
        String query = "SELECT ds.*, " +
                "(SELECT GROUP_CONCAT(CONCAT(subjectname, ' - ', subjectCode)) " +
                " FROM DS_SEMESTER_DEPARTMENTS " +
                " WHERE department LIKE CONCAT('%', ds.department, '%') AND semester LIKE CONCAT('%', ds.semester, '%')) AS subjects " +
                "FROM DS_STUDENTS ds " +
                "WHERE ds.department = ? AND ds.semester = ?";
        return jdbcTemplate.query(query, new Object[]{department, semester}, new BeanPropertyRowMapper<>(Student.class));
    }
}
