/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.trino.plugin.example;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.Inject;

import io.airlift.http.client.FormDataBodyBuilder;
import io.airlift.http.client.HttpClient;
import io.airlift.http.client.HttpUriBuilder;
import io.airlift.http.client.JsonResponseHandler;
import io.airlift.http.client.Request;
import io.airlift.json.JsonCodec;
import okhttp3.Credentials;

class DatabricksAccessTokenProvider implements Supplier<String> {

    /**
     * To avoid client taking a cached token that is about to expire
     */
    private static final long EXPIRY_BUFFER_SECS = 60;

    private final HttpClient httpClient;
    private final JsonCodec<DatabricksResponse> jsonCodec;
    private final URI uri;
    private final String credentials;

    private CachedAccessToken cachedAccessToken = new CachedAccessToken("", LocalDateTime.MIN);

    @Inject
    DatabricksAccessTokenProvider(
            @ForDatabricks
            HttpClient httpClient,
            DatabricksJdbcConfig config,
            JsonCodec<DatabricksResponse> jsonCodec) {
        this.httpClient = httpClient;
        this.jsonCodec = jsonCodec;
        this.uri = URI.create(config.getBaseUrl());
        this.credentials = Credentials.basic(config.getClientId(), config.getClientSecret());

        get();
    }

    @Override
    public String get() {
        if (cachedAccessToken.isExpired()) {
            synchronized (this) {
                if (cachedAccessToken.isExpired()) {
                    Request request = new Request.Builder()
                            .addHeader("Authorization", credentials)
                            .addHeader("Content-Type", "application/x-www-form-urlencoded")
                            .setUri(HttpUriBuilder.uriBuilderFrom(uri).replacePath("/oidc/v1/token").build())
                            .setBodyGenerator(new FormDataBodyBuilder()
                                    .addField("grant_type", "client_credentials")
                                    .addField("scope", "all-apis")
                                    .build()
                            )
                            .setMethod("post")
                            .build();

                    DatabricksResponse response = httpClient.execute(request, JsonResponseHandler.createJsonResponseHandler(jsonCodec));
                    cachedAccessToken = new CachedAccessToken(response.accessToken(),
                            LocalDateTime.now().plusSeconds(response.expiresInSeconds()).minusSeconds(EXPIRY_BUFFER_SECS));
                }
            }
        }
        return cachedAccessToken.accessToken();
    }

    private record CachedAccessToken(String accessToken, LocalDateTime expirationTime) {
        boolean isExpired() {
            return expirationTime.isBefore(LocalDateTime.now());
        }
    }

    public record DatabricksResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") Integer expiresInSeconds
    ) {
    }
}

