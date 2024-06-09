package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class CrptApi {
    private Semaphore semaphore = new Semaphore(0);

    private final URI DOCUMENTS_CREATE_URI = new URI("https://ismp.crpt.ru/api/v3/lk/documents/create");

    public CrptApi(TimeUnit timeUnit, int requestLimit) throws URISyntaxException {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            semaphore.drainPermits();
            semaphore.release(requestLimit);}, 0, 1, timeUnit);
    }

    public HttpResponse<String> createIntroduceGoodsDocument(DocumentDTO document, String sign) throws IOException, InterruptedException {
        semaphore.acquire();

        HttpRequest request = HttpRequest
                .newBuilder(DOCUMENTS_CREATE_URI)
                .POST(HttpRequest.BodyPublishers.ofString(mapDocumentToJSON(document))).build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return response;
    }

    private String mapDocumentToJSON (DocumentDTO document) throws IOException {
        StringWriter documentJSON = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(documentJSON, document);
        return document.toString();
    }

    public static class DocumentDTO {
        public Description description;
        public String doc_id;
        public String doc_status;
        public String doc_type;
        public boolean importRequest;
        public String owner_inn;
        public String participant_inn;
        public String producer_inn;
        public String production_date;
        public String production_type;
        public ArrayList<Product> products;
        public String reg_date;
        public String reg_number;
    }

    public class Description{
        public String participantInn;
    }

    public class Product{
        public String certificate_document;
        public String certificate_document_date;
        public String certificate_document_number;
        public String owner_inn;
        public String producer_inn;
        public String production_date;
        public String tnved_code;
        public String uit_code;
        public String uitu_code;
    }
}