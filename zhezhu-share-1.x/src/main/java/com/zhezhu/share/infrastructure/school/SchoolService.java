package com.zhezhu.share.infrastructure.school;

import com.zhezhu.share.domain.common.Period;
import com.zhezhu.share.domain.id.PersonId;
import com.zhezhu.share.domain.id.identityaccess.TenantId;
import com.zhezhu.share.domain.id.school.SchoolId;
import com.zhezhu.share.domain.school.Term;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 学校数据服务
 *
 * @author Liguiqing
 * @since V3.0
 */

@Component
public class SchoolService {

    @Autowired(required = false)
    @Qualifier("schoolInfoApi")
    private SchoolInfoApi schoolInfoApi;

    @Autowired
    @Qualifier("defaultSchoolInfoApi")
    private SchoolInfoApi defaultSchoolInfoApi;

    private SchoolInfoApi api(){
        if(this.schoolInfoApi == null)
            return defaultSchoolInfoApi;
        return this.schoolInfoApi;
    }

    public Period getSchoolTermPeriod(SchoolId schoolId){
        return Term.defaultPeriodOfThisTerm();
    }

    public boolean hasSchool(SchoolId schoolId){
        return this.getSchoolTenantId(schoolId) != null;
    }

    public TenantId getSchoolTenantId(SchoolId schoolId){
        return api().getSchoolTenantId(schoolId);
    }

    public List<PersonId> getAllStudentPersonIds(SchoolId schoolId) {
        return api().getAllStudentPersonIds(schoolId);
    }

    public List<PersonId> getTeacherPersonIds(SchoolId schoolId) {
        return api().getAllTeacherPersonIds(schoolId);
    }

    public StudentData getStudentBy(PersonId personId){
        return api().getStudent(personId);
    }

    public TeacherData getTeacherBy(PersonId personId,SchoolId schoolId){
        return api().getTeacher(personId,schoolId);
    }
}