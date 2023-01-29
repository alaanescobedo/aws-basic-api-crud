package lambda.api;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.model.AttributeAction;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lambda.model.ApiResponse;
import org.json.simple.JSONObject;


import java.nio.file.attribute.AttributeView;
import java.util.HashMap;
import java.util.Map;

public class UpdateProduct implements RequestHandler<Map<String, Object>, ApiResponse> {

    @Override
    public ApiResponse handleRequest(Map<String, Object> input, Context context) {

        ApiResponse apiResponse = null;
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        try {
            if (input.get("pathParameters") != null) {
                Map<String, Object> pps = (Map<String, Object>) input.get("pathParameters");
                if (pps.get("id") != null) {
                    String id = (String) pps.get("id");
                    String body = (String) pps.get("body");
                    apiResponse = new ApiResponse(updateData(id, body), headers, 400);
                }
            }
        } catch (Exception e) {
            JSONObject errorObj = new JSONObject();
            errorObj.put("error", e);
            apiResponse = new ApiResponse(errorObj.toString(), headers, 400);
        }
        return apiResponse;
    }

    private String updateData(String id, String body) {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
        DynamoDB dynamoDB = new DynamoDB(client);

        String tableName = System.getenv("TABLE_NAME");
        String primaryKey = System.getenv("PRIMARY_KEY");

        Table table = dynamoDB.getTable(tableName);

        Map<String, AttributeValue> tableKey = new HashMap<>();
        tableKey.put(primaryKey, new AttributeValue().withN(id));

        Map<String, AttributeValueUpdate> item = new HashMap<>();

        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(body);
        JsonObject jsonObject = element.getAsJsonObject();

        if (jsonObject.get("name") != null) {
            item.put("name", new AttributeValueUpdate()
                    .withValue(new AttributeValue().withS(jsonObject.get("name").getAsString()))
                    .withAction(AttributeAction.PUT));
        }

        if (jsonObject.get("price") != null) {
            item.put("price", new AttributeValueUpdate()
                    .withValue(new AttributeValue().withS(jsonObject.get("price").getAsString()))
                    .withAction(AttributeAction.PUT));
        }

        table.putItem((Item) item);
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("message", "Item created with ID: " + jsonObj.get("id"));
        return jsonObject.toString();
    }
}
