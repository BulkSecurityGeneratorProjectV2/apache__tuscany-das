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
package org.apache.tuscany.das.rdb.test.data;

import java.sql.Connection;
import java.sql.Types;

import org.apache.tuscany.das.rdb.test.framework.TestDataWithExplicitColumns;


public class DepartmentsData extends TestDataWithExplicitColumns {

    private static int[] columnTypes = {Types.INTEGER, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.INTEGER};

    private static Object[][] deptData = {{Integer.valueOf(1),"Advanced Technologies", "NY", "123", new Integer(1) },
                                          {Integer.valueOf(2),"New Technologies", "CA", "125", new Integer(2) }};

    private static String[] deptColumns = {"ID", "NAME", "LOCATION", "DEPNUMBER", "COMPANYID"};

    public DepartmentsData(Connection connection) {
        super(connection, deptData, deptColumns, columnTypes);
    }

    public String getTableName() {
        return "DEPARTMENTS";
    }

}
