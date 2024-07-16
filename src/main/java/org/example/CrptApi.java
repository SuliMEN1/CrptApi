package org.example;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class CrptApi {

    private final TimeUnit timeUnit;
    private final int requestLimit;
    private final BlockingQueue<Long> requestTimes;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        this.requestLimit = requestLimit;
        this.requestTimes = new LinkedBlockingQueue<>(requestLimit);
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
    }

    public synchronized void createDocument(Document document) throws IOException, InterruptedException {
        long currentTime = System.nanoTime();
        long timeWindow = timeUnit.toNanos(1);

        // Remove expired requests
        while (!requestTimes.isEmpty() && currentTime - requestTimes.peek() > timeWindow) {
            requestTimes.poll();
        }

        // If limit is reached, wait until there is room
        while (requestTimes.size() >= requestLimit) {
            long waitTime = timeWindow - (currentTime - requestTimes.peek());
            if (waitTime > 0) {
                wait(waitTime / 1_000_000, (int) (waitTime % 1_000_000));
            }
            currentTime = System.nanoTime();
        }

        // Add current request time
        requestTimes.add(currentTime);

        // Create JSON payload
        String jsonPayload = objectMapper.writeValueAsString(document);

        // Create HTTP POST request
        var httpPost = new HttpPost("https://ismp.crpt.ru/api/v3/lk/documents/create");
        httpPost.setEntity(new StringEntity(jsonPayload, ContentType.APPLICATION_JSON));

        // Execute request
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            // Process the response if needed
            System.out.println("Response status: " + response.getStatusLine().getStatusCode());
        }
    }

    public static class Document {

        private Description description;
        @JsonProperty("doc_id")
        private String docId;
        @JsonProperty("doc_status")
        private String docStatus;
        @JsonProperty("doc_type")
        private String docType;
        private boolean importRequest;
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
        private List<Product> products;
        @JsonProperty("reg_date")
        private String regDate;
        @JsonProperty("reg_number")
        private String regNumber;

        private Document(Builder builder) {
            this.description = builder.description;
            this.docId = builder.docId;
            this.docStatus = builder.docStatus;
            this.docType = builder.docType;
            this.importRequest = builder.importRequest;
            this.ownerInn = builder.ownerInn;
            this.participantInn = builder.participantInn;
            this.producerInn = builder.producerInn;
            this.productionDate = builder.productionDate;
            this.productionType = builder.productionType;
            this.products = builder.products;
            this.regDate = builder.regDate;
            this.regNumber = builder.regNumber;
        }

        public static class Builder {

            private Description description;
            private String docId;
            private String docStatus;
            private String docType;
            private boolean importRequest;
            private String ownerInn;
            private String participantInn;
            private String producerInn;
            private String productionDate;
            private String productionType;
            private List<Product> products;
            private String regDate;
            private String regNumber;

            public Builder description(Description description) {
                this.description = description;
                return this;
            }

            public Builder docId(String docId) {
                this.docId = docId;
                return this;
            }

            public Builder docStatus(String docStatus) {
                this.docStatus = docStatus;
                return this;
            }

            public Builder docType(String docType) {
                this.docType = docType;
                return this;
            }

            public Builder importRequest(boolean importRequest) {
                this.importRequest = importRequest;
                return this;
            }

            public Builder ownerInn(String ownerInn) {
                this.ownerInn = ownerInn;
                return this;
            }

            public Builder participantInn(String participantInn) {
                this.participantInn = participantInn;
                return this;
            }

            public Builder producerInn(String producerInn) {
                this.producerInn = producerInn;
                return this;
            }

            public Builder productionDate(String productionDate) {
                this.productionDate = productionDate;
                return this;
            }

            public Builder productionType(String productionType) {
                this.productionType = productionType;
                return this;
            }

            public Builder products(List<Product> products) {
                this.products = products;
                return this;
            }

            public Builder regDate(String regDate) {
                this.regDate = regDate;
                return this;
            }

            public Builder regNumber(String regNumber) {
                this.regNumber = regNumber;
                return this;
            }

            public Document build() {
                return new Document(this);
            }
        }

        public static class Description {

            private String participantInn;

            private Description(String participantInn) {
                this.participantInn = participantInn;
            }
        }

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

            private Product(Builder builder) {
                this.certificateDocument = builder.certificateDocument;
                this.certificateDocumentDate = builder.certificateDocumentDate;
                this.certificateDocumentNumber = builder.certificateDocumentNumber;
                this.ownerInn = builder.ownerInn;
                this.producerInn = builder.producerInn;
                this.productionDate = builder.productionDate;
                this.tnvedCode = builder.tnvedCode;
                this.uitCode = builder.uitCode;
                this.uituCode = builder.uituCode;
            }

            public static class Builder {

                private String certificateDocument;
                private String certificateDocumentDate;
                private String certificateDocumentNumber;
                private String ownerInn;
                private String producerInn;
                private String productionDate;
                private String tnvedCode;
                private String uitCode;
                private String uituCode;

                public Builder certificateDocument(String certificateDocument) {
                    this.certificateDocument = certificateDocument;
                    return this;
                }

                public Builder certificateDocumentDate(String certificateDocumentDate) {
                    this.certificateDocumentDate = certificateDocumentDate;
                    return this;
                }

                public Builder certificateDocumentNumber(String certificateDocumentNumber) {
                    this.certificateDocumentNumber = certificateDocumentNumber;
                    return this;
                }

                public Builder ownerInn(String ownerInn) {
                    this.ownerInn = ownerInn;
                    return this;
                }

                public Builder producerInn(String producerInn) {
                    this.producerInn = producerInn;
                    return this;
                }

                public Builder productionDate(String productionDate) {
                    this.productionDate = productionDate;
                    return this;
                }

                public Builder tnvedCode(String tnvedCode) {
                    this.tnvedCode = tnvedCode;
                    return this;
                }

                public Builder uitCode(String uitCode) {
                    this.uitCode = uitCode;
                    return this;
                }

                public Builder uituCode(String uituCode) {
                    this.uituCode = uituCode;
                    return this;
                }

                public Product build() {
                    return new Product(this);
                }
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        CrptApi api = new CrptApi(TimeUnit.MINUTES, 10);

        // Create sample document using builder
        Document doc = new Document.Builder()
                .description(new Document.Description("string"))
                .docId("string")
                .docStatus("string")
                .docType("LP_INTRODUCE_GOODS")
                .importRequest(true)
                .ownerInn("string")
                .participantInn("string")
                .producerInn("string")
                .productionDate("2023-01-01")
                .productionType("string")
                .products(List.of(
                        new Document.Product.Builder()
                                .certificateDocument("string")
                                .certificateDocumentDate("2023-01-01")
                                .certificateDocumentNumber("string")
                                .ownerInn("string")
                                .producerInn("string")
                                .productionDate("2023-01-01")
                                .tnvedCode("string")
                                .uitCode("string")
                                .uituCode("string")
                                .build()
                ))
                .regDate("2023-01-01")
                .regNumber("string")
                .build();

        api.createDocument(doc);
    }
}