package com.dynamo.demo.model;

import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.*;

/**
 * ConditionalWrites - addUser() // writing only if userId is not already present
 * DeleteItem - deleteUser()
 * BatchWriteItem - batchDelete() and batchSave()
 * GetItem - loadUsers()
 * Global Secondary Index - Index name - country-age-index
 *                        - Partition Value - country
 *                        - Sort Value - age
 *                        Output will display Users that belong to a country , sorted by age.
 *
 * We create GSI is we want to query the same table data using values other than the table hash key/partition key and range key
 *
 * Global secondary index — an index with a HASH AND RANGE KEY that can be DIFFERENT from those on the TABLE.
 * A global secondary index is considered "global" because queries on the index can span all of the data in a table,
 * across ALL PARTITIONS.
 *
 * Global Secondary Indexes have their OWN PROVISIONED THROUGHPUT,
 * when you query the index the operation will consume read capacity from the index
 *
 * Global Secondary Indexes can be created when you create the table and added to an existing table,
 * deleting an existing Global Secondary Index is also allowed.
 *
 * the key values (Hash and Range) DO NOT need to be unique.
 */
@DynamoDbBean
@Data
public class User extends DynamoDbBasePO {

    private String userName;
    private String userId;
    private String status;
    private Integer age;
    private String country;
    private String occupation;

    private static final DynamoDbTable<User> userTable =
            enhancedClient.table("user", TableSchema.fromBean(User.class));

    @DynamoDbPartitionKey
    public String getUserId() {
        return userId;
    }

    @DynamoDbSecondarySortKey(indexNames = {"country-age-index"})
    public Integer getAge() { return age;    }

    @DynamoDbSecondaryPartitionKey(indexNames = {"country-age-index"})
    public String getCountry() {  return country;    }

    public void addUser() {
        AttributeValue valueTobeUsedInExpression
                = AttributeValue.builder().s(this.getUserId()).build();

        Map<String, AttributeValue> varsToBReplacedByValueInExpression = new HashMap<>();
        varsToBReplacedByValueInExpression.put(":myvar", valueTobeUsedInExpression);

        Expression myExpression = Expression.builder()
                .expression("userId <> :myvar")
                .expressionValues(varsToBReplacedByValueInExpression)
                .build();

        userTable.putItem(
                PutItemEnhancedRequest.<User>builder(User.class)
                            .item(this)
                            .conditionExpression(myExpression)
                        .build()
        );
    }

    public static User loadUser(String userId) {
        return userTable.getItem(
                Key.builder().partitionValue(userId).build());
    }

    public static List<User> loadUserByCountry(String country , Integer ageLowerLimit , Integer ageUpperLimit) {
        List<User> users = new ArrayList<>();

        DynamoDbIndex dbIndex = userTable.index("country-age-index");
        Iterator<Page<User>> itrUser = dbIndex.query(QueryConditional.sortBetween(
                                        Key.builder().partitionValue(country).sortValue(ageLowerLimit).build(),
                                        Key.builder().partitionValue(country).sortValue(ageUpperLimit).build()))
                                        .iterator();

        while (itrUser.hasNext()) {
            Page<User> page = itrUser.next();
            users.addAll(page.items());
        }

        return users;
    }

    public static void deleteUser(String userId) {
        userTable.deleteItem(
                Key.builder().partitionValue(userId).build());
    }

    public static List<User> batchSave(List<User> users) {

        WriteBatch.Builder writeBuilder = WriteBatch.builder(User.class)
                .mappedTableResource(userTable);

        users.forEach(writeBuilder::addPutItem);

        BatchWriteItemEnhancedRequest batchWriteRequest =
                BatchWriteItemEnhancedRequest.builder()
                        .writeBatches(writeBuilder.build())
                .build();

        // The ENTIRE call will fail
        // 1. even if ONE item is GREATER THAN 400kb
        // 2. Payload is GREATER THAN 16MB
        // 3. if the size of users is < 1 OR > 25
        BatchWriteResult result = enhancedClient.batchWriteItem(batchWriteRequest);
        return result.unprocessedPutItemsForTable(userTable);

    }

    public static List<Key> batchDelete(List<User> users) {

        WriteBatch.Builder writeBuilder = WriteBatch.builder(User.class)
                .mappedTableResource(userTable);

        users.forEach(writeBuilder::addDeleteItem);

        BatchWriteItemEnhancedRequest batchWriteRequest =
                BatchWriteItemEnhancedRequest.builder()
                        .writeBatches(writeBuilder.build())
                        .build();

        BatchWriteResult result = enhancedClient.batchWriteItem(batchWriteRequest);
        return result.unprocessedDeleteItemsForTable(userTable);
    }
}
