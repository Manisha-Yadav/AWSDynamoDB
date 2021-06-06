package com.dynamo.demo.model;

import lombok.Data;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.extensions.annotations.DynamoDbVersionAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;

/**
 * PutItem - createPost()
 * UpdateItem - addComment()
 * Query - getAllPosts() // based on userId (partitionKey) and within timestamp (sortKey) range
 * LSI "userId-noOfLikes-index" was created during userPost TABLE CREATION
 *      with secondary sort key as noOfLikes (with LSI partition key has to be the partition key for the table)
 *      LOCAL SECONDARY INDEX -
 *          An index that has the SAME HASH KEY as the table, but a DIFFERENT RANGE KEY (in this case noOfLikes).
 *          A local secondary index is "local" because queries on the index can span data
 *          in a particular partition as hash key is same as the table hash key.
 *          Cannot be deleted once created
 *          In a Local Secondary Index, the RANGE KEY value DOES NOT need to be UNIQUE for a GIVEN HASH KEY value
 *
 * Optimistic Locking using version, annotated by DynamoDbVersionAttribute
 */
@Data
@DynamoDbBean
public class UserPost extends DynamoDbBasePO {

    private String userId;
    private String post;
    private Long creationTimestamp;
    private List<Comment> comments;
    private Integer noOfLikes;
    private Long version;

    private static final DynamoDbTable<UserPost> userPostTable =
            enhancedClient.table("userPost", TableSchema.fromBean(UserPost.class));

    @DynamoDbPartitionKey
    public String getUserId() {
        return userId;
    }

    @DynamoDbSortKey
    public Long getCreationTimestamp() {
        return creationTimestamp;
    }

    @DynamoDbSecondarySortKey(indexNames = {"userId-noOfLikes-index"})
    public Integer getNoOfLikes() {
        return noOfLikes;
    }

    /* OPTIMISTIC LOCKING in DynamoDb
    1. Each userPost would have a version number.
    2. Application can update/delete userPost only if the version matches the version on the server side.
    3. ConditionalCheckFailedException is thrown if version value on the server is different from the value on the client side.
    4. Client has to manage the retries
     */
    @DynamoDbVersionAttribute
    public Long getVersion() {
        return version;
    }

    public void createPost() {
        userPostTable.putItem(this);
    }

    public static PageIterable<UserPost> getAllPosts(String userId,
                                                     Instant startTime,
                                                     Instant endTime) {
        return userPostTable.query(
                QueryConditional.sortBetween(
                        Key.builder().partitionValue(userId).sortValue(startTime.toEpochMilli()).build(),
                        Key.builder().partitionValue(userId).sortValue(endTime.toEpochMilli()).build()
                ));
    }

    public static void addComment(UserPost userPost,
                                  String comment,
                                  String userName,
                                  Boolean like) {
        UserPost latestUserPost = userPostTable.getItem(Key.builder()
                                                                .partitionValue(userPost.userId)
                                                                .sortValue(userPost.creationTimestamp)
                                                                .build());
        latestUserPost.setComments(ofNullable(latestUserPost.getComments())
            .orElse(new ArrayList<>()));
        latestUserPost.getComments().add(new Comment(userName, comment));
        if (like) {
            Integer noOfLikes = ofNullable(latestUserPost.getNoOfLikes()).orElse(0);
            latestUserPost.setNoOfLikes(noOfLikes + 1);
        }
        userPostTable.updateItem(latestUserPost);
    }

    public static SdkIterable<Page<UserPost>> getPostsByLikes(String userId, Integer noOfLikes) {
        DynamoDbIndex index = userPostTable.index("userId-noOfLikes-index");
        return index.query(
                QueryConditional.sortGreaterThan(
                        Key.builder()
                                .partitionValue(userId)
                                .sortValue(noOfLikes)
                        .build()
                )
        );
    }
}
