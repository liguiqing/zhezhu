package com.zhezhu.assessment.domain.model.index;

import com.zhezhu.assessment.AssessmentTestConfiguration;
import com.zhezhu.assessment.config.AssessmentApplicationConfiguration;
import com.zhezhu.commons.config.CommonsConfiguration;
import com.zhezhu.commons.util.ClientType;
import com.zhezhu.share.domain.id.identityaccess.TenantId;
import com.zhezhu.share.domain.id.index.IndexId;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Copyright (c) 2016,$today.year, Liguiqing
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

public class IndexRepositoryTest extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    IndexRepository indexRepository;

    @Test
    public void test() {
        assertNotNull(indexRepository);
        TenantId ownerId = new TenantId();
        IndexId id1 = indexRepository.nextIdentity();
        IndexId id2 = indexRepository.nextIdentity();
        IndexId id3 = indexRepository.nextIdentity();
        IndexId id4 = indexRepository.nextIdentity();
        IndexId id5 = indexRepository.nextIdentity();
        Index dIndex = Index.builder().indexId(id1).owner(ownerId)
                .name("D Index").alias("D1").plus(true).score(3.0d).weight(0.2).category(IndexCategory.Morals).build();
        Index lIndex = Index.builder().indexId(id2).owner(ownerId)
                .name("L Index").alias("L1").plus(true).score(3.0d).weight(0.2).category(IndexCategory.Labours).build();
        Index mIndex = Index.builder().indexId(id3).owner(ownerId)
                .name("M Index").alias("M1").plus(true).score(3.0d).weight(0.2).category(IndexCategory.Esthetics).build();
        Index zIndex = Index.builder().indexId(id4).owner(ownerId)
                .name("Z Index").alias("Z1").plus(true).score(3.0d).weight(0.2).category(IndexCategory.Intelligence).build();
        Index tIndex = Index.builder().indexId(id5).owner(ownerId)
                .name("T Index").alias("T1").plus(true).score(3.0d).weight(0.2).category(IndexCategory.Sports).build();

        Index mIndex2 = Index.builder().indexId(indexRepository.nextIdentity()).owner(ownerId)
                .name("M2 Index").alias("M1").plus(true).score(3.0d).weight(0.2).category(IndexCategory.Esthetics).build();
        Index mIndex3 = Index.builder().indexId(indexRepository.nextIdentity()).owner(ownerId)
                .name("M3 Index").alias("M3").plus(false).score(3.0d).weight(0.2).category(IndexCategory.Esthetics).build();
        Index mIndex4 = Index.builder().indexId(indexRepository.nextIdentity()).owner(ownerId)
                .name("M4 Index").alias("M4").plus(false).score(3.0d).weight(0.2).category(IndexCategory.Esthetics).build();
        mIndex3.addChild(mIndex4);
        mIndex.addChild(mIndex2).addChild(mIndex3);
        assertEquals(2,mIndex.size());
        assertEquals(3,mIndex.allSize());

        Index mIndex5 = Index.builder().indexId(indexRepository.nextIdentity())
                .name("M5 Index").alias("M5").plus(true).score(1.0d).weight(0.2).category(IndexCategory.Esthetics).build();
        Index mIndex6 = Index.builder().indexId(indexRepository.nextIdentity())
                .name("M6 Index").score(2.0d).weight(0.2).category(IndexCategory.Esthetics).build();
        mIndex.mappedTo(mIndex5,2.0,0.5);
        mIndex.mappedTo(mIndex6,1.0,0.5);
        assertEquals(2,mIndex.mappedSize());

        mIndex5.addIconWebResource(ClientType.WeChatApp,"m6");
        mIndex5.addIconWebResource(ClientType.WeChatApp,"m6_wechatapp");
        mIndex5.addIconWebResource(ClientType.PC,"m6_pc");

        indexRepository.save(lIndex);
        indexRepository.save(dIndex);
        indexRepository.save(zIndex);
        indexRepository.save(tIndex);
        indexRepository.save(mIndex);

        load(100000,false,id1,id2,id3,id4,id5);


        Index mIndex_ = indexRepository.loadOf(id3);
        assertEquals(mIndex.getName(),mIndex_.getName());
        assertEquals(mIndex.getAlias(),mIndex_.getAlias());
        assertEquals(mIndex.isPlus(),mIndex_.isPlus());

        assertEquals(2,mIndex_.size());
        assertEquals(3,mIndex_.allSize());
        assertEquals(2,mIndex_.mappedSize());
        mIndex_.removeChild(mIndex2);
        assertEquals(1,mIndex_.size());
        assertEquals(2,mIndex_.allSize());

        load(10,false,id1,id2,id3,id4,id5);

        List<Index> indexes = indexRepository.findAllByNameAndOwnerAndCategory("M Index",ownerId,IndexCategory.Esthetics);
        assertEquals(1,indexes.size());
        assertEquals(mIndex,indexes.get(0));

        indexes = indexRepository.findAllByOwnerAndParentIsNull(ownerId);
        assertEquals(5,indexes.size());
        assertTrue(indexes.contains(mIndex));

        indexes = indexRepository.findAllByOwnerIsNullAndParentIsNull();
        assertTrue(indexes.contains(mIndex5));
        assertTrue(indexes.contains(mIndex6));

        indexes = indexRepository.findAllByNameAndCategoryAndOwnerIsNull("M5 Index",IndexCategory.Esthetics);
        assertEquals(1,indexes.size());
        assertTrue(indexes.contains(mIndex5));
        assertEquals(2,indexes.get(0).getWebResources().size());
        assertEquals("m6_wechatapp",indexes.get(0).getIconWebResource(ClientType.WeChatApp));

        indexes = indexRepository.findAllByNameAndCategoryAndOwnerIsNullAndParentIsNull("M5 Index",IndexCategory.Esthetics);
        assertEquals(1,indexes.size());
        assertTrue(indexes.contains(mIndex5));

        indexRepository.delete(id1.id());
        indexRepository.delete(id2.id());
        indexRepository.delete(id3.id());
        indexRepository.delete(id4.id());
        indexRepository.delete(id5.id());
        indexRepository.delete(id1.id());

        load(1,true,id1,id2,id3,id4,id5);

    }

    private void load(int times,boolean isAssertNull,IndexId... ids){
        for(int i=0;i<times;i++){
            for(IndexId id:ids){
                Index index = indexRepository.loadOf(id);
                if(isAssertNull){
                    assertNull(index);
                }else{
                    assertNotNull(index);
                }
            }
        }
    }

}