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
package org.apache.tuscany.das.rdb.test.framework;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import junit.extensions.TestSetup;
import junit.framework.Test;

public class DatabaseSetup extends TestSetup {

    protected Statement s;

    protected String platformName = "Not initialized";

    protected String driverName = "Not initialized";

    protected String databaseURL = "Not initialized";

    protected String userName;

    protected String password;

    // Data Types
    protected String stringType = "VARCHAR";

    protected String integerType = "INT";

    protected String timestampType = "TIMESTAMP";

    protected String floatType = "FLOAT";

    protected String decimalType = "DECIMAL";

    private Connection connection;

    public DatabaseSetup(Test test) {
        super(test);
        initConnectionProtocol();
        initConnection();
        DasTest.connection = connection;
    }

    protected void initConnectionProtocol() {
        // Subclasses provide implementation
    }

    private void initConnection() {

        try {

            Class.forName(driverName).newInstance();
            if (userName != null) {
                connection = DriverManager.getConnection(databaseURL, userName, password);
            } else {
                connection = DriverManager.getConnection(databaseURL);
            }
            connection.setAutoCommit(false);

        } catch (SQLException e) {

            if (e.getNextException() != null) {
                e.getNextException().printStackTrace();
            } else {
                e.printStackTrace();
            }

            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    protected void setUp() throws Exception {

        System.out.println("Setting up for " + platformName + " run");

        s = connection.createStatement();

        try {
            dropTriggers();
            dropSequences();
            dropTables();
            dropProcedures();
            dropSchema();//JIRA-952

            createSchema();//JIRA-952
            createSequences();
            createTables();
            createTriggers();
            createProcedures();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
        }

    }

    protected void tearDown() throws Exception {

        System.out.println("Ending " + platformName + " run");
        connection.close();

    }

    //JIRA-952
    private void dropSchema(){
        String[] statements = {"DROP SCHEMA DASTEST1 RESTRICT",
        		"DROP SCHEMA DASTEST2 RESTRICT",
        		"DROP SCHEMA DASTEST3 RESTRICT",
        };
        
        for (int i = 0; i < statements.length; i++) {
            try {
                s.execute(statements[i]);
            } catch (SQLException e) {
                // If the table does not exist then ignore the exception on drop
                if ((!(e.getMessage().indexOf("does not exist") >= 0)) && (!(e.getMessage().indexOf("Unknown table") >= 0)) 
                        && (!(e.getMessage().indexOf("42704") >= 0))) {
                    throw new RuntimeException(e);
                }
            }
        }        
    }
    
    private void dropTables() {

        String[] statements = {
            "DROP TABLE CUSTOMER", "DROP TABLE ANORDER", "DROP TABLE ORDERDETAILS", 
            "DROP TABLE ORDERDETAILSDESC", "DROP TABLE ITEM", "DROP TABLE COMPANY", 
            "DROP TABLE EMPLOYEE", "DROP TABLE DEPARTMENT", "DROP TABLE BOOK", 
            "DROP TABLE PART", "DROP TABLE TYPETEST", "DROP TABLE CITIES", 
            "DROP TABLE STATES", "DROP TABLE conmgt.SERVERSTATUS", 
            "DROP TABLE DOG", "DROP TABLE OWNER", "DROP TABLE KENNEL", 
            "DROP TABLE VISIT",
            "DROP TABLE DASTEST1.CUSTOMER" ,"DROP TABLE DASTEST2.CUSTOMER","DROP TABLE DASTEST3.CUSTOMER",
            "DROP TABLE DASTEST1.CITY", "DROP TABLE DASTEST2.CITY",
            "DROP TABLE DASTEST2.ACCOUNT",
            "DROP TABLE DASTEST3.CUSTORDER",
            "DROP TABLE DASTEST3.ORDERDETAILSDESC", "DROP TABLE DASTEST1.ORDERDETAILS",
            "DROP TABLE DASTEST1.EMPLOYEE",
            "DROP TABLE SINGER",
            "DROP TABLE BANKACCOUNT",
            "DROP TABLE DEPARTMENTS",
            "DROP TABLE EMPLOYEES",
            "DROP TABLE SONG",            
            "DROP TABLE DASTEST1.EMPLOYEE",
            "DROP TABLE DOCUMENTS_IMAGES"

        };

        for (int i = 0; i < statements.length; i++) {
            try {
                s.execute(statements[i]);
            } catch (SQLException e) {
                // If the table does not exist then ignore the exception on drop
                if ((!(e.getMessage().indexOf("does not exist") >= 0)) && (!(e.getMessage().indexOf("Unknown table") >= 0))
                        && (!(e.getMessage().indexOf("42704") >= 0))) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    protected void dropTriggers() {

    }

    protected void createTriggers() {

    }

    protected void dropSequences() {

    }

    protected void createSequences() {

    }

    protected void dropProcedures() {

        // System.out.println("Dropping procedures");

        String[] statements = {

            "DROP PROCEDURE GETALLCOMPANIES", "DROP PROCEDURE DELETECUSTOMER", 
            "DROP PROCEDURE GETNAMEDCOMPANY", "DROP PROCEDURE GETCUSTOMERANDORDERS",
            "DROP PROCEDURE GETNAMEDCUSTOMERS", "DROP PROCEDURE GETALLCUSTOMERSANDORDERS"

        };

        for (int i = 0; i < statements.length; i++) {
            try {
                s.execute(statements[i]);
            } catch (SQLException e) {
                // If the proc does not exist then ignore the exception on drop
                if (!(e.getMessage().indexOf("does not exist") >= 0) && !(e.getMessage().indexOf("42704") >= 0)) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    //JIRA-952
    private void createSchema() {
    	// System.out.println("Creating schema");
    	try {

            s.execute("CREATE SCHEMA DASTEST1");
            s.execute("CREATE SCHEMA DASTEST2");
            s.execute("CREATE SCHEMA DASTEST3");
    	}catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    private void createTables() {

        // System.out.println("Creating tables");

        try {

            s.execute(getCreateCustomer());
            s.execute(getCreateAnOrder());
            s.execute(getCreateOrderDetails());
            s.execute(getCreateItem());
            s.execute(getCreateCompany());
            s.execute(getCreateEmployee());
            s.execute(getCreateDepartment());
            s.execute(getCreateBook());
            s.execute(getCreatePart());
            s.execute(getCreateTypeTest());
            s.execute(getCreateStates());
            s.execute(getCreateCities());
            s.execute(getCreateServerStatus());

            s.execute(getCreateDog());
            s.execute(getCreateOwner());
            s.execute(getCreateKennel());
            s.execute(getCreateVisit());
            s.execute(getCreateOrderDetailsDesc());//JIRA-841
            
            //JIRA-952 start
            s.execute(getCreateDASTEST1Customer());
            s.execute(getCreateDASTEST1Employee());
            s.execute(getCreateDASTEST2Customer());
            s.execute(getCreateDASTEST3Customer());
            s.execute(getCreateDASTEST1City());
            s.execute(getCreateDASTEST2City());
            s.execute(getCreateDASTEST2Account());
            s.execute(getCreateDASTEST3CustOrder());
            s.execute(getCreateDASTEST1OrderDetails());
            s.execute(getCreateDASTEST3OrderDetailsDesc());            
            //JIRA-952 end
            s.execute(getCreateSinger());
            s.execute(getCreateSong());
            s.execute(getCreateDepartments());
            s.execute(getCreateBankAccount());
            s.execute(getCreateEmployees());
            s.execute(getCreateDocumentsImages());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected void createProcedures() {

        // System.out.println("Creating procedures");
        try {

            s.execute("CREATE PROCEDURE GETALLCOMPANIES() PARAMETER STYLE JAVA LANGUAGE JAVA "
                            + "READS SQL DATA DYNAMIC RESULT SETS 1 "
                            + "EXTERNAL NAME " 
                            + "'org.apache.tuscany.das.rdb.test.framework.JavaStoredProcs.getAllCompanies'");
            s.execute("CREATE PROCEDURE DELETECUSTOMER(theId int) PARAMETER STYLE JAVA LANGUAGE JAVA " 
                    + "MODIFIES SQL DATA EXTERNAL NAME " 
                    + "'org.apache.tuscany.das.rdb.test.framework.JavaStoredProcs.deleteCustomer'");
            s.execute("CREATE PROCEDURE GETNAMEDCOMPANY(theName VARCHAR(100)) PARAMETER STYLE JAVA LANGUAGE JAVA " 
                    + "READS SQL DATA DYNAMIC RESULT SETS 1 EXTERNAL NAME " 
                    + "'org.apache.tuscany.das.rdb.test.framework.JavaStoredProcs.getNamedCompany'");
            s.execute("CREATE PROCEDURE GETCUSTOMERANDORDERS(theID INTEGER) PARAMETER STYLE JAVA LANGUAGE JAVA " 
                    + "READS SQL DATA DYNAMIC RESULT SETS 1 EXTERNAL NAME " 
                    + "'org.apache.tuscany.das.rdb.test.framework.JavaStoredProcs.getCustomerAndOrders'");
            s.execute("CREATE PROCEDURE GETNAMEDCUSTOMERS(theName VARCHAR(100), OUT theCount INTEGER) " 
                    + "PARAMETER STYLE JAVA LANGUAGE JAVA READS SQL DATA DYNAMIC RESULT SETS 1 EXTERNAL NAME " 
                    + "'org.apache.tuscany.das.rdb.test.framework.JavaStoredProcs.getNamedCustomers'");
            s.execute("CREATE PROCEDURE GETALLCUSTOMERSANDORDERS() PARAMETER STYLE JAVA LANGUAGE JAVA " 
                    + "READS SQL DATA DYNAMIC RESULT SETS 2 EXTERNAL NAME " 
                    + "'org.apache.tuscany.das.rdb.test.framework.JavaStoredProcs.getAllCustomersAndAllOrders'");
            // TODO - "GETNAMEDCUSTOMERS" is failing on DB2 with SQLCODE: 42723. Need to investigate
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //
    // This section povides methods that return strings for table creation.
    // Platform-specific sublcasses
    // can override these as necessary
    //

    protected String getCreateCustomer() {
        return "CREATE TABLE CUSTOMER (" + getIntegerColumn("ID") + " PRIMARY KEY NOT NULL, " 
            + getStringColumn("LASTNAME", 30)
                + " DEFAULT 'Garfugengheist', " + getStringColumn("ADDRESS", 30) + ")";
    }

    protected String getCreateAnOrder() {
        return "CREATE TABLE ANORDER (" + getIntegerColumn("ID") + " PRIMARY KEY NOT NULL, " 
            + getStringColumn("PRODUCT", 30) + ", "
                + getIntegerColumn("QUANTITY") + "," + getIntegerColumn("CUSTOMER_ID") + ")";
    }

    protected String getCreateOrderDetails() {
        return "CREATE TABLE ORDERDETAILS (" + getIntegerColumn("ORDERID") + " NOT NULL, " 
            + getIntegerColumn("PRODUCTID")
                + " NOT NULL, PRICE FLOAT, PRIMARY KEY (ORDERID, PRODUCTID))";
    }

    protected String getCreateItem() {
        return "CREATE TABLE ITEM (" + getIntegerColumn("ID") + " PRIMARY KEY NOT NULL, " 
            + getStringColumn("NAME", 30) + ")";
    }

    protected String getCreateCompany() {
        return "CREATE TABLE COMPANY (" + getIntegerColumn("ID") + " PRIMARY KEY NOT NULL " 
            + getGeneratedKeyClause() + " , "
                + getStringColumn("NAME", 30) + ", " + getIntegerColumn("EOTMID") + ")";
    }

    protected String getCreateEmployee() {
        return "CREATE TABLE EMPLOYEE (" + getIntegerColumn("ID") + " PRIMARY KEY NOT NULL " 
            + getGeneratedKeyClause() + ","
                + getStringColumn("NAME", 30) + "," + getStringColumn("SN", 10) + ", MANAGER SMALLINT, " 
                + getIntegerColumn("DEPARTMENTID") + ")";
    }

    protected String getCreateEmployees() {
        return "CREATE TABLE EMPLOYEES (" + getIntegerColumn("ID") + " PRIMARY KEY NOT NULL " 
            + getGeneratedKeyClause() + ","
                + getStringColumn("NAME", 30) + "," + getStringColumn("SN", 10) + ", MANAGER SMALLINT, " 
                + getIntegerColumn("DEPARTMENTID") + ")";
    }

    protected String getCreateDepartment() {
        return "CREATE TABLE DEPARTMENT (" + getIntegerColumn("ID") + " PRIMARY KEY NOT NULL " 
            + getGeneratedKeyClause() + ", "
                + getStringColumn("NAME", 30) + "," + getStringColumn("LOCATION", 30) + ", " 
                + getStringColumn("DEPNUMBER", 10) + ","
                + getIntegerColumn("COMPANYID") + ")";
    }

    protected String getCreateDepartments() {
        return "CREATE TABLE DEPARTMENTS (" + getIntegerColumn("ID") + " PRIMARY KEY NOT NULL , "
                + getStringColumn("NAME", 30) + "," + getStringColumn("LOCATION", 30) + ", " 
                + getStringColumn("DEPNUMBER", 10) + ","
                + getIntegerColumn("COMPANYID") + ")";
    }
    
    protected String getCreateBook() {
        return "CREATE TABLE BOOK (" + getIntegerColumn("BOOK_ID") + " PRIMARY KEY NOT NULL, " 
            + getStringColumn("NAME", 50) + ","
                + getStringColumn("AUTHOR", 30) + ", " + getIntegerColumn("QUANTITY") + "," 
                + getIntegerColumn("OCC") + ")";
    }

    protected String getCreatePart() {
        return "CREATE TABLE PART (" + getIntegerColumn("ID") + " PRIMARY KEY NOT NULL, NAME VARCHAR(50),  " 
            + getIntegerColumn("QUANTITY") + ","
                + getIntegerColumn("PARENT_ID") + " )";
    }

    protected String getCreateTypeTest() {
        return "CREATE TABLE TYPETEST (" + getIntegerColumn("ID") + " PRIMARY KEY NOT NULL, " 
                + getTimestampColumn("ATIMESTAMP") + ","
                + getDecimalColumn("ADECIMAL", 9, 2) + "," + getFloatColumn("AFLOAT") + ")";
    }

    protected String getCreateStates() {
        return "CREATE TABLE STATES (" + getIntegerColumn("ID") + " PRIMARY KEY NOT NULL, " 
                + getStringColumn("NAME", 2) + ")";
    }

    protected String getCreateCities() {
        return "CREATE TABLE CITIES (" + getIntegerColumn("ID") + " PRIMARY KEY NOT NULL," 
                + getStringColumn("NAME", 50) + ","
                + getIntegerColumn("STATE_ID") + "," + getForeignKeyConstraint("STATES", "ID", "STATE_ID") + ")";
    }

    protected String getCreateServerStatus() {

        return "CREATE TABLE CONMGT.SERVERSTATUS (STATUSID INTEGER PRIMARY KEY NOT NULL " + getGeneratedKeyClause()
                + "  (START WITH 1 ,INCREMENT BY 1), MANAGEDSERVERID INTEGER NOT NULL, TIMESTAMP TIMESTAMP NOT NULL)";

    }

    // Dog Kennel Schema

    protected String getCreateDog() {
        return "CREATE TABLE DOG (" + getIntegerColumn("ID") + " NOT NULL " + getGeneratedKeyClause() + " , " 
            + getIntegerColumn("OWNER_ID") + " , "
                + getStringColumn("NAME", 20) + ", " + getStringColumn("BREED", 20) + ", " 
                + getIntegerColumn("OCC_COUNT") + ", "
                + "PRIMARY KEY(ID))";
    }

    protected String getCreateOwner() {
        return "CREATE TABLE OWNER (" + getIntegerColumn("ID") + " NOT NULL " + getGeneratedKeyClause() + " , " 
            + getStringColumn("NAME", 20) + ", "
                + getStringColumn("CONTACT_PHONE", 20) + ", " + getIntegerColumn("OCC_COUNT") + ", " 
                + "PRIMARY KEY(ID))";
    }

    protected String getCreateKennel() {
        return "CREATE TABLE KENNEL (" + getIntegerColumn("ID") + " NOT NULL " + getGeneratedKeyClause() 
            + " , " + getIntegerColumn("KNUMBER") + ", "
                + getStringColumn("KIND", 20) + ", " + getIntegerColumn("OCC_COUNT") + ", " + "PRIMARY KEY(ID))";
    }

    protected String getCreateVisit() {
        return "CREATE TABLE VISIT (" + getIntegerColumn("ID") + " NOT NULL " + getGeneratedKeyClause() + " , " 
            + getTimestampColumn("CHECK_IN")
                + ", " + getTimestampColumn("CHECK_OUT") + ", " + getIntegerColumn("OCC_COUNT") + ", " 
                + "PRIMARY KEY(ID))";
    }

    protected String getCreateOrderDetailsDesc() {
        return "CREATE TABLE ORDERDETAILSDESC ("+ getIntegerColumn("ID") + " NOT NULL, " + getIntegerColumn("ORDERID") + " NOT NULL, " 
            + getIntegerColumn("PRODUCTID")
                + " NOT NULL,"+ getStringColumn("DESCR", 20)+", PRIMARY KEY (ID))";
    } 
    
    //JIRA-952 start
    protected String getCreateDASTEST1Customer() {
        return "CREATE TABLE DASTEST1.CUSTOMER (" + getIntegerColumn("ID") + " PRIMARY KEY NOT NULL, " 
            + getStringColumn("LASTNAME", 30)
                + " DEFAULT 'Garfugengheist', " + getStringColumn("ADDRESS", 30) + ")";
    }

    protected String getCreateDASTEST1Employee() {
        return "CREATE TABLE DASTEST1.EMPLOYEE (" + getIntegerColumn("ID") + " PRIMARY KEY NOT NULL, " 
            + getStringColumn("LASTNAME", 30)
                + " DEFAULT 'Garfugengheist', " + getStringColumn("ADDRESS", 30) + ")";
    }
    
    protected String getCreateDASTEST2Customer() {
        return "CREATE TABLE DASTEST2.CUSTOMER (" + getIntegerColumn("ID") + " PRIMARY KEY NOT NULL, " 
            + getStringColumn("LASTNAME", 30)
                + " DEFAULT 'Garfugengheist', " + getStringColumn("ADDRESS", 30) + ")";
    }
    
    protected String getCreateDASTEST3Customer() {
        return "CREATE TABLE DASTEST3.CUSTOMER (" + getIntegerColumn("ID") + " PRIMARY KEY NOT NULL, " 
            + getStringColumn("LASTNAME", 30)
                + " DEFAULT 'Garfugengheist', " + getStringColumn("ADDRESS", 30) + ")";
    }
    
    protected String getCreateDASTEST1City() {
        return "CREATE TABLE DASTEST1.CITY (" + getIntegerColumn("INDX") + " PRIMARY KEY NOT NULL," 
                + getStringColumn("NAME", 50)  + ")";
    }

    protected String getCreateDASTEST2City() {
        return "CREATE TABLE DASTEST2.CITY (" + getIntegerColumn("INDX") + " PRIMARY KEY NOT NULL," 
                + getStringColumn("NAME", 50)  + ")";
    }    
    
    protected String getCreateDASTEST2Account() {
    	return "CREATE TABLE DASTEST2.ACCOUNT ("+ getIntegerColumn("ACCOUNT_ID") + " PRIMARY KEY NOT NULL," +
    	getIntegerColumn("CUSTOMER_ID") +", "+ getIntegerColumn("BALANCE") + ")";
    }
    
    protected String getCreateDASTEST3CustOrder() {
    	return "CREATE TABLE DASTEST3.CUSTORDER ("+ getIntegerColumn("ORDER_ID") + " PRIMARY KEY NOT NULL," +
    	getIntegerColumn("CUSTOMER_ID") +", "+getIntegerColumn("ORDER_COUNT")+ ")";
    }    

    protected String getCreateDASTEST1OrderDetails() {
        return "CREATE TABLE DASTEST1.ORDERDETAILS (" + getIntegerColumn("ORDERID") + " NOT NULL, " 
            + getIntegerColumn("PRODUCTID")
                + " NOT NULL, PRICE FLOAT, PRIMARY KEY (ORDERID, PRODUCTID))";
    }
    
    protected String getCreateDASTEST3OrderDetailsDesc() {
        return "CREATE TABLE DASTEST3.ORDERDETAILSDESC ("+ getIntegerColumn("ID") + " NOT NULL, " + getIntegerColumn("ORDERID") + " NOT NULL, " 
            + getIntegerColumn("PRODUCTID")
                + " NOT NULL,"+ getStringColumn("DESCR", 20)+", PRIMARY KEY (ID))";
    }
    //JIRA-952 end
    // //////////////

    protected String getCreateSinger() {
    	return "CREATE TABLE SINGER ("+ getIntegerColumn("ID") + " NOT NULL, " +
    		getStringColumn("NAME", 20) +" )";
    }
    
    protected String getCreateSong() {
    	return "CREATE TABLE SONG ("+ getIntegerColumn("ID") + " , " +//" NOT NULL, " +
    		getStringColumn("TITLE", 20) + ", "+
    		getIntegerColumn("SINGERID") + " )";
    }
    

    protected String getCreateDocumentsImages() {
    	return "CREATE TABLE DOCUMENTS_IMAGES (ID INT, TEXT CLOB(64K),PIC BLOB(16M))";
    }
    



    protected String getCreateBankAccount() {
    	return "CREATE TABLE BANKACCOUNT ("+ getIntegerColumn("ID") + " , " +
		getIntegerColumn("SSN") + ", "+
		getIntegerColumn("BALANCE") + " )";    	
    }
    




    protected String getForeignKeyConstraint(String pkTable, String pkColumn, String foreignKey) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("CONSTRAINT FK1 FOREIGN KEY (");
        buffer.append(foreignKey);
        buffer.append(") REFERENCES ");
        buffer.append(pkTable);
        buffer.append("(");
        buffer.append(pkColumn);
        buffer.append(") ON DELETE NO ACTION ON UPDATE NO ACTION");
        return buffer.toString();
    }

    protected String getStringColumn(String name, int length) {
        return name + ' ' + stringType + "(" + new Integer(length).toString() + ")";
    }

    protected String getIntegerColumn(String name) {
        return name + ' ' + integerType;
    }

    protected String getGeneratedKeyClause() {
        return "GENERATED ALWAYS AS IDENTITY";
    }

    protected String getDecimalColumn(String name, int size1, int size2) {
        return name + ' ' + decimalType + "(" + new Integer(size1).toString() + ',' 
            + new Integer(size2).toString() + ")";
    }

    protected String getFloatColumn(String name) {
        return name + ' ' + floatType;
    }

    protected String getTimestampColumn(String name) {
        return name + ' ' + timestampType;
    }

}
