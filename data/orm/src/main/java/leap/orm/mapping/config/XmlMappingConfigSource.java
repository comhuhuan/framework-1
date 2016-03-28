/*
 * Copyright 2016 the original author or authors.
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
package leap.orm.mapping.config;

import leap.core.AppConfig;
import leap.core.AppConfigException;
import leap.core.AppContextInitializer;
import leap.core.annotation.Inject;
import leap.core.el.EL;
import leap.db.model.DbColumnBuilder;
import leap.lang.Enums;
import leap.lang.Strings;
import leap.lang.expression.Expression;
import leap.lang.io.IO;
import leap.lang.jdbc.JdbcType;
import leap.lang.jdbc.JdbcTypes;
import leap.lang.resource.Resource;
import leap.lang.resource.Resources;
import leap.lang.xml.XML;
import leap.lang.xml.XmlReader;
import leap.orm.OrmContext;
import leap.orm.domain.DomainConfigException;
import leap.orm.mapping.FieldMappingBuilder;
import leap.orm.mapping.MappingConfigException;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class XmlMappingConfigSource implements MappingConfigSource,MappingConfig {

    private static final String MODELS_ELEMENT             = "models";
    private static final String IMPORT_ELEMENT             = "import";
    private static final String GLOBAL_FIELD_ELEMENT       = "global-field";
    private static final String INCLUDED_ENTITIES_ELEMENT  = "included-entities";
    private static final String EXCLUDED_ENTITIES_ELEMENT  = "excluded-entities";
    private static final String DEFAULT_OVERRIDE_ATTRIBUTE = "default-override";
    private static final String CHECK_EXISTENCE_ATTRIBUTE  = "check-existence";
    private static final String MAPPING_STRATEGY_ATTRIBUTE = "mapping-strategy";
    private static final String RESOURCE_ATTRIBUTE         = "resource";
    private static final String NAME_ATTRIBUTE             = "name";
    private static final String COLUMN_ATTRIBUTE           = "column";
    private static final String JDBC_TYPE_ATTRIBUTE        = "jdbc-type";
    private static final String NULLABLE_ATTRIBUTE         = "nullable";
    private static final String LENGTH_ATTRIBUTE           = "length";
    private static final String PRECISION_ATTRIBUTE        = "precision";
    private static final String SCALE_ATTRIBUTE            = "scale";
    private static final String DEFAULT_VALUE_ATTRIBUTE    = "default-value";
    private static final String INSERT_ATTRIBUTE           = "insert";
    private static final String UPDATE_ATTRIBUTE           = "update";
    private static final String WHERE_ATTRIBUTE            = "where";
    private static final String INSERT_VALUE_ATTRIBUTE     = "insert-value";
    private static final String UPDATE_VALUE_ATTRIBUTE     = "update-value";
    private static final String WHERE_VALUE_ATTRIBUTE      = "where-value";
    private static final String OVERRIDE_ATTRIBUTE         = "override";

    protected @Inject AppConfig appConfig;

    private final Set<GlobalFieldMappingConfig> globalFields = new LinkedHashSet<>();

    private boolean loaded = false;

    @Override
    public Set<GlobalFieldMappingConfig> getGlobalFields() {
        return globalFields;
    }

    @Override
    public MappingConfig load(OrmContext context) {
        if(!loaded) {
            load(context, AppContextInitializer.searchClasspathXmlResourcesForFramework("models"));
            loaded = true;
        }
        return this;
    }

    protected void load(OrmContext context, Resource[] rs) {
        load(new LoadContext(context), false, rs);
    }

    protected void load(LoadContext context, boolean defaultOverride, Resource... resources) {
        for(Resource resource : resources){

            if(resource.isReadable() && resource.exists()){
                XmlReader reader = null;
                try{
                    String resourceUrl = resource.getURL().toString();

                    if(context.resources.contains(resourceUrl)){
                        throw new AppConfigException("Cyclic importing detected, please check your config : " + resourceUrl);
                    }

                    context.resources.add(resourceUrl);

                    reader = XML.createReader(resource);
                    reader.setPlaceholderResolver(appConfig.getPlaceholderResolver());

                    loadModels(context,resource,reader, false);
                }catch(RuntimeException e){
                    throw e;
                }catch(Exception e){
                    throw new MappingConfigException("Error loading models from 'classpath:" + resource.getClasspath() + "', msg : " + e.getMessage(),e);
                }finally{
                    IO.close(reader);
                }
            }
        }
    }

    protected void loadModels(LoadContext context, Resource resource, XmlReader reader, boolean defaultOverride) {

        boolean foundValidRootElement = false;

        while(reader.next()){
            if(reader.isStartElement(MODELS_ELEMENT)){
                foundValidRootElement = true;

                Boolean defaultOverrideAttribute = reader.resolveBooleanAttribute(DEFAULT_OVERRIDE_ATTRIBUTE);
                if(null != defaultOverrideAttribute){
                    defaultOverride = defaultOverrideAttribute;
                }

                while(reader.next()){
                    if(reader.isStartElement(IMPORT_ELEMENT)){
                        boolean checkExistence    = reader.resolveBooleanAttribute(CHECK_EXISTENCE_ATTRIBUTE, true);
                        boolean override          = reader.resolveBooleanAttribute(DEFAULT_OVERRIDE_ATTRIBUTE, defaultOverride);
                        String importResourceName = reader.resolveRequiredAttribute(RESOURCE_ATTRIBUTE);

                        Resource importResource = Resources.getResource(resource,importResourceName);

                        if(null == importResource || !importResource.exists()){
                            if(checkExistence){
                                throw new DomainConfigException("the import resource '" + importResourceName + "' not exists");
                            }
                        }else{
                            load(context,override,importResource);
                            reader.nextToEndElement(IMPORT_ELEMENT);
                        }
                        continue;
                    }

                    if(reader.isStartElement(GLOBAL_FIELD_ELEMENT)) {
                        loadGlobalField(context, resource, reader, defaultOverride);
                        continue;
                    }

                    if(reader.isStartElement()) {
                        throw new MappingConfigException("Unsupported element '" + reader.getElementLocalName() + "' in file : " + resource.getClasspath());
                    }

                }

                break;
            }
        }

        if(!foundValidRootElement){
            throw new MappingConfigException("valid root element not found in file : " + resource.getClasspath());
        }
    }

    protected void loadGlobalField(LoadContext context, Resource resource, XmlReader reader, boolean defaultOverride) {
        FieldMappingBuilder  field    = readFieldMapping(reader, defaultOverride);
        FieldMappingStrategy strategy = readFieldMappingStrategy(reader);

        GlobalFieldMappingConfig gf = new GlobalFieldMappingConfig(field, strategy);

        while(reader.nextWhileNotEnd(GLOBAL_FIELD_ELEMENT)) {

            if(reader.isStartElement(INCLUDED_ENTITIES_ELEMENT)) {
                gf.addIncludedEntities(Strings.splitMultiLines(reader.getElementTextAndEnd(), ','));
                continue;
            }

            if(reader.isStartElement(EXCLUDED_ENTITIES_ELEMENT)) {
                gf.addExcludedEntities(Strings.splitMultiLines(reader.getElementTextAndEnd(), ','));
                continue;
            }
        }

        globalFields.add(gf);
    }

    protected FieldMappingBuilder readFieldMapping(XmlReader reader, boolean defaultOverride) {
        FieldMappingBuilder field  = new FieldMappingBuilder();
        DbColumnBuilder     column = new DbColumnBuilder();

        String  fieldName    = reader.resolveRequiredAttribute(NAME_ATTRIBUTE);
        String  columnName   = reader.resolveRequiredAttribute(COLUMN_ATTRIBUTE);
        String  typeName     = reader.resolveRequiredAttribute(JDBC_TYPE_ATTRIBUTE);
        Boolean nullable     = reader.resolveBooleanAttribute(NULLABLE_ATTRIBUTE);
        Integer length       = reader.resolveIntegerAttribute(LENGTH_ATTRIBUTE);
        Integer precision    = reader.resolveIntegerAttribute(PRECISION_ATTRIBUTE);
        Integer scale        = reader.resolveIntegerAttribute(SCALE_ATTRIBUTE);
        String  defaultValue = reader.resolveAttribute(DEFAULT_VALUE_ATTRIBUTE);
        Boolean insert       = reader.resolveBooleanAttribute(INSERT_ATTRIBUTE);
        Boolean update       = reader.resolveBooleanAttribute(UPDATE_ATTRIBUTE);
        Boolean where        = reader.resolveBooleanAttribute(WHERE_ATTRIBUTE);
        String  insertValue  = reader.resolveAttribute(INSERT_VALUE_ATTRIBUTE);
        String  updateValue  = reader.resolveAttribute(UPDATE_VALUE_ATTRIBUTE);
        String  whereValue   = reader.resolveAttribute(WHERE_VALUE_ATTRIBUTE);
        boolean override     = reader.resolveBooleanAttribute(OVERRIDE_ATTRIBUTE, defaultOverride);

        //field-name
        field.setFieldName(fieldName);
        field.setColumn(column);

        //column-name
        if(!Strings.isEmpty(columnName)) {
            column.setName(columnName);
            field.setColumnNameDeclared(true);
        }

        JdbcType type = null;
        if(!Strings.isEmpty(typeName)) {
            type = JdbcTypes.tryForTypeName(typeName);
            if(null == type){
                throw new DomainConfigException("Jdbc type '" + typeName + "' not supported, check the xml : " + reader.getCurrentLocation());
            }
            column.setTypeCode(type.getCode());
        }

        Expression insertValueExpression = null;
        Expression updateValueExpression = null;
        Expression whereValueExpression = null;

        if(!Strings.isEmpty(insertValue)){
            insertValueExpression = EL.tryCreateValueExpression(insertValue);
        }

        if(!Strings.isEmpty(updateValue)){
            updateValueExpression = EL.tryCreateValueExpression(updateValue);
        }

        if(!Strings.isEmpty(whereValue)){
            whereValueExpression = EL.tryCreateValueExpression(whereValue);
        }

        field.setNullable(nullable);
        field.setMaxLength(length);
        field.setPrecision(precision);
        field.setScale(scale);
        field.setDefaultValue(defaultValue);
        field.setInsert(insert);
        field.setUpdate(update);
        field.setWhere(where);
        field.setInsertValue(insertValueExpression);
        field.setUpdateValue(updateValueExpression);
        field.setWhereValue(whereValueExpression);

        return field;
    }

    protected FieldMappingStrategy readFieldMappingStrategy(XmlReader reader) {
        String v = reader.getAttribute(MAPPING_STRATEGY_ATTRIBUTE);
        if(Strings.isEmpty(v)) {
            return FieldMappingStrategy.MANDATORY;
        }else{
            return Enums.nameOf(FieldMappingStrategy.class, v);
        }
    }

    private final class LoadContext {
        private final OrmContext  context;
        private final Set<String> resources = new HashSet<>();

        private LoadContext(OrmContext context){
            this.context = context;
        }
    }

}