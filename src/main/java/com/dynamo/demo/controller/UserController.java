package com.dynamo.demo.controller;

import com.dynamo.demo.model.BatchUserOperationResponse;
import com.dynamo.demo.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@RestController
public class UserController {

    @PostMapping("/createUser")
    public ResponseEntity<String> createUser(@RequestBody User user){
        System.out.println("START : create User " + user.getUserName());
        user.addUser();
        System.out.println("END : create User " + user.getUserName());

        return new ResponseEntity<String>(
                String.format("User %s created successfully", user.getUserId()),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserDetails(@PathVariable String userId){
        return ResponseEntity.ok(User.loadUser(userId));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable String userId){
        System.out.println("START : delete User Details " + userId);
        User.deleteUser(userId);
        System.out.println("END : delete User Details " + userId);
        return ResponseEntity.ok(
                String.format("User %s DELETED successfully", userId));
    }

    @PostMapping("/users")
    public ResponseEntity<BatchUserOperationResponse> addUsersBatch(@RequestBody List<User> users) {

        List<User> remainingUsers = User.batchSave(users);
        BatchUserOperationResponse response =
                new BatchUserOperationResponse();
        if(remainingUsers.isEmpty()){
            return ResponseEntity.ok(response);
        }

        response.setFailedItems(
            remainingUsers.stream()
                    .map(User::getUserId)
                    .collect(Collectors.toList()));

        return new ResponseEntity<BatchUserOperationResponse>(
                response,
                HttpStatus.MULTI_STATUS
        );
    }

    @DeleteMapping("/users")
    public ResponseEntity<BatchUserOperationResponse> deleteUsersBatch(@RequestBody List<User> users){
        List<Key> pendingDeletes = User.batchDelete(users);
        BatchUserOperationResponse response =
                new BatchUserOperationResponse();
        if(pendingDeletes.isEmpty()){
            return ResponseEntity.ok(response);
        }

        response.setFailedItems(
                pendingDeletes.stream()
                .map(Key::partitionKeyValue)
                .map(AttributeValue::s)
                .collect(Collectors.toList())
        );

        return new ResponseEntity<BatchUserOperationResponse>(
                response,
                HttpStatus.MULTI_STATUS
        );

    }

    @GetMapping("/{country}/users")
    public ResponseEntity<List<User>> getUserDetailsByCountry(@PathVariable String country,
                                                              @RequestParam(required = false) Integer ageLowerLimit,
                                                              @RequestParam(required = false) Integer ageUpperLimit) {
        if(isNull(ageLowerLimit)) {
            ageLowerLimit = 18;
        }

        if(isNull(ageUpperLimit)) {
            ageLowerLimit = 100;
        }

        List<User> users = User.loadUserByCountry(country, ageLowerLimit, ageUpperLimit);
        return ResponseEntity.ok(users);
    }

}
