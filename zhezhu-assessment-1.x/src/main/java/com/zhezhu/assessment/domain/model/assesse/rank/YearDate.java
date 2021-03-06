package com.zhezhu.assessment.domain.model.assesse.rank;

import com.zhezhu.assessment.domain.model.assesse.RankCategory;
import com.zhezhu.assessment.domain.model.assesse.RankCategoryDate;
import com.zhezhu.commons.util.DateUtilWrapper;
import com.zhezhu.share.domain.school.StudyYear;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author Liguiqing
 * @since V3.0
 */
@Component
public class YearDate implements RankCategoryDate {


    @Override
    public boolean supports(RankCategory category) {
        return category.equals(RankCategory.Year);
    }

    @Override
    public Date from() {
        StudyYear year = StudyYear.now();
        return DateUtilWrapper.startOfDay(year.getDefaultDateStarts());
    }

    @Override
    public Date to() {
        StudyYear year = StudyYear.now();
        return DateUtilWrapper.endOfDay(year.getDefaultDateEnds());
    }

    @Override
    public String node() {

        StudyYear year = StudyYear.now();
        return  year.getYearStarts()+"-"+year.getYearEnds();
    }
}