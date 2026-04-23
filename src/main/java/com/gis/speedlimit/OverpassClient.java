package com.gis.speedlimit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Low-level client that sends Overpass QL queries to the public Overpass API
 * and returns the parsed JSON response.
 *
 * <p>No API key is required; the public Overpass API endpoint at
 * {@code https://overpass-api.de/api/interpreter} is used by default.
 */
public class OverpassClient {

    private static final String DEFAULT_ENDPOINT = "https://overpass-api.de/api/interpreter";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    private final String endpoint;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    /** Creates a client using the default public Overpass API endpoint. */
    public OverpassClient() {
        this(DEFAULT_ENDPOINT);
    }

    /**
     * Creates a client using a custom endpoint (useful for testing with a local
     * Overpass instance).
     *
     * @param endpoint the base URL of the Overpass API interpreter
     */
    public OverpassClient(String endpoint) {
        this.endpoint = endpoint;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(DEFAULT_TIMEOUT)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Executes an Overpass QL query and returns the parsed JSON root node.
     *
     * @param query the Overpass QL query string
     * @return the parsed JSON response
     * @throws IOException          if an I/O error occurs
     * @throws InterruptedException if the request is interrupted
     */
    public JsonNode query(String query) throws IOException, InterruptedException {
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint + "?data=" + encoded))
                .timeout(DEFAULT_TIMEOUT)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Overpass API returned HTTP " + response.statusCode()
                    + ": " + response.body());
        }

        return objectMapper.readTree(response.body());
    }
}
