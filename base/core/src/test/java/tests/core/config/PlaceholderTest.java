/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tests.core.config;

import leap.core.junit.AppTestBase;
import org.junit.Test;

public class PlaceholderTest extends AppTestBase {

    @Test
    public void testNotExistsPlaceholder() {
        assertEquals("${notExistProperty}", config.getProperty("testNotExistPlaceholder"));
    }

    @Test
    public void testPlaceholderValue() {
        assertEquals("a", config.getProperty("placeholder1"));
        assertEquals("a", config.getProperty("placeholder2"));
        assertEquals("a", config.getProperty("placeholder3"));
        assertEquals("a", config.getProperty("placeholder4"));
    }

    @Test
    public void testPlaceholderInArrayProperty() {

        String[] values = config.getArrayProperty("arrayPlaceholder1");
        assertEquals("a", values[0]);

    }

    @Test
    public void testPlaceholderInProperties() {
        assertEquals("a",   config.getProperty("placeholder.prop1"));
        assertEquals("a.b", config.getProperty("placeholder.prop2"));
    }
}

