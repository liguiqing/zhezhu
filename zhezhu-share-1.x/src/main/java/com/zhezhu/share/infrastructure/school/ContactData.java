package com.zhezhu.share.infrastructure.school;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Liguiqing
 * @since V3.0
 */
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ContactData {
    private String category;

    private String name;

    private String value;

    public boolean categoryOf(String category) {
        return this.category.equalsIgnoreCase(category);
    }
}