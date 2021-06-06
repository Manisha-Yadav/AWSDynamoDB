package com.dynamo.demo.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@Data
@NoArgsConstructor
@DynamoDbBean
public class Comment {
    private String userName;
    private String comment;

    public Comment(String userName, String comment) {
        this.userName = userName;
        this.comment = comment;
    }
}
