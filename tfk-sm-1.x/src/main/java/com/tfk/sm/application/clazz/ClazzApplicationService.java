package com.tfk.sm.application.clazz;

import com.tfk.share.domain.id.school.ClazzId;
import com.tfk.sm.domain.model.clazz.Clazz;
import com.tfk.sm.domain.model.clazz.ClazzRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author Liguiqing
 * @since V3.0
 */

public class ClazzApplicationService {
    private static Logger logger = LoggerFactory.getLogger(ClazzApplicationService.class);

    private List<ClazzRepository> clazzRepositories;

    public ClazzApplicationService(List<ClazzRepository> clazzRepositories) {
        this.clazzRepositories = clazzRepositories;
    }

    @Transactional(rollbackFor = Exception.class)
    public void newClazz(NewClazzCommand command){
        logger.debug("New Clazz {}",command);
        Clazz clazz = command.toClazz();
        ClazzRepository clazzRepository = getClazzRepositoryOf(clazz);
        clazzRepository.save(clazz);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateOpenedTime(ClazzId clazzId,Date openedTime){
        //TODO
    }

    private ClazzRepository getClazzRepositoryOf(Clazz clazz){
        for(ClazzRepository clazzRepository:this.clazzRepositories){
            if(clazzRepository.supports(clazz))
                return clazzRepository;
        }
        return null;
    }

    private ClazzRepository getClazzRepositoryOf(Class clazz){
        for(ClazzRepository clazzRepository:this.clazzRepositories){
            if(clazzRepository.supports(clazz))
                return clazzRepository;
        }
        return null;
    }
}