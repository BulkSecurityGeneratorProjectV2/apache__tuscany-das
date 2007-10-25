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
package org.apache.tuscany.das.rdb.test;

/*
 * 
 * 
 */

import java.sql.SQLException;

import org.apache.tuscany.das.rdb.Command;
import org.apache.tuscany.das.rdb.ConfigHelper;
import org.apache.tuscany.das.rdb.DAS;
import org.apache.tuscany.das.rdb.config.Config;
import org.apache.tuscany.das.rdb.config.Relationship;
import org.apache.tuscany.das.rdb.config.wrapper.MappingWrapper;
import org.apache.tuscany.das.rdb.test.data.CompanyData;
import org.apache.tuscany.das.rdb.test.data.CompanyEmpData;
import org.apache.tuscany.das.rdb.test.data.CustomerData;
import org.apache.tuscany.das.rdb.test.data.DepEmpData;
import org.apache.tuscany.das.rdb.test.data.DepartmentData;
import org.apache.tuscany.das.rdb.test.data.EmployeeData;
import org.apache.tuscany.das.rdb.test.data.OrderData;
import org.apache.tuscany.das.rdb.test.framework.DasTest;

import commonj.sdo.DataObject;
import commonj.sdo.helper.XMLHelper;

public class RelationshipTests extends DasTest {

    protected void setUp() throws Exception {
        super.setUp();

        new CustomerData(getAutoConnection()).refresh();
        new OrderData(getAutoConnection()).refresh();

        new CompanyData(getAutoConnection()).refresh();
        new EmployeeData(getAutoConnection()).refresh();
        new DepartmentData(getAutoConnection()).refresh();
        new CompanyEmpData(getAutoConnection()).refresh();
        new DepEmpData(getAutoConnection()).refresh();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test ability to read a compound graph
     */
    public void testRead() throws Exception {

        String statement = "SELECT * FROM CUSTOMER LEFT JOIN ANORDER "
                + "ON CUSTOMER.ID = ANORDER.CUSTOMER_ID WHERE CUSTOMER.ID = 1";

        DAS das = DAS.FACTORY.createDAS(getConfig("customerOrderRelationshipMapping.xml"), getConnection());
        // Read some customers and related orders
        Command select = das.createCommand(statement);

        DataObject root = select.executeQuery();
        DataObject customer = root.getDataObject("Customer[1]");
        assertEquals(2, customer.getList("orders").size());

    }

    /**
     * Same as above except uses xml file for relationhip and key information.
     * Employs CUD generation.
     */
    public void testRelationshipModification2() throws Exception {

        DAS das = DAS.FACTORY.createDAS(getConfig("basicCustomerOrderMapping.xml"), getConnection());
        // Read some customers and related orders
        Command select = das
                .createCommand("SELECT * FROM CUSTOMER LEFT JOIN ANORDER ON CUSTOMER.ID = ANORDER.CUSTOMER_ID");

        DataObject root = select.executeQuery();
        
        DataObject cust1 = root.getDataObject("CUSTOMER[1]");
        DataObject cust2 = root.getDataObject("CUSTOMER[2]");

        // Save IDs
        Integer cust1ID = (Integer) cust1.get("ID");
        Integer cust2ID = (Integer) cust2.get("ID");
        // save order count
        Integer cust1OrderCount = new Integer(cust1.getList("orders").size());
        Integer cust2OrderCount = new Integer(cust2.getList("orders").size());

        // Move an order to cust1 from cust2
        DataObject order = (DataObject) cust2.getList("orders").get(0);
        cust1.getList("orders").add(order);

        // Flush changes
        das.applyChanges(root);

        // verify cust1 relationship updates
        select = das
                .createCommand("SELECT * FROM CUSTOMER LEFT JOIN ANORDER ON "
                        + "CUSTOMER.ID = ANORDER.CUSTOMER_ID where CUSTOMER.ID = ?");
        select.setParameter(1, cust1ID);

        root = select.executeQuery();
        assertEquals(cust1OrderCount.intValue() + 1, root.getList("CUSTOMER[1]/orders").size());

        // verify cust2 relationship updates
        select.setParameter(1, cust2ID);
        root = select.executeQuery();
        assertEquals(cust2OrderCount.intValue() - 1, root.getList("CUSTOMER[1]/orders").size());

    }
    
    /**
     * This scenario uses union to simmulate full outer join
     * The resulted graph will have departments without employees, and employees without departments
     * And this testcase will modify the relationship between these entities and assign the employees to the department
     * 
     * @throws Exception
     */
    public void testSimulateFullOuterJoinRelationshipModification() throws Exception {

        DAS das = DAS.FACTORY.createDAS(getConfig("companyMappingWithResultDescriptor.xml"), getConnection());
        // Read some customers and related orders
        
        Command select = das.getCommand("testFullOuterJoinRelationship");
        DataObject root = select.executeQuery();

        DataObject department = root.getDataObject("DEPARTMENT[NAME='New Technologies']"); //department with no employees
                
        DataObject emp1 = root.getDataObject("EMPLOYEE[NAME='Mary Smith']"); //employee not assgned to department
        DataObject emp2 = root.getDataObject("EMPLOYEE[NAME='John Smith']"); //employee not assgned to department
        
        department.getList("employees").add(emp1);
        department.getList("employees").add(emp2);

        das.applyChanges(root);

        //verify cust1 relationship updates
        select = das.getCommand("testEmployeesFromDepartment");
        select.setParameter(1, "New Technologies" );

        root = select.executeQuery();
        assertEquals(2, root.getDataObject("DEPARTMENT[NAME='New Technologies']").getList("employees").size());

        

    }
    
    public void testFKBehavior() throws SQLException {

        DAS das = DAS.FACTORY.createDAS(getConfig("basicCustomerOrderMapping.xml"), getConnection());
        // Read some customers and related orders
        Command select = das
                .createCommand("SELECT * FROM CUSTOMER LEFT JOIN ANORDER ON CUSTOMER.ID = ANORDER.CUSTOMER_ID");

        DataObject root = select.executeQuery();

        DataObject cust1 = root.getDataObject("CUSTOMER[1]");
        DataObject cust2 = root.getDataObject("CUSTOMER[2]");

        // Save IDs
        Integer cust1ID = (Integer) cust1.get("ID");
        
        // Move an order to cust1 from cust2
        DataObject order = (DataObject) cust2.getList("orders").get(0);
        cust1.getList("orders").add(order);
        order.setInt("CUSTOMER_ID", cust1ID.intValue());
       
        try {
            das.applyChanges(root);
            fail("An exception should be thrown");
        } catch (RuntimeException ex) {
            assertEquals("Foreign key properties should not be set when the corresponding relationship has changed", ex.getMessage());
        }

    }
    
    public void testFKBehavior2() throws SQLException {
        DAS das = DAS.FACTORY.createDAS(getConfig("basicCustomerOrderMapping.xml"), getConnection());
        // Read some customers and related orders
        Command select = das
                .createCommand("SELECT * FROM CUSTOMER LEFT JOIN ANORDER ON CUSTOMER.ID = ANORDER.CUSTOMER_ID");

        DataObject root = select.executeQuery();

        DataObject cust1 = root.getDataObject("CUSTOMER[1]");     

        // Save IDs
        Integer cust1ID = (Integer) cust1.get("ID");
        
        // Move an order to cust1 from cust2
        DataObject order = root.createDataObject("ANORDER");
        order.setInt("ID", 500);
        order.setInt("CUSTOMER_ID", cust1ID.intValue());
        cust1.getList("orders").add(order);       
       
        try {
            das.applyChanges(root);
            fail("An exception should be thrown");
        } catch (RuntimeException ex) {
            assertEquals("Foreign key properties should not be set when the corresponding relationship has changed", ex.getMessage());
        }
    }
    
    public void testInvalidFKColumn() throws SQLException {
        ConfigHelper helper = new ConfigHelper();
        Relationship r = helper.addRelationship("CUSTOMER.ID", "ANORDER.CUSTOMER_ID_INVALID");
        r.setName("orders");
       

        DAS das = DAS.FACTORY.createDAS(helper.getConfig(), getConnection());
        Command select = das.createCommand("select * from CUSTOMER left join ANORDER "
                + "ON CUSTOMER.ID = ANORDER.CUSTOMER_ID");

        DataObject root = select.executeQuery();  
        DataObject cust1 = root.getDataObject("CUSTOMER[1]");  
        DataObject order = root.createDataObject("ANORDER");
        order.setInt("ID", 500);
        cust1.getList("orders").add(order);   
        try {
            das.applyChanges(root);
        } catch (RuntimeException ex) {
            assertEquals("Invalid foreign key column: CUSTOMER_ID_INVALID", ex.getMessage());
        }
    }
    
    /*If <Table> is present in DAS Config with type and property mappings, with tableName/typeName and columnName/propertyName differing
     * consider relationship with proper mapping*/
    public void testRelationshipTypesAndProperties() throws Exception {
    	//existing records
        DAS das = DAS.FACTORY.createDAS(getConfig("CustomersOrdersRelationship.xml"), getConnection());
        Command cmd = das.getCommand("customer and orders");
        cmd.setParameter("ID", new Integer(1));
        DataObject root = cmd.executeQuery();
        DataObject firstCustomer = root.getDataObject("Customer[id=1]");
        
        DataObject newOrder = root.createDataObject("AnOrder");
        newOrder.setInt("OrderId", 100);
        newOrder.setString("PRODUCT", "MyProd");
        
        firstCustomer.getList("orders").add(newOrder);
        
        das.applyChanges(root);
        
        root = cmd.executeQuery();
        firstCustomer = root.getDataObject("Customer[id=1]");
        assertEquals(3, firstCustomer.getList("orders").size());
    }
    
    /*same as testRelationshipTypesAndProperties(), except no DAS Config, and so table/type and column/property name match by default*/
    public void testRelationshipWithProgrammaticConfig() throws Exception {
    	//existing records
    	ConfigHelper configHelper = new ConfigHelper();
    	Config config = configHelper.getConfig();
    	MappingWrapper wrapper = new MappingWrapper(config);
    	wrapper.addRelationship("CUSTOMER.ID", "ANORDER.CUSTOMER_ID", "orders");
    	
        DAS das = DAS.FACTORY.createDAS(config, getConnection());
        Command cmd = das.createCommand("select * from CUSTOMER left join ANORDER on CUSTOMER.ID = ANORDER.CUSTOMER_ID where CUSTOMER.ID = 1");
        DataObject root = cmd.executeQuery();
        DataObject firstCustomer = root.getDataObject("CUSTOMER[ID=1]");
        
        DataObject newOrder = root.createDataObject("ANORDER");
        newOrder.setInt("ID", 100);
        newOrder.setString("PRODUCT", "MyProd");
        
        firstCustomer.getList("orders").add(newOrder);
        
        das.applyChanges(root);
        
        root = cmd.executeQuery();
        firstCustomer = root.getDataObject("CUSTOMER[ID=1]");
    }    
}
