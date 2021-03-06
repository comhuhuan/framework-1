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
package app1.controllers;

import leap.lang.http.HTTP;
import leap.web.WebTestCase;

import leap.web.cors.CorsHandler;
import org.junit.Test;

public class HomeControllerTest extends WebTestCase{
    
    @Test
    public void testIndex() {
        get("/app1/?s=hello").assertContentEquals("hello");
        get("/app1/index?s=hello").assertContentEquals("hello");
    }

	@Test
	public void testFilter() {
		get("/app1/test_filter").assertContentEquals("/app1");
	}

    @Test
    public void testCors() {
        request(HTTP.Method.OPTIONS,"/app1")
                .addHeader(CorsHandler.REQUEST_HEADER_ORIGIN, "")
                .send()
                .assertForbidden();

        request(HTTP.Method.OPTIONS,"/app1")
                .addHeader(CorsHandler.REQUEST_HEADER_ORIGIN, "example.com")
                .send()
                .assertOk()
                .assertHeaderEquals(CorsHandler.RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_ORIGIN, "example.com");
    }
}
