package com.zhezhu.assessment.application.index;

import com.zhezhu.assessment.domain.model.index.Index;
import com.zhezhu.assessment.domain.model.index.IndexRepository;
import com.zhezhu.share.domain.id.index.IndexId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Liguiqing
 * @since V3.0
 */

@Service
@Slf4j
public class IndexApplicationService {

    @Autowired
    private IndexRepository indexRepository;

    @Transactional(rollbackFor = Exception.class)
    public String newStIndex(NewIndexCommand command){
        log.debug("New st index {}",command);

        Index index = command.toStIndex(indexRepository.nextIdentity());
        save(index,command);
        return index.getIndexId().id();
    }

    @Transactional(rollbackFor = Exception.class)
    public String newTenantIndex(NewIndexCommand command){
        log.debug("New index {}",command);

        Index index = command.toOwnerIndex(indexRepository.nextIdentity());
        save(index,command);
        return index.getIndexId().id();
    }

    private void save(Index index,NewIndexCommand command){
        if(command.getParentIndexId() != null){
            Index parentIndex = indexRepository.loadOf(new IndexId(command.getParentIndexId()));
            if(parentIndex != null){
                parentIndex.addChild(index);
            }
        }
        indexRepository.save(index);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateIndex(UpdateIndexCommand command){
        log.debug("Update Index {}",command.getIndexId());
        Index index = indexRepository.loadOf(new IndexId(command.getIndexId()));
        command.updateIndex(index);
        indexRepository.save(index);
    }

}