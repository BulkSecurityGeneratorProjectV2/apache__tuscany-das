/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tuscany.das.rdb.generator.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.tuscany.das.rdb.config.Column;
import org.apache.tuscany.das.rdb.config.Table;
import org.apache.tuscany.das.rdb.config.wrapper.MappingWrapper;
import org.apache.tuscany.das.rdb.config.wrapper.RelationshipWrapper;
import org.apache.tuscany.das.rdb.config.wrapper.TableWrapper;
import org.apache.tuscany.das.rdb.impl.CollisionParameter;
import org.apache.tuscany.das.rdb.impl.ManagedParameterImpl;
import org.apache.tuscany.das.rdb.impl.OptimisticWriteCommandImpl;
import org.apache.tuscany.das.rdb.impl.ParameterImpl;
import org.apache.tuscany.das.rdb.impl.UpdateCommandImpl;
import org.apache.tuscany.das.rdb.util.LoggerFactory;

import commonj.sdo.ChangeSummary;
import commonj.sdo.DataObject;
import commonj.sdo.Property;
import commonj.sdo.Type;
import commonj.sdo.ChangeSummary.Setting;

public final class UpdateGenerator extends BaseGenerator {

    public static final UpdateGenerator INSTANCE = new UpdateGenerator();

    private final Logger logger = LoggerFactory.INSTANCE.getLogger(UpdateGenerator.class);

    private UpdateGenerator() {
        super();
    }

    public UpdateCommandImpl getUpdateCommand(MappingWrapper mapping, DataObject changedObject, Table table) {      
        List parameters = new ArrayList();
        Type type = changedObject.getType();
        TableWrapper tableWrapper = new TableWrapper(table);
        StringBuffer statement = new StringBuffer("update ");
        statement.append(table.getTableName());
        statement.append(" set ");

        ChangeSummary summary = changedObject.getDataGraph().getChangeSummary();
        List changedFields = getChangedFields(mapping, summary, changedObject, tableWrapper);
        Iterator i = changedFields.iterator();
      
        int idx = 1;
        while (i.hasNext()) {
            Property property = (Property) i.next();
            Column c = tableWrapper.getColumnByPropertyName(property.getName());          
            
            if ((c == null) || !c.isCollision() || !c.isPrimaryKey()) { 
                String columnName = c == null ? property.getName() : c.getColumnName();
                appendFieldSet(statement, idx > 1, columnName);
                parameters.add(createParameter(tableWrapper, property, idx++));
            } 
        }
        
        Column c = tableWrapper.getManagedColumn();
        if (c != null) {
            appendFieldSet(statement, idx > 1, c.getColumnName());
            String propertyName = c.getPropertyName() == null ? c.getColumnName() : c.getPropertyName();
            parameters.add(createManagedParameter(tableWrapper, 
                    changedObject.getProperty(propertyName), idx++));
        }
        
        statement.append(" where ");

        Iterator pkColumnNames = tableWrapper.getPrimaryKeyNames().iterator();
        Iterator pkPropertyNames = tableWrapper.getPrimaryKeyProperties().iterator();
        while (pkColumnNames.hasNext() && pkPropertyNames.hasNext()) {
            String columnName = (String) pkColumnNames.next();
            String propertyName = (String) pkPropertyNames.next();
            statement.append(columnName);
            statement.append(" = ?");
            if (pkColumnNames.hasNext() && pkPropertyNames.hasNext()) {
                statement.append(" and ");
            }
            parameters.add(createParameter(tableWrapper, type.getProperty(propertyName), idx++));
        }

        if (tableWrapper.getCollisionColumn() == null) {
            Iterator iter = changedFields.iterator();
            while (iter.hasNext()) {
                statement.append(" and ");
                Property changedProperty = (Property) iter.next();
                Column column = tableWrapper.getColumnByPropertyName(changedProperty.getName()); 
                statement.append(column == null ? changedProperty.getName() : column.getColumnName());
                                 
                Object value;
                Setting setting = summary.getOldValue(changedObject, changedProperty);
                // Setting is null if this is a relationship change
                if (setting == null) {
                    value = changedObject.get(changedProperty);
                } else {
                    value = setting.getValue();
                }
                
                if (value == null) {                   
                    statement.append(" is null");                    
                } else {
                    ParameterImpl param = createCollisionParameter(tableWrapper, changedProperty, idx++);
                    statement.append(" = ?");
                    param.setValue(value);
                    parameters.add(param);
                }
                
               
            }
           
        } else {
            statement.append(" and ");
            statement.append(tableWrapper.getCollisionColumn().getColumnName());
            statement.append(" = ?");
            parameters.add(createParameter(tableWrapper, 
                    type.getProperty(tableWrapper.getCollisionColumnPropertyName()), idx++));                       
        }                  

        UpdateCommandImpl updateCommand = new OptimisticWriteCommandImpl(statement.toString());
        
        Iterator params = parameters.iterator();
        while (params.hasNext()) {           
            updateCommand.addParameter((ParameterImpl) params.next());
        }
           
        if (this.logger.isDebugEnabled()) {
            this.logger.debug(statement.toString());
        }

        return updateCommand;
    }

  

    private void appendFieldSet(StringBuffer statement, boolean appendComma, String columnName) {
        if (appendComma) {
            statement.append(", ");
        }
        statement.append(columnName);
        statement.append(" = ?");
    }



    private List getChangedFields(MappingWrapper config, ChangeSummary summary, DataObject obj, TableWrapper tw) {
        List changes = new ArrayList();
        Iterator i = summary.getOldValues(obj).iterator();
        while (i.hasNext()) {
            ChangeSummary.Setting setting = (ChangeSummary.Setting) i.next();
            
            if (setting.getProperty().getType().isDataType()) {
                changes.add(setting.getProperty());
            } else {
                Property ref = setting.getProperty();
                if (!ref.isMany()) {
                    RelationshipWrapper r = new RelationshipWrapper(config.getRelationshipByReference(ref));

                    Iterator keys = r.getForeignKeys().iterator();
                    while (keys.hasNext()) {
                        String key = (String) keys.next();
                        String keyProperty = config.getColumnPropertyName(tw.getTableName(), key);
                        Property keyProp = obj.getType().getProperty(keyProperty);
                        changes.add(keyProp);
                    }
                }

            }
        }
        return changes;
    }

    private ParameterImpl fillParameter(ParameterImpl param, TableWrapper table, Property property, int idx) {
        param.setName(property.getName());
        param.setType(property.getType());
        param.setConverter(getConverter(table.getConverter(property.getName())));
        if (idx != -1) {
            param.setIndex(idx);
        }

        return param;
    }
    private ParameterImpl createCollisionParameter(TableWrapper tableWrapper, Property property, int i) {
        ParameterImpl param = new CollisionParameter();
        return fillParameter(param, tableWrapper, property, i);
    }
    
    private ParameterImpl createManagedParameter(TableWrapper table, Property property, int idx) {
        ParameterImpl param = new ManagedParameterImpl();
        return fillParameter(param, table, property, idx);
    }

    private ParameterImpl createParameter(TableWrapper table, Property property, int idx) {
        ParameterImpl param = new ParameterImpl();
        return fillParameter(param, table, property, idx);
    }

}
