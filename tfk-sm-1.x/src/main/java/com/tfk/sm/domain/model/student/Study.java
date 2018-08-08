package com.tfk.sm.domain.model.student;

import com.google.common.base.Objects;
import com.tfk.commons.domain.IdentifiedValueObject;
import com.tfk.share.domain.common.Period;
import com.tfk.share.domain.id.school.ClazzId;
import com.tfk.share.domain.id.school.StudentId;
import com.tfk.share.domain.school.Course;
import com.tfk.share.domain.school.Grade;
import com.tfk.share.domain.school.StudyYear;
import com.tfk.share.domain.school.TeachAndStudyClazz;

/**
 * 学生学习
 *
 * @author Liguiqing
 * @since V3.0
 */

public class Study extends IdentifiedValueObject {

    private StudentId studentId;

    private TeachAndStudyClazz clazz;

    protected Study(Student student, ClazzId clazzId, Period period, Course course, Grade grade) {
        this.studentId = student.studentId();
        StudyYear year = StudyYear.newYearsOf(period.starts());
        this.clazz = new TeachAndStudyClazz(student.schoolId(),clazzId, grade, year,course,period);
    }

    public StudentId studentId() {
        return studentId;
    }

    public TeachAndStudyClazz clazz() {
        return clazz;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Study study = (Study) o;
        return Objects.equal(studentId, study.studentId) &&
                Objects.equal(clazz, study.clazz);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(studentId, clazz);
    }

    Study(){}
}