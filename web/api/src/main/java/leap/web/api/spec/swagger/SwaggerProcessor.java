/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.web.api.spec.swagger;

import leap.core.annotation.Inject;
import leap.web.Request;
import leap.web.Response;
import leap.web.api.Api;
import leap.web.api.Apis;
import leap.web.api.config.ApiConfig;
import leap.web.api.config.ApiConfigProcessor;
import leap.web.api.meta.ApiMetadata;
import leap.web.api.meta.ApiMetadataBuilder;
import leap.web.api.meta.ApiMetadataContext;
import leap.web.api.meta.ApiMetadataProcessor;
import leap.web.api.spec.ApiSpecContext;
import leap.web.route.Route;
import leap.web.route.Routes;

public class SwaggerProcessor implements ApiConfigProcessor,ApiMetadataProcessor {
	
	private static final String SWAGGER_JSON_FILE = "swagger.json";

	protected @Inject Apis apis;

	@Override
    public void preProcess(ApiMetadataContext context, ApiMetadataBuilder m) {
		m.getPaths().remove("/" + SWAGGER_JSON_FILE);
    }

    @Override
    public void completeProcess(ApiMetadataContext context, ApiMetadata m) {
        ApiConfig config = context.getConfig();

        Routes routes = config.getContainerRoutes();

        Route route = routes.create().get(getJsonSpecPath(config), (req, resp) ->
                            handleJsonSpecRequest(context.getApi(), req, resp)).enableCors()
                            .allowAnonymous()
                            .build();

        context.getApi().getConfigurator().addDynamicRoute(route, false);
    }

    void handleJsonSpecRequest(Api api, Request req, Response resp) throws Throwable {
		SwaggerJsonWriter w = new SwaggerJsonWriter();
		w.setPropertyNamingStyle(api.getConfig().getPropertyNamingStyle());
		
		resp.setContentType(w.getContentType());
		w.write(new ApiSpecContextImpl(req), api.getMetadata(), resp.getWriter());
	}
	
	protected String getJsonSpecPath(ApiConfig c) {
		return c.getBasePath() + "/" + SWAGGER_JSON_FILE;
	}

    private static final class ApiSpecContextImpl implements ApiSpecContext {
        private final Request request;

        public ApiSpecContextImpl(Request request) {
            this.request = request;
        }

        @Override
        public String getHost() {
            return request.getServletRequest().getServerName();
        }

        @Override
        public int getPort() {
            return request.getServletRequest().getServerPort();
        }

        @Override
        public String getContextPath() {
            return request.getContextPath();
        }
    }
    
}