package com.charms.beans;

public class Subject {
    String subjectCode;
    String subjectName;
    String department;
    String semester;
    String examTiming;

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getExamTiming() {
        return examTiming;
    }

    public void setExamTiming(String examTiming) {
        this.examTiming = examTiming;
    }

}
