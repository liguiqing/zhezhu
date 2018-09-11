package com.zhezhu.access.application.wechat;

import com.zhezhu.access.domain.model.wechat.WeChat;
import com.zhezhu.access.domain.model.wechat.WeChatCategory;
import com.zhezhu.access.domain.model.wechat.WeChatRepository;
import com.zhezhu.commons.util.CollectionsUtilWrapper;
import com.zhezhu.share.domain.id.school.ClazzId;
import com.zhezhu.share.domain.person.Gender;
import com.zhezhu.share.infrastructure.school.SchoolService;
import com.zhezhu.share.infrastructure.school.StudentData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Liguiqing
 * @since V3.0
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class WeChatQueryService {

    @Autowired
    private WeChatRepository weChatRepository;

    @Autowired
    private FollowerTransferHelper followerTransferHelper;

    @Autowired
    private SchoolService schoolService;

    public List<WeChatData> getWeChats(String weChatOpenId) {
        log.debug("Get Wechats by OpenId {}", weChatOpenId);

        return this.weChatRepository.findAllByWeChatOpenId(weChatOpenId)
                .stream().map(weChat -> WeChatData.builder()
                        .personId(weChat.getPersonId().id())
                        .name(weChat.getName())
                        .openId(weChat.getWeChatOpenId())
                        .phone(weChat.getPhone())
                        .role(weChat.getCategory().name())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 取得已经通过审核的关注者
     *
     * @param weChatOpenId
     * @param category
     * @return
     */
    public List<FollowerData> getFollowers(String weChatOpenId, WeChatCategory category) {
        log.debug("Get Followers by OpenId {}", weChatOpenId);

        WeChat weChat = this.weChatRepository.findByWeChatOpenIdAndCategoryEquals(weChatOpenId, category);
        if (weChat == null || CollectionsUtilWrapper.isNullOrEmpty(weChat.getFollowers()))
            return new ArrayList<>();

        return weChat.getFollowers().stream().filter(follower -> follower.isAudited()).map(follower ->
                followerTransferHelper.transTo(follower, category)
        ).collect(Collectors.toList());
    }

    public String[] findFollowerBy(String name, String clazzId, String credentialsName, String credentialsValue, Gender gender) {
        log.debug("Get Follower  by {} and clazzId {}", name, clazzId);

        List<StudentData> studentData = schoolService.getClazzStudents(new ClazzId(clazzId));
        if (CollectionsUtilWrapper.isNullOrEmpty(studentData))
            return new String[]{};

        List<StudentData> names = studentData.stream()
                .filter(student -> student.getName().equals(name))
                .collect(Collectors.toList());
        if (CollectionsUtilWrapper.isNullOrEmpty(names))
            return new String[]{};
        if (names.size() == 1)
            return new String[]{names.get(0).getPersonId()};

        List<StudentData> namesAnGender = names.stream()
                .filter(student -> {
                    if (gender == null)
                        return true;
                    return student.sameGenderAs(gender);
                }).collect(Collectors.toList());
        if (CollectionsUtilWrapper.isNullOrEmpty(namesAnGender))
            return new String[]{};
        if (namesAnGender.size() == 1)
            return new String[]{namesAnGender.get(0).getPersonId()};
        if (credentialsName == null)
            return namesAnGender.stream().map(student->student.getPersonId()).collect(Collectors.toList()).toArray(new String[]{});


        List<StudentData> namesAnGenderAndCredentials = namesAnGender.stream()
                .filter(student -> student.hasCredentials(credentialsName, credentialsValue))
                .collect(Collectors.toList());
        if (CollectionsUtilWrapper.isNullOrEmpty(namesAnGenderAndCredentials))
            return new String[]{};

        return namesAnGenderAndCredentials.stream().map(student->student.getPersonId()).collect(Collectors.toList()).toArray(new String[]{});
    }
}