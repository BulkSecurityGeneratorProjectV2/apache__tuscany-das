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
package org.apache.tuscany.das.rdb.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.tuscany.das.rdb.config.Config;
import org.apache.tuscany.das.rdb.config.ResultDescriptor;

import commonj.sdo.DataObject;

public abstract class WriteCommandImpl extends CommandImpl {

    public WriteCommandImpl(String sqlString) {
        super(sqlString);
    }

    public void setResultDescriptors(List resultDescriptorList){
    	//ignore , applicable for ReadCommand only
    }
    
    public List getResultDescriptors(){
    	//ignore, applicable for ReadCommand only
    	return null;
    }
    
    public void addResultDescriptor(ResultDescriptor resultDescriptor){
    	//ignore
    }
    

    public ResultDescriptor removeResultDescriptor(int index){
    	return null;
    }
    
    public ResultDescriptor removeResultDescriptor(ResultDescriptor resultDescriptor){
    	return null;
    }
    
    public ResultDescriptor getResultDescriptor(int index){
    	return null;
    }
    
    public void printResultDescriptors(OutputStream ostrm) throws IOException{
    //ignore	
    }
    
    public void execute() {

        boolean success = false;
        try {
            basicExecute();
            success = true;
        } finally {
            if (success) {
                statement.getConnection().cleanUp();
            } else {
                statement.getConnection().errorCleanUp();
            }
        }

    }

    public void basicExecute() {
        try {
            statement.executeUpdate(parameters);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public DataObject executeQuery() {
        throw new UnsupportedOperationException();
    }

    public Config getMappingModel() {
        return configWrapper.getConfig();
    }

    public String toString() {

        StringBuffer buffer = new StringBuffer();
        buffer.append("\nSQL: " + statement.queryString);

        return buffer.toString();
    }

    public int getGeneratedKey() {
        throw new RuntimeException("No generated key is available");
    }

    public void addParameters(String parameters) {
        StringTokenizer tokenizer = new StringTokenizer(parameters);
        for (int idx = 1; tokenizer.hasMoreTokens(); idx++) {
            ParameterImpl p = new ParameterImpl();
            p.setName(tokenizer.nextToken());
            p.setIndex(idx);
            addParameter(p);
        }
    }

}
