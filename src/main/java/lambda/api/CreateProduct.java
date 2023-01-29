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

public class CreateProduct implements RequestHandler<Map<String, Object>, ApiResponse> {

    @Override
    public ApiResponse handleRequest(Map<String, Object> input, Context context) {

        ApiResponse apiResponse = null;
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        try {
            if (input.get("body") != null) {
                Product product = new Product((String) input.get("body"));
                apiResponse = new ApiResponse(createProduct(product), headers, 200);
            }
        } catch (Exception e) {
            JSONObject errorObj = new JSONObject();
            errorObj.put("error", e);
            apiResponse = new ApiResponse(errorObj.toString(), headers, 400);
        }
        return apiResponse;
    }

    private String createProduct(Product product) {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
        DynamoDB dynamoDB = new DynamoDB(client);

        String tableName = System.getenv("TABLE_NAME");
        String primaryKey = System.getenv("PRIMARY_KEY");

        Table table = dynamoDB.getTable(tableName);
        Item item = new Item().withPrimaryKey(primaryKey, product.getId())
                .withString("name", product.getName())
                .withNumber("price", product.getPrice());

        table.putItem(item);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("message", "Item created with ID: " + product.getId());
        return jsonObject.toString();
    }

}
