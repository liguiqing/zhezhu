package com.zhezhu.assessment.application.collaborator;

import com.zhezhu.assessment.domain.model.collaborator.Assessee;
import com.zhezhu.assessment.domain.model.collaborator.AssesseeRepository;
import com.zhezhu.assessment.domain.model.collaborator.Assessor;
import com.zhezhu.assessment.domain.model.collaborator.AssessorRepository;
import com.zhezhu.share.domain.id.PersonId;
import com.zhezhu.share.domain.id.assessment.AssesseeId;
import com.zhezhu.share.domain.id.assessment.AssessorId;
import com.zhezhu.share.domain.id.school.SchoolId;
import com.zhezhu.share.infrastructure.school.SchoolService;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Copyright (c) 2016,$today.year, Liguiqing
 **/
public class CollaboratorServiceTest {

    @Mock
    private SchoolService schoolService;

    @Mock
    private AssesseeRepository assesseeRepository;

    @Mock
    private AssessorRepository assessorRepository;

    @Before
    public void before(){
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void toCollaborator() throws Exception{
        CollaboratorApplicationService service = getService();
        doNothing().when(service).studentToAssessee(anyString());
        doNothing().when(service).teacherToAssessor(anyString());
        service.toCollaborator(new SchoolId().id());
        verify(assesseeRepository,times(0)).save(any(Assessee.class));
    }

    @Test
    public void studentToAssessee() throws Exception{
        CollaboratorApplicationService service = getService();
        List<PersonId> studentPersonIds = new ArrayList<PersonId>();
        studentPersonIds.add(new PersonId());
        studentPersonIds.add(new PersonId());
        studentPersonIds.add(new PersonId());
        when(schoolService.getAllStudentPersonIds(any(SchoolId.class))).thenReturn(studentPersonIds);
        when(assesseeRepository.nextIdentity()).thenReturn(new AssesseeId()).thenReturn(new AssesseeId()).thenReturn(new AssesseeId());
        doNothing().when(assesseeRepository).save(any(Assessee.class));

        service.studentToAssessee(new SchoolId().id());

        verify(schoolService,times(1)).getAllStudentPersonIds(any(SchoolId.class));
        verify(assesseeRepository,times(3)).nextIdentity();
        verify(assesseeRepository,times(3)).save(any(Assessee.class));
    }

    @Test
    public void teacherToAssessor() throws Exception{
        CollaboratorApplicationService service = getService();
        List<PersonId> teacherPersonIds = new ArrayList<PersonId>();
        teacherPersonIds.add(new PersonId());
        teacherPersonIds.add(new PersonId());
        teacherPersonIds.add(new PersonId());
        when(schoolService.getTeacherPersonIds(any(SchoolId.class))).thenReturn(teacherPersonIds);
        when(assessorRepository.nextIdentity()).thenReturn(new AssessorId()).thenReturn(new AssessorId()).thenReturn(new AssessorId());
        doNothing().when(assessorRepository).save(any(Assessor.class));

        service.teacherToAssessor(new SchoolId().id());

        verify(schoolService,times(1)).getTeacherPersonIds(any(SchoolId.class));
        verify(assessorRepository,times(3)).nextIdentity();
        verify(assessorRepository,times(3)).save(any(Assessor.class));
    }

    private CollaboratorApplicationService getService() throws IllegalAccessException {
        CollaboratorApplicationService service = new CollaboratorApplicationService();
        FieldUtils.writeField(service,"schoolService",schoolService,true);
        FieldUtils.writeField(service,"assesseeRepository",assesseeRepository,true);
        FieldUtils.writeField(service,"assessorRepository",assessorRepository,true);

        return spy(service);
    }
}