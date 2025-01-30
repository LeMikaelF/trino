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

import io.airlift.configuration.Config;
import io.trino.plugin.jdbc.BaseJdbcConfig;
import jakarta.validation.constraints.NotNull;

public class DatabricksJdbcConfig extends BaseJdbcConfig {

    @NotNull
    private String clientId;

    @NotNull
    private String clientSecret;
    @NotNull
    private String baseUrl;

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    @Config("client-id")
    public void setClientId(@NotNull String clientId) {
        this.clientId = clientId;
    }

    @Config("client-secret")
    public void setClientSecret(@NotNull String clientSecret) {
        this.clientSecret = clientSecret;
    }

    @Config("base-url")
    public void setBaseUrl(@NotNull String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
