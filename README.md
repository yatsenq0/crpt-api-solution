# CrptApi Solution for SELSUP Test Assignment

This project is a Java 11 implementation of the `CrptApi` class as specified in the test assignment for SELSUP. It provides a thread-safe client for interacting with the Russian "Честный знак" (Honest Sign) marking system API, featuring built-in rate limiting.

## 📌 Key Features

*   **Thread-Safety**: The class is designed to be safely used by multiple threads concurrently.
*   **Rate Limiting**: Implements a request rate limiter as specified in the constructor (`TimeUnit`, `requestLimit`). Requests are blocked if the limit is exceeded until the next time window opens.
*   **Core Functionality**: Implements the `createDocument` method for creating a "Ввод в оборот товара, произведенного на территории РФ" (`LP_INTRODUCE_GOODS`) document.
*   **Extensible Design**: The architecture is structured to allow easy addition of new API methods in the future.
*   **Self-Contained**: All necessary data structures are implemented as nested static classes within `CrptApi.java`.

## 🛠️ Requirements

*   **Java Development Kit (JDK)**: Version 11 or higher.
*   **Apache Maven**: For dependency management and building the project.

## 🚀 How to Use

1.  **Build the Project**:
    Open a terminal in the project's root directory and run:
    ```bash
    mvn clean package
    ```
    This command will download dependencies, compile the source code, and generate a runnable JAR file named `crpt-api-solution-1.0.0.jar` in the `target/` directory.

2.  **Integrate into Your Project**:
    You can either:
    *   Add the generated JAR file to your project's classpath.
    *   Install it into your local Maven repository using `mvn install` and then add it as a dependency in your project's `pom.xml`.

3.  **Example Usage**:
    ```java
    import java.util.concurrent.TimeUnit;

    public class UsageExample {
        public static void main(String[] args) throws Exception {
            // Create an instance of CrptApi with a limit of 5 requests per minute.
            CrptApi api = new CrptApi(TimeUnit.MINUTES, 5);

            // Set the authentication token obtained from the /auth/cert/ endpoint.
            api.setAuthToken("your_valid_auth_token_here");

            // Create a new Document object and populate it with your data.
            CrptApi.Document document = new CrptApi.Document();
            CrptApi.Description description = new CrptApi.Description();
            description.setParticipantInn("1234567890");
            document.setDescription(description);
            document.setDocId("DOC-001");
            document.setDocStatus("CREATED");
            document.setDocType("LP_INTRODUCE_GOODS");
            document.setOwnerInn("1234567890");
            document.setParticipantInn("1234567890");
            document.setProducerInn("1234567890");
            document.setProductionDate("2024-05-24");
            document.setProductionType("OWN_PRODUCTION");

            // Create a Product and add it to the document.
            CrptApi.Product product = new CrptApi.Product();
            product.setCertificateDocument("CONFORMITY_CERTIFICATE");
            product.setCertificateDocumentDate("2024-05-24");
            product.setCertificateDocumentNumber("CERT-001");
            product.setOwnerInn("1234567890");
            product.setProducerInn("1234567890");
            product.setProductionDate("2024-05-24");
            product.setTnvedCode("1234567890");
            product.setUitCode("019876543210987654321abcdEFGH1234567");

            document.setProducts(new CrptApi.Product[]{product});
            document.setRegDate("2024-05-24T10:00:00");

            // Call the createDocument method.
            // The 'shoes' parameter is the product group identifier.
            api.createDocument(document, "your_base64_detached_signature_here", "shoes");

            // Gracefully shut down the internal scheduler.
            api.shutdown();
        }
    }
    ```

## 🧠 Implementation Details

*   **Rate Limiting**: The current implementation uses a `java.util.concurrent.Semaphore` combined with a `ScheduledExecutorService`. The semaphore's permits are replenished to the `requestLimit` value every `timeUnit`. This is a simple, working approach. For production-grade applications, libraries like Guava's `RateLimiter` are recommended for more precise control.
*   **HTTP Client**: Uses the built-in `java.net.http.HttpClient` available since Java 11.
*   **JSON Handling**: Uses the Jackson library (`com.fasterxml.jackson`) for serializing Java objects to JSON and vice versa.
*   **Authentication**: The `setAuthToken` method must be called with a valid token obtained from the API's `/auth/cert/` endpoint before calling `createDocument`.

## 📦 Dependencies

This project relies on the [Jackson](https://github.com/FasterXML/jackson) library for JSON processing.

## 👤 Author

Walentn Yatsenko
