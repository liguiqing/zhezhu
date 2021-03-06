package com.zhezhu.access.domain.model.wechat.config;


import lombok.*;

/**
 * ClassName: WXConfig <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON(可选). <br/>
 * date: 18-3-1 下午4:01 <br/>
 *
 * @author liuyu
 * @version v1.0
 * @since JDK 1.7+
 */

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class WeChatConfig {


    private String appId;

    private String appSecret;

    private String token;

    private String aesKey;

    private String mchId;

    private String apiKey;

    //内存更新
    private volatile String accessToken;

    private volatile long expiresTime;

    public void expireAccessToken() {
        this.expiresTime = 0;
    }

    public boolean isAccessTokenExpired() {
        return System.currentTimeMillis() > this.expiresTime;
    }

    public synchronized void updateAccessToken(String accessToken, int expiresInSeconds) {
        this.accessToken = accessToken;
        this.expiresTime = System.currentTimeMillis() + (expiresInSeconds - 200) * 1000l;
    }

}
