package com.zhezhu.share.domain.id.wechat;

import com.zhezhu.commons.domain.AbstractId;
import com.zhezhu.commons.domain.Identities;
import com.zhezhu.share.domain.id.IdPrefixes;

/**
 * 微信唯一标识
 *
 * @author Liguiqing
 * @since V3.0
 */

public class WeChatId extends AbstractId {

    public WeChatId(String anId) {
        super(anId);
    }

    public WeChatId() {
        super(Identities.genIdNone(IdPrefixes.WeChatIdPrefix));
    }
}