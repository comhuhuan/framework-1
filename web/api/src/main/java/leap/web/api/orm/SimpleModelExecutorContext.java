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

package leap.web.api.orm;

import leap.orm.dao.Dao;
import leap.orm.mapping.EntityMapping;
import leap.web.api.Api;
import leap.web.api.config.ApiConfig;
import leap.web.api.meta.ApiMetadata;
import leap.web.api.meta.model.MApiModel;

public class SimpleModelExecutorContext implements ModelExecutorContext {

    protected ApiConfig     ac;
    protected ApiMetadata   amd;
    protected MApiModel     apiModel;
    protected Dao           dao;
    protected EntityMapping entityMapping;

    public SimpleModelExecutorContext(Api api, MApiModel am, Dao dao, EntityMapping em) {
        this(api.getConfig(), api.getMetadata(), am, dao, em);
    }

    public SimpleModelExecutorContext(ApiConfig ac, ApiMetadata amd, MApiModel am, Dao dao, EntityMapping em) {
        this.ac = ac;
        this.amd = amd;
        this.apiModel = am;
        this.dao = dao;
        this.entityMapping = em;
    }

    @Override
    public ApiConfig getApiConfig() {
        return ac;
    }

    @Override
    public ApiMetadata getApiMetadata() {
        return amd;
    }

    @Override
    public MApiModel getApiModel() {
        return apiModel;
    }

    @Override
    public Dao getDao() {
        return dao;
    }

    public void setDao(Dao dao) {
        this.dao = dao;
    }

    @Override
    public EntityMapping getEntityMapping() {
        return entityMapping;
    }

}
