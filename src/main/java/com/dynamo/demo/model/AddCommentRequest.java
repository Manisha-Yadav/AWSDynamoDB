package com.dynamo.demo.model;

import lombok.Data;

@Data
public class AddCommentRequest {
    private String userName;
    private String comment;
    private boolean like;
    private UserPost postToCommentOn;
}
