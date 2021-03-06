/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.orm.tested.tag;

import leap.lang.params.Params;
import leap.orm.metadata.MetadataContext;
import leap.orm.sql.*;

import java.io.IOException;

public class NopTagProcessor implements SqlTagProcessor {

    @Override
    public String processTag(SqlContext context, Sql sql, SqlTag tag, Params params) {
        return tag.getContent();
    }

}
