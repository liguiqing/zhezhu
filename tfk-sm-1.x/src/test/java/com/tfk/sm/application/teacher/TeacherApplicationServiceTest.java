package com.tfk.sm.application.teacher;

import com.tfk.commons.domain.Identities;
import com.tfk.commons.util.DateUtilWrapper;
import com.tfk.share.domain.common.Period;
import com.tfk.share.domain.id.IdPrefixes;
import com.tfk.share.domain.id.SubjectId;
import com.tfk.share.domain.id.school.ClazzId;
import com.tfk.share.domain.id.school.TeacherId;
import com.tfk.share.domain.person.Contact;
import com.tfk.share.domain.person.Gender;
import com.tfk.share.domain.school.Course;
import com.tfk.share.domain.school.Grade;
import com.tfk.share.infrastructure.validate.contact.ContactValidations;
import com.tfk.sm.application.data.Contacts;
import com.tfk.sm.application.data.CourseData;
import com.tfk.sm.domain.model.clazz.*;
import com.tfk.sm.domain.model.teacher.Teacher;
import com.tfk.sm.domain.model.teacher.TeacherRepository;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Date;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

/**
 * Copyright (c) 2016,$today.year, 深圳市易考试乐学测评有限公司
 **/
public class TeacherApplicationServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private LearningClazzRepository learningClazzRepository;

    @Mock
    private SuperviseClazzRepository superviseClazzRepository;

    @Mock
    private UnitedClazzRepository unitedClazzRepository;

    @Mock
    private ContactValidations contactValidations;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void before(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void newTeacher() throws Exception{
        TeacherApplicationService teacherApplicationService = getTeacherApplicationService();
        Date joinDate = DateUtilWrapper.now();
        Contacts[] contacts = new Contacts[5];
        contacts[0] = new Contacts("QQ", "123456");
        contacts[2] = new Contacts("Email", "123456@aa.com");
        contacts[3] = new Contacts("Mobile", "85623321");
        contacts[4] = new Contacts("QQ", "1234567");
        contacts[1] = new Contacts("Weixin", "123456@aa.com");

        NewTeacherCommand command = new NewTeacherCommand("test school",joinDate,null,
                "Test Teacher",null,Gender.Male,contacts);

        when(contactValidations.validate(any(Contact.class))).thenReturn(true).thenReturn(true)
                .thenReturn(true).thenReturn(true).thenReturn(true);
        when(teacherRepository.nextIdentity()).thenReturn(new TeacherId(Identities.genIdNone(IdPrefixes.TeacherIdPrefix)));

        String teacherId = teacherApplicationService.newTeacher(command);
        assertNotNull(teacherId);
        assertTrue(teacherId.startsWith(IdPrefixes.TeacherIdPrefix));
        verify(contactValidations,times(5)).validate(any(Contact.class));
        verify(teacherRepository,times(1)).save(any(Teacher.class));
        verify(teacherRepository,times(1)).nextIdentity();
    }

    @Test
    public void arranging() throws Exception{
        TeacherApplicationService teacherApplicationService = getTeacherApplicationService();
        doNothing().when(teacherApplicationService).addTeacherCourse(any(TeacherArrangingCommand.class),any(Teacher.class));
        doNothing().when(teacherApplicationService).addManagementClazzes(any(TeacherArrangingCommand.class),any(Teacher.class));
        doNothing().when(teacherApplicationService).addTeachingClazzes(any(TeacherArrangingCommand.class),any(Teacher.class));

        Teacher teacher = mock(Teacher.class);
        TeacherId teacherId = new TeacherId();
        when(teacherRepository.loadOf(any(TeacherId.class))).thenReturn(teacher).thenReturn(null);

        TeacherArrangingCommand command = mock(TeacherArrangingCommand.class);
        when(command.getTeacherId()).thenReturn(teacherId.id());
        teacherApplicationService.arranging(command);

        verify(teacherRepository,times(1)).save(any(Teacher.class));

        thrown.expect(NullPointerException.class);
        thrown.expectMessage("sm-03-001");
        teacherApplicationService.arranging(command);

    }

    @Test
    public void addTeacherCourse() throws Exception{
        TeacherApplicationService teacherApplicationService = getTeacherApplicationService();

        TeacherArrangingCommand command = mock(TeacherArrangingCommand.class);
        Teacher teacher = mock(Teacher.class);

        SubjectId subjectId1 = new SubjectId();
        SubjectId subjectId2 = new SubjectId();
        SubjectId subjectId3 = new SubjectId();

        CourseData[] courses = new CourseData[3];
        courses[0] = new CourseData("语文",subjectId1.id());
        courses[1] = new CourseData("数学",subjectId2.id());
        courses[2] = new CourseData("英语",subjectId3.id());

        when(command.getCourses()).thenReturn(courses).thenReturn(null);
        doNothing().when(teacher).addCourse(any(Grade.class),any(Course.class));
        teacherApplicationService.addTeacherCourse(command,teacher);
        teacherApplicationService.addTeacherCourse(command,teacher);

        verify(teacher,times(3)).addCourse(any(Grade.class),any(Course.class));

    }

    @Test
    public void addManagementClazzes() throws Exception{
        TeacherApplicationService teacherApplicationService = getTeacherApplicationService();

        TeacherArrangingCommand command = mock(TeacherArrangingCommand.class);
        Teacher teacher = mock(Teacher.class);

        String[] ids = new String[4];
        ids[0] = Identities.genIdNone(IdPrefixes.ClazzIdPrefix);
        ids[1] = Identities.genIdNone(IdPrefixes.ClazzIdPrefix);
        ids[2] = Identities.genIdNone(IdPrefixes.ClazzIdPrefix);
        ids[3] = Identities.genIdNone(IdPrefixes.ClazzIdPrefix);

        Clazz c1 = mock(SuperviseClazz.class);
        Clazz c2 = mock(UnitedClazz.class);

        when(command.getManagementClazzIds()).thenReturn(ids).thenReturn(null);
        when(superviseClazzRepository.loadOf(any(ClazzId.class))).thenReturn(null).thenReturn((SuperviseClazz)c1).thenReturn((SuperviseClazz)c1).thenReturn(null);
        when(unitedClazzRepository.loadOf(any(ClazzId.class))).thenReturn((UnitedClazz)c2).thenReturn(null);

        when(command.getDateStarts()).thenReturn(new Date()).thenReturn(new Date()).thenReturn(new Date());
        when(command.getDateEnds()).thenReturn(new Date()).thenReturn(new Date()).thenReturn(new Date());

        doNothing().when(teacher).managementAt(any(Period.class),any(Grade.class),any(Clazz.class));

        teacherApplicationService.addManagementClazzes(command,teacher);
        verify(superviseClazzRepository,times(4)).loadOf(any(ClazzId.class));

    }

    @Test
    public void addTeachingClazzes() throws Exception{
        TeacherApplicationService teacherApplicationService = getTeacherApplicationService();

        TeacherArrangingCommand command = mock(TeacherArrangingCommand.class);
        Teacher teacher = mock(Teacher.class);

        SubjectId subjectId1 = new SubjectId();
        SubjectId subjectId2 = new SubjectId();
        SubjectId subjectId3 = new SubjectId();

        CourseData[] courses = new CourseData[3];
        courses[0] = new CourseData("语文",subjectId1.id());
        courses[1] = new CourseData("数学",subjectId2.id());
        courses[2] = new CourseData("英语",subjectId3.id());

        String[] ids = new String[4];
        ids[0] = Identities.genIdNone(IdPrefixes.ClazzIdPrefix);
        ids[1] = Identities.genIdNone(IdPrefixes.ClazzIdPrefix);
        ids[2] = Identities.genIdNone(IdPrefixes.ClazzIdPrefix);
        ids[3] = Identities.genIdNone(IdPrefixes.ClazzIdPrefix);

        LearningClazz c1 = mock(LearningClazz.class);
        UnitedClazz c2 = mock(UnitedClazz.class);

        when(command.getTeachingClazzIds()).thenReturn(ids).thenReturn(null);
        when(command.getCourses()).thenReturn(courses).thenReturn(null);
        when(command.getDateStarts()).thenReturn(new Date()).thenReturn(new Date()).thenReturn(new Date());
        when(command.getDateEnds()).thenReturn(new Date()).thenReturn(new Date()).thenReturn(new Date());

        when(learningClazzRepository.loadOf(any(ClazzId.class))).thenReturn(c1).thenReturn(null).thenReturn(null).thenReturn(null);
        when(unitedClazzRepository.loadOf(any(ClazzId.class))).thenReturn(c2).thenReturn(c2).thenReturn(null);
        doNothing().when(teacher).teachingAt(any(Period.class),any(Grade.class),any(Clazz.class),any(Course.class));
        teacherApplicationService.addTeachingClazzes(command,teacher);
        teacherApplicationService.addTeachingClazzes(command,teacher);
        verify(learningClazzRepository, times(4)).loadOf(any(ClazzId.class));
        verify(unitedClazzRepository, times(3)).loadOf(any(ClazzId.class));
    }

    private TeacherApplicationService getTeacherApplicationService()throws Exception{
        TeacherApplicationService teacherApplicationService = new TeacherApplicationService();
        FieldUtils.writeField(teacherApplicationService,"teacherRepository",teacherRepository,true);
        FieldUtils.writeField(teacherApplicationService,"learningClazzRepository",learningClazzRepository,true);
        FieldUtils.writeField(teacherApplicationService,"superviseClazzRepository",superviseClazzRepository,true);
        FieldUtils.writeField(teacherApplicationService,"unitedClazzRepository",unitedClazzRepository,true);
        FieldUtils.writeField(teacherApplicationService,"contactValidations",contactValidations,true);

        return spy(teacherApplicationService);
    }
}