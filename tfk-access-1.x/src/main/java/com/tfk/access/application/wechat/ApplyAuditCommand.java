package com.tfk.access.application.wechat;

import lombok.*;

/**
 * @author Liguiqing
 * @since V3.0
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ApplyAuditCommand {
    private String auditorId;

    private String applyId;

    private boolean ok;

    private String description;

}