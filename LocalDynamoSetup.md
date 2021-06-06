
INSTALL
We need Local DynamoDB instance to avoid the cost of running a live instance.
Refer - https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBLocal.html
Download the jar from above link.

START
Navigate to directory of DynamoDBLocal.jar
                java -Djava.library.path=./DynamoDBLocal_lib -jar DynamoDBLocal.jar -sharedDb
If you use the -sharedDb option, DynamoDB creates a single database file named shared-local-instance.db.
If you delete the file, you lose any data that you have stored in it.
DynamoDB uses port 8000 by default.You can use the -port option to specify a different port number.
Local endpoint -  http://localhost:8000
DB credentials are in application.properties file.

To access local DynamoDB
            aws dynamodb list-tables --endpoint-url http://localhost:8000

STOP
DynamoDB : press Ctrl+C at the command prompt.