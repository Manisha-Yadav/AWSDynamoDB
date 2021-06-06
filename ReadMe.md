**BACKEND SERVICE FOR A SOCIAL NETWORKING APP**

_User APIs_

1. Create a user - POST /createUser
    
    `{
    
    "userName" : "string", // mandatory
    
    "userId" : "string", // mandatory
    
    "status" : "string",  // optional
    
    "age" : integer, // mandatory
    
    "country" : "string" // optional
    
    "occupation" : "string" // optional
    
    }`
2. Get a user - GET /{user_id}
3. Delete a user - DELETE /{user_id}
4. Get users that belong to a country, sorted by age - GET/{country}/users?ageLowerLimit=2&&ageUpperLimit=80

_Post APIs_

1. Add a post - POST /createPost

    {
       "userId" : "string",
       "post" : "string",
       "creationTimestamp" : timestamp
    }

2. Get all posts for users within a time range - GET /{user_id}/posts?startTime=timestamp1&&endTime=timestamp2
3. Comment on post - POST /comment

    {
        "userName" : "string",
        "comment"  : "string",
        "like"   :  boolean,
        "postToCommentOn" : {
           "userId" : "string" //mandatory
           "post" : "string" //mandatory
           "creationTimestamp" : timestamp //mandatory    
        }
    }
    
4. Get all posts with likes greaterThan specified value - GET /{userId}/likedPosts?likesGreaterThan=4    


**BATCH PROCESS**

_User Apis_

1. Add/Migrate users - POST /users
2. Delete users - DELETE /users