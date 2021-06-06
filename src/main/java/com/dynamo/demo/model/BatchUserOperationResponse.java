package com.dynamo.demo.model;

import lombok.Data;

import java.util.List;

@Data
public class BatchUserOperationResponse {
    private List<String> failedItems;
}
