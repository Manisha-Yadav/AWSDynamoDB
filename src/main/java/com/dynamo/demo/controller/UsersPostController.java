package com.dynamo.demo.controller;

import com.dynamo.demo.model.AddCommentRequest;
import com.dynamo.demo.model.UserPost;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.util.Objects.isNull;

@RestController
public class UsersPostController {

    @PostMapping("/createPost")
    public ResponseEntity<String> createPost(@RequestBody UserPost userPost) {
        if (isNull(userPost.getCreationTimestamp())) {
            userPost.setCreationTimestamp(Instant.now().toEpochMilli());
        }
        userPost.createPost();
        return new ResponseEntity<>(
                String.format("Post created by %s", userPost.getUserId()),
                HttpStatus.CREATED);
    }

    @GetMapping("/{userId}/{createTimestamp}/post")
    public ResponseEntity<UserPost> getPost(@PathVariable String userId,
                                            @PathVariable String createTimestamp) {

        System.out.println(UserPost.getPost(userId, Long.valueOf(createTimestamp)));
        UserPost userPost = UserPost.getPost(userId, Long.valueOf(createTimestamp));

        return ResponseEntity.ok(userPost);

    }

    @GetMapping("/{userId}/posts")
    public ResponseEntity<List<UserPost>> getPosts(@PathVariable String userId,
                                                   @RequestParam(required = false) String startTimestamp,
                                                   @RequestParam(required = false) String endTimestamp) {
        if (isNull(startTimestamp)) {
            startTimestamp = "1970-01-01 00:00:00";
        }

        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault());
        Instant startTime = Instant.from(formatter.parse(startTimestamp));

        Instant endTime = null;
        if (isNull(endTimestamp)) {
            endTime = Instant.now();
        } else {
            endTime = Instant.from(formatter.parse(endTimestamp));
        }

        Iterator<UserPost> itrUserPost =
        UserPost.getAllPosts(userId, startTime, endTime)
                .items()
                .iterator();

        List<UserPost> results = new ArrayList<>();
        while (itrUserPost.hasNext()) {
            results.add(itrUserPost.next());
        }

        return ResponseEntity.ok(results);
    }

    @PostMapping("/comment")
    public ResponseEntity<String> addComment(@RequestBody AddCommentRequest addCommentRequest) {
        UserPost.addComment(addCommentRequest.getPostToCommentOn(),
                addCommentRequest.getComment(),
                addCommentRequest.getUserName(),
                addCommentRequest.isLike());
        return new ResponseEntity<String>(
                String.format("User %s commented on post created by user %s",
                        addCommentRequest.getUserName(),
                        addCommentRequest.getPostToCommentOn().getUserId()),
                HttpStatus.CREATED);
    }

    @GetMapping("/{userId}/likedPosts")
    public ResponseEntity<List<UserPost>> getPostsByLike(@PathVariable String userId,
                                                   @RequestParam Integer likesGreaterThan) {

        SdkIterable<Page<UserPost>> sdkIterator = UserPost.getPostsByLikes(userId, likesGreaterThan);
        Iterator<Page<UserPost>> itrUserPost = sdkIterator.iterator();

        List<UserPost> results = new ArrayList<>();
        while (itrUserPost.hasNext()) {
            Page<UserPost> page = itrUserPost.next();
            results.addAll(page.items());
        }

        return ResponseEntity.ok(results);
    }


}
