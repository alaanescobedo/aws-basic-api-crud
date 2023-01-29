package lambda.api;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import lambda.model.ApiResponse;
import lambda.model.Product;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DeleteProduct implements RequestHandler<Map<String, Object>, ApiResponse> {

    @Override
    public ApiResponse handleRequest(Map<String, Object> input, Context context) {

        int id;
        ApiResponse apiResponse = null;
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        try {
            if (input.get("pathParameters") != null) {
                Map<String, Object> pps = (Map<String, Object>) input.get("pathParameters");

                if (pps.get("id") != null) {
                    id = Integer.parseInt((String) pps.get("id"));
                    apiResponse = new ApiResponse(deleteProduct(id), headers, 200);
                }
            }
        } catch (Exception e) {
            JSONObject errorObj = new JSONObject();
            errorObj.put("error", e);
            apiResponse = new ApiResponse(errorObj.toString(), headers, 400);
        }
        return apiResponse;
    }

    private String deleteProduct(int id) {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
        DynamoDB dynamoDB = new DynamoDB(client);
        String tableName = System.getenv("TABLE_NAME");
        String primaryKey = System.getenv("PRIMARY_KEY");
        dynamoDB.getTable(tableName).deleteItem(primaryKey, id);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("message", "Item deleted with ID: " + id);
        return jsonObject.toString();
    }

}
