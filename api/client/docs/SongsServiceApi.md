# SongsServiceApi

All URIs are relative to *http://10.0.2.2:8080/MusicQuiz/api*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**findAll**](SongsServiceApi.md#findAll) | **GET** /songs | findAll |
| [**getById**](SongsServiceApi.md#getById) | **GET** /songs/{id} | getById |


<a id="findAll"></a>
# **findAll**
> List&lt;Song&gt; findAll()

findAll

Endpoint to return the whole list of songs.

### Example
```java
// Import classes:
import com.usj.musicquizz.invoker.ApiClient;
import com.usj.musicquizz.invoker.ApiException;
import com.usj.musicquizz.invoker.Configuration;
import com.usj.musicquizz.invoker.models.*;
import com.usj.musicquizz.api.SongsServiceApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://10.0.2.2:8080/MusicQuiz/api");

    SongsServiceApi apiInstance = new SongsServiceApi(defaultClient);
    try {
      List<Song> result = apiInstance.findAll();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling SongsServiceApi#findAll");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**List&lt;Song&gt;**](Song.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |
| **404** | Not Found |  -  |
| **500** | Server Error |  -  |

<a id="getById"></a>
# **getById**
> Song getById(id)

getById

Endpoint to get one song by its id.

### Example
```java
// Import classes:
import com.usj.musicquizz.invoker.ApiClient;
import com.usj.musicquizz.invoker.ApiException;
import com.usj.musicquizz.invoker.Configuration;
import com.usj.musicquizz.invoker.models.*;
import com.usj.musicquizz.api.SongsServiceApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://10.0.2.2:8080/MusicQuiz/api");

    SongsServiceApi apiInstance = new SongsServiceApi(defaultClient);
    Integer id = 56; // Integer | Song id to retrieve.
    try {
      Song result = apiInstance.getById(id);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling SongsServiceApi#getById");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **Integer**| Song id to retrieve. | |

### Return type

[**Song**](Song.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |
| **404** | Not Found |  -  |
| **500** | Server Error |  -  |

