import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

// Jackson library imports for JSON serialization/deserialization.
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * A thread-safe Java client for the "Честный знак" API with built-in rate limiting.
 * <p>
 * This class implements the requirements specified in the SELSUP test assignment.
 * It allows for the creation of a "Ввод в оборот товара, произведенного на территории РФ" document.
 * The class is designed to be easily extensible for adding new API methods in the future.
 * </p>
 * <p>
 * <strong>Usage Example:</strong>
 * <pre>{@code
 *     // Create an instance with a limit of 5 requests per minute.
 *     CrptApi api = new CrptApi(TimeUnit.MINUTES, 5);
 *
 *     // Set the authentication token.
 *     api.setAuthToken("your_valid_auth_token_here");
 *
 *     // Create and populate a Document object.
 *     CrptApi.Document document = new CrptApi.Document();
 *     // ... populate document fields ...
 *
 *     // Call the createDocument method.
 *     api.createDocument(document, "your_base64_detached_signature_here", "shoes");
 * }</pre>
 * </p>
 */
public class CrptApi {

    // ----------------------------
    // Nested Classes (Data Transfer Objects - DTOs)
    // These classes represent the structure of the JSON document for the API.
    // They are declared as 'public static' to be accessible and not tied to an instance of CrptApi.
    // ----------------------------

    /**
     * Represents the main document structure for introducing goods into circulation.
     * This class maps directly to the JSON structure required by the API endpoint for
     * the "LP_INTRODUCE_GOODS" document type.
     */
    public static class Document {
        private Description description;
        @JsonProperty("doc_id")
        private String docId;
        @JsonProperty("doc_status")
        private String docStatus;
        @JsonProperty("doc_type")
        private String docType;
        @JsonProperty("importRequest")
        private Boolean importRequest;
        @JsonProperty("owner_inn")
        private String ownerInn;
        @JsonProperty("participant_inn")
        private String participantInn;
        @JsonProperty("producer_inn")
        private String producerInn;
        @JsonProperty("production_date")
        private String productionDate;
        @JsonProperty("production_type")
        private String productionType;
        private Product[] products;
        @JsonProperty("reg_date")
        private String regDate;
        @JsonProperty("reg_number")
        private String regNumber;

        // Getters and Setters for all fields.

        /**
         * Gets the nested description object.
         * @return The description object.
         */
        public Description getDescription() { return description; }

        /**
         * Sets the nested description object.
         * @param description The description object.
         */
        public void setDescription(Description description) { this.description = description; }

        /**
         * Gets the document ID.
         * @return The document ID.
         */
        public String getDocId() { return docId; }

        /**
         * Sets the document ID.
         * @param docId The document ID.
         */
        public void setDocId(String docId) { this.docId = docId; }

        /**
         * Gets the document status.
         * @return The document status.
         */
        public String getDocStatus() { return docStatus; }

        /**
         * Sets the document status.
         * @param docStatus The document status.
         */
        public void setDocStatus(String docStatus) { this.docStatus = docStatus; }

        /**
         * Gets the document type.
         * @return The document type.
         */
        public String getDocType() { return docType; }

        /**
         * Sets the document type.
         * @param docType The document type.
         */
        public void setDocType(String docType) { this.docType = docType; }

        /**
         * Gets the import request flag.
         * @return The import request flag.
         */
        public Boolean getImportRequest() { return importRequest; }

        /**
         * Sets the import request flag.
         * @param importRequest The import request flag.
         */
        public void setImportRequest(Boolean importRequest) { this.importRequest = importRequest; }

        /**
         * Gets the owner's INN (Taxpayer Identification Number).
         * @return The owner's INN.
         */
        public String getOwnerInn() { return ownerInn; }

        /**
         * Sets the owner's INN.
         * @param ownerInn The owner's INN.
         */
        public void setOwnerInn(String ownerInn) { this.ownerInn = ownerInn; }

        /**
         * Gets the participant's INN.
         * @return The participant's INN.
         */
        public String getParticipantInn() { return participantInn; }

        /**
         * Sets the participant's INN.
         * @param participantInn The participant's INN.
         */
        public void setParticipantInn(String participantInn) { this.participantInn = participantInn; }

        /**
         * Gets the producer's INN.
         * @return The producer's INN.
         */
        public String getProducerInn() { return producerInn; }

        /**
         * Sets the producer's INN.
         * @param producerInn The producer's INN.
         */
        public void setProducerInn(String producerInn) { this.producerInn = producerInn; }

        /**
         * Gets the production date.
         * @return The production date in "yyyy-MM-dd" format.
         */
        public String getProductionDate() { return productionDate; }

        /**
         * Sets the production date.
         * @param productionDate The production date in "yyyy-MM-dd" format.
         */
        public void setProductionDate(String productionDate) { this.productionDate = productionDate; }

        /**
         * Gets the production type.
         * @return The production type (e.g., "OWN_PRODUCTION").
         */
        public String getProductionType() { return productionType; }

        /**
         * Sets the production type.
         * @param productionType The production type.
         */
        public void setProductionType(String productionType) { this.productionType = productionType; }

        /**
         * Gets the array of products.
         * @return The array of products.
         */
        public Product[] getProducts() { return products; }

        /**
         * Sets the array of products.
         * @param products The array of products.
         */
        public void setProducts(Product[] products) { this.products = products; }

        /**
         * Gets the registration date.
         * @return The registration date.
         */
        public String getRegDate() { return regDate; }

        /**
         * Sets the registration date.
         * @param regDate The registration date.
         */
        public void setRegDate(String regDate) { this.regDate = regDate; }

        /**
         * Gets the registration number.
         * @return The registration number.
         */
        public String getRegNumber() { return regNumber; }

        /**
         * Sets the registration number.
         * @param regNumber The registration number.
         */
        public void setRegNumber(String regNumber) { this.regNumber = regNumber; }
    }

    /**
     * Represents the nested 'description' object within the Document.
     * Contains the participant's INN.
     */
    public static class Description {
        @JsonProperty("participantInn")
        private String participantInn;

        /**
         * Gets the participant's INN.
         * @return The participant's INN.
         */
        public String getParticipantInn() { return participantInn; }

        /**
         * Sets the participant's INN.
         * @param participantInn The participant's INN.
         */
        public void setParticipantInn(String participantInn) { this.participantInn = participantInn; }
    }

    /**
     * Represents a single product within the Document.
     * Maps to the 'products' array in the JSON structure.
     */
    public static class Product {
        @JsonProperty("certificate_document")
        private String certificateDocument;
        @JsonProperty("certificate_document_date")
        private String certificateDocumentDate;
        @JsonProperty("certificate_document_number")
        private String certificateDocumentNumber;
        @JsonProperty("owner_inn")
        private String ownerInn;
        @JsonProperty("producer_inn")
        private String producerInn;
        @JsonProperty("production_date")
        private String productionDate;
        @JsonProperty("tnved_code")
        private String tnvedCode;
        @JsonProperty("uit_code")
        private String uitCode;
        @JsonProperty("uitu_code")
        private String uituCode;

        /**
         * Gets the certificate document type.
         * @return The certificate document type.
         */
        public String getCertificateDocument() { return certificateDocument; }

        /**
         * Sets the certificate document type.
         * @param certificateDocument The certificate document type.
         */
        public void setCertificateDocument(String certificateDocument) { this.certificateDocument = certificateDocument; }

        /**
         * Gets the certificate document date.
         * @return The certificate document date.
         */
        public String getCertificateDocumentDate() { return certificateDocumentDate; }

        /**
         * Sets the certificate document date.
         * @param certificateDocumentDate The certificate document date.
         */
        public void setCertificateDocumentDate(String certificateDocumentDate) { this.certificateDocumentDate = certificateDocumentDate; }

        /**
         * Gets the certificate document number.
         * @return The certificate document number.
         */
        public String getCertificateDocumentNumber() { return certificateDocumentNumber; }

        /**
         * Sets the certificate document number.
         * @param certificateDocumentNumber The certificate document number.
         */
        public void setCertificateDocumentNumber(String certificateDocumentNumber) { this.certificateDocumentNumber = certificateDocumentNumber; }

        /**
         * Gets the owner's INN.
         * @return The owner's INN.
         */
        public String getOwnerInn() { return ownerInn; }

        /**
         * Sets the owner's INN.
         * @param ownerInn The owner's INN.
         */
        public void setOwnerInn(String ownerInn) { this.ownerInn = ownerInn; }

        /**
         * Gets the producer's INN.
         * @return The producer's INN.
         */
        public String getProducerInn() { return producerInn; }

        /**
         * Sets the producer's INN.
         * @param producerInn The producer's INN.
         */
        public void setProducerInn(String producerInn) { this.producerInn = producerInn; }

        /**
         * Gets the production date for this specific product.
         * @return The production date.
         */
        public String getProductionDate() { return productionDate; }

        /**
         * Sets the production date for this specific product.
         * @param productionDate The production date.
         */
        public void setProductionDate(String productionDate) { this.productionDate = productionDate; }

        /**
         * Gets the TNVED code (10-digit commodity code).
         * @return The TNVED code.
         */
        public String getTnvedCode() { return tnvedCode; }

        /**
         * Sets the TNVED code.
         * @param tnvedCode The TNVED code.
         */
        public void setTnvedCode(String tnvedCode) { this.tnvedCode = tnvedCode; }

        /**
         * Gets the Unique Identification Tag (UIT) for the product.
         * @return The UIT code. Mandatory if `uituCode` is not set.
         */
        public String getUitCode() { return uitCode; }

        /**
         * Sets the Unique Identification Tag (UIT) for the product.
         * @param uitCode The UIT code.
         */
        public void setUitCode(String uitCode) { this.uitCode = uitCode; }

        /**
         * Gets the Unique Identification Tag for the Unit (UITU) for the packaging.
         * @return The UITU code. Mandatory if `uitCode` is not set.
         */
        public String getUituCode() { return uituCode; }

        /**
         * Sets the Unique Identification Tag for the Unit (UITU) for the packaging.
         * @param uituCode The UITU code.
         */
        public void setUituCode(String uituCode) { this.uituCode = uituCode; }
    }

    /**
     * An internal class representing the structure of the request body
     * for the `/api/v3/lk/documents/create` endpoint.
     * This is the wrapper that contains the base64-encoded document and signature.
     */
    private static class CreateDocumentRequest {
        private String document_format;
        private String product_document;
        private String signature;
        private String type;

        /**
         * Constructs a new CreateDocumentRequest.
         * @param document_format The document format (e.g., "MANUAL" for JSON).
         * @param product_document The base64-encoded JSON string of the Document.
         * @param signature The detached digital signature in base64.
         * @param type The document type (e.g., "LP_INTRODUCE_GOODS").
         */
        public CreateDocumentRequest(String document_format, String product_document, String signature, String type) {
            this.document_format = document_format;
            this.product_document = product_document;
            this.signature = signature;
            this.type = type;
        }

        // Getters for Jackson serialization.

        /**
         * Gets the document format.
         * @return The document format.
         */
        public String getDocument_format() { return document_format; }

        /**
         * Gets the base64-encoded product document.
         * @return The base64-encoded product document.
         */
        public String getProduct_document() { return product_document; }

        /**
         * Gets the detached digital signature.
         * @return The detached digital signature in base64.
         */
        public String getSignature() { return signature; }

        /**
         * Gets the document type.
         * @return The document type.
         */
        public String getType() { return type; }
    }

    // ----------------------------
    // Instance Fields
    // These are the core components that make the class work.
    // ----------------------------

    private final Semaphore semaphore; // Controls the rate of requests.
    private final ScheduledExecutorService scheduler; // Periodically replenishes the semaphore.
    private final HttpClient httpClient; // The HTTP client for making API calls.
    private final ObjectMapper objectMapper; // Converts Java objects to/from JSON.
    private String authToken; // The Bearer token for API authentication.

    // ----------------------------
    // Constructor
    // Initializes the rate limiter and other components.
    // ----------------------------

    /**
     * Constructs a new CrptApi instance with a specified rate limit.
     * <p>
     * This constructor initializes a rate limiter that allows a maximum of `requestLimit`
     * requests per `timeUnit`. If the limit is exceeded, subsequent calls to `createDocument`
     * will block until the next time window opens and permits are replenished.
     * </p>
     *
     * @param timeUnit     The time unit for the rate limit (e.g., SECONDS, MINUTES).
     * @param requestLimit The maximum number of requests allowed per time unit. Must be positive.
     * @throws IllegalArgumentException if `requestLimit` is zero or negative.
     */
    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        if (requestLimit <= 0) {
            throw new IllegalArgumentException("Request limit must be positive");
        }

        // Initialize the semaphore with the specified request limit.
        this.semaphore = new Semaphore(requestLimit);
        // Create a single-threaded scheduler for periodic tasks.
        this.scheduler = Executors.newScheduledThreadPool(1);
        // Build the HTTP client with a 10-second connect timeout.
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        // Configure the ObjectMapper to handle Java 8 date/time and disable timestamp serialization.
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Schedule a task to replenish the semaphore's permits every 'timeUnit'.
        // This is a simple, fixed-window rate limiter.
        long periodMillis = timeUnit.toMillis(1); // Convert timeUnit to milliseconds.
        this.scheduler.scheduleAtFixedRate(() -> {
            int available = semaphore.availablePermits();
            // Release enough permits to bring the total back up to 'requestLimit'.
            semaphore.release(requestLimit - available);
        }, periodMillis, periodMillis, TimeUnit.MILLISECONDS);
    }

    // ----------------------------
    // Public Methods
    // The API exposed to the user of this class.
    // ----------------------------

    /**
     * Sets the authentication token required for API calls.
     * <p>
     * This token must be obtained by the user through the API's authentication process
     * (calling /api/v3/auth/cert/key and then /api/v3/auth/cert/).
     * The token is valid for 10 hours.
     * </p>
     *
     * @param token The Bearer token for authorization.
     */
    public void setAuthToken(String token) {
        this.authToken = token;
    }

    /**
     * Creates a document for introducing goods into circulation in the Russian Federation.
     * <p>
     * This method is thread-safe and will block if the rate limit is exceeded until a permit is available.
     * It sends an HTTP POST request to the `/api/v3/lk/documents/create` endpoint.
     * </p>
     *
     * @param document    The populated Document object containing the data for the "LP_INTRODUCE_GOODS" document.
     * @param signature   The detached digital signature (УКЭП) in base64 format.
     * @param productGroup The product group identifier (e.g., "shoes", "clothes").
     * @throws IllegalArgumentException if `document`, `signature`, or `productGroup` is null or empty.
     * @throws IllegalStateException if the auth token has not been set via `setAuthToken`.
     * @throws Exception If the HTTP request to the API fails (e.g., network error, non-200 status code).
     */
    public void createDocument(Document document, String signature, String productGroup) throws Exception {
        // Validate input parameters.
        if (document == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }
        if (signature == null || signature.trim().isEmpty()) {
            throw new IllegalArgumentException("Signature cannot be null or empty");
        }
        if (productGroup == null || productGroup.trim().isEmpty()) {
            throw new IllegalArgumentException("Product group cannot be null or empty");
        }
        if (authToken == null || authToken.trim().isEmpty()) {
            throw new IllegalStateException("Auth token is not set. Call setAuthToken() first.");
        }

        // Acquire a permit from the semaphore. This will block if no permits are available.
        semaphore.acquire();

        try {
            // Step 1: Serialize the Document object to a JSON string.
            String documentJson = objectMapper.writeValueAsString(document);
            // Step 2: Encode the JSON string to Base64, as required by the API.
            String documentBase64 = java.util.Base64.getEncoder()
                    .encodeToString(documentJson.getBytes(StandardCharsets.UTF_8));

            // Step 3: Create the wrapper request object for the /create endpoint.
            CreateDocumentRequest requestBody = new CreateDocumentRequest(
                    "MANUAL", // Document format is JSON.
                    documentBase64, // The base64-encoded document.
                    signature, // The provided signature.
                    "LP_INTRODUCE_GOODS" // The document type.
            );

            // Step 4: Serialize the wrapper request to JSON.
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);

            // Step 5: Build the HTTP POST request.
            HttpRequest request = HttpRequest.newBuilder()
                    // Construct the full URL with the product group as a query parameter.
                    .uri(URI.create("https://ismp.crpt.ru/api/v3/lk/documents/create?pg=" + productGroup))
                    // Add the required Authorization header.
                    .header("Authorization", "Bearer " + authToken)
                    // Specify that we are sending JSON.
                    .header("Content-Type", "application/json")
                    // Set the request method and body.
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson, StandardCharsets.UTF_8))
                    .build();

            // Step 6: Send the HTTP request and wait for the response.
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Step 7: Check the HTTP status code.
            if (response.statusCode() != 200) {
                // If not successful, throw an exception with the error details.
                throw new RuntimeException("API request failed with status: " + response.statusCode() + ", body: " + response.body());
            }

            // If we reach here, the request was successful.
            System.out.println("Document created successfully. Response: " + response.body());

        } finally {
            // The semaphore is replenished by the scheduled task, not here.
            // This ensures the fixed-window behavior.
            // In a more sophisticated rate limiter (like Guava's), you would release the permit here.
        }
    }

    /**
     * Shuts down the internal scheduler.
     * <p>
     * This method should be called when the application is terminating to release resources
     * associated with the scheduled task that replenishes the rate limiter.
     * It is good practice but not strictly required for the core functionality.
     * </p>
     */
    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }
          }
