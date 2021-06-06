package com.dynamo.demo.model;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

public class DynamoDbBasePO {

    protected static final DynamoDbEnhancedClient enhancedClient
            = DynamoDbEnhancedClient.create();
}
