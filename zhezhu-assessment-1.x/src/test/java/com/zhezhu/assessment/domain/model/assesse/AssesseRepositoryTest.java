package com.zhezhu.assessment.domain.model.assesse;

import com.zhezhu.assessment.AssessmentTestConfiguration;
import com.zhezhu.assessment.config.AssessmentApplicationConfiguration;
import com.zhezhu.commons.config.CommonsConfiguration;
import com.zhezhu.commons.util.DateUtilWrapper;
import com.zhezhu.share.domain.id.assessment.AssessId;
import com.zhezhu.share.domain.id.assessment.AssessTeamId;
import com.zhezhu.share.domain.id.assessment.AssesseeId;
import com.zhezhu.share.domain.id.assessment.AssessorId;
import com.zhezhu.share.domain.id.index.IndexId;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Copyright (c) 2016,2018, Liguiqing
 **/
@ContextConfiguration(
        classes = {
                AssessmentTestConfiguration.class,
                CommonsConfiguration.class,
                AssessmentApplicationConfiguration.class
        }
)
@Transactional
@Rollback
public class AssesseRepositoryTest extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    AssessRepository assessRepository;

    @Test
    public void test() {
        assertNotNull(assessRepository);
        IndexId indexId = new IndexId();
        AssessId assessId = new AssessId();
        AssesseeId assesseeId = new AssesseeId();
        AssessorId assessorId = new AssessorId();
        AssessTeam team = AssessTeam.builder().assessTeamId(new AssessTeamId()).build();
        Assess assess = Assess.builder()
                .indexId(indexId)
                .assessId(assessId)
                .assessorId(assessorId)
                .assesseeId(assesseeId)
                .category("D")
                .score(10d)
                .word("YAMADIE")
                .doneDate(DateUtilWrapper.now())
                .build();
        assess.associateTo(team);
        assessRepository.save(assess);

        Assess assess_ = assessRepository.loadOf(assessId);
        assertEquals(assess,assess_);
        assertEquals(assess_.getAssessTeamId(), team.getAssessTeamId());
        Date now = DateUtilWrapper.now();
        Assess assess2 = Assess.builder()
                .indexId(indexId)
                .assessId(assessId)
                .assessorId(assessorId)
                .assesseeId(assesseeId)
                .category("D")
                .score(10d)
                .word("YAMADIE")
                .doneDate(now)
                .build();
        assess2.associateTo(team);
        assertNotEquals(assess2,assess);
        assertNotEquals(assess2,assess_);

        Date from = DateUtilWrapper.getStartDayOfWeek(now);
        Date to = DateUtilWrapper.getEndDayOfWeek(now);
        List<Assess> assessList = assessRepository.findByAssesseeIdAndDoneDateBetween(assesseeId, from, to);
        assertEquals(1,assessList.size());

        assessRepository.save(assess2);
        assessList = assessRepository.findByAssessTeamIdAndDoneDateBetween(team.getAssessTeamId(), from, to);
        assertEquals(2,assessList.size());

        assessRepository.delete(assessId);
        assess_ = assessRepository.loadOf(assessId);
        assertNull(assess_);


        testBatch(10,10000);
    }

    private void testBatch(int saves, int querys) {
        AssessId[] ids = new  AssessId[saves];

        IndexId indexId = new IndexId();
        AssesseeId assesseeId = new AssesseeId();
        AssessorId assessorId = new AssessorId();

        for(int i=0;i<saves;i++){
            ids[i] = assessRepository.nextIdentity();
            Assess assess = Assess.builder()
                    .indexId(indexId)
                    .assessId(ids[i] )
                    .assessorId(assessorId)
                    .assesseeId(assesseeId)
                    .category("D")
                    .score(10d)
                    .doneDate(DateUtilWrapper.now())
                    .build();
            assessRepository.save(assess);
        }

        for(int i=0;i<querys;i++){
            for(int j=0;j<saves;j++){
                AssessId id = ids[j];
                Assess assess = assessRepository.loadOf(id);
                assertNotNull(assess);
            }
        }

        for(int j=0;j<saves;j++){
            AssessId id = ids[j];
            assessRepository.delete(id);
            Assess assess = assessRepository.loadOf(id);
            assertNull(assess);
        }
    }

}