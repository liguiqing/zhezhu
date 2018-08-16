package com.tfk.assessment.infrastructure.message;

import com.tfk.assessment.domain.model.collaborator.Assessee;
import com.tfk.assessment.domain.model.index.Index;
import com.tfk.commons.message.Messagingable;

import java.time.LocalDate;

/**
 * @author Liguiqing
 * @since V3.0
 */

public class AssessMessage implements Messagingable {

    private String message;

    private String target;

    public AssessMessage(Index index, Assessee assessee,double score) {
        LocalDate date = LocalDate.now();
        this.message = date + ":["+index.getName() + "]得分:"+score;
        this.target = assessee.getCollaborator().getPersonId().id();
    }

    @Override
    public String toMessge() {
        return this.message;
    }

    @Override
    public String target() {
        return this.target;
    }
}