package repos;

import java.lang.reflect.*;
import java.util.List;
import annotations.*;
import java.util.ArrayList;
import util.ConnectionFactory;
import models.ColumnNode;
import java.sql.Connection;
import java.util.*;
import java.sql.*;
import exceptions.*;


/**
 * Takes in any object and constructs a query
 * @author Kevin Chang
 * @author Thomas Diendorf
 * @author Chris Levano
 */
public class UserRepo {

    /*
    CREATE TABLE users (
        user_id serial NOT null constraint pk_user primary key,
        first_name varchar(25) NOT NULL,
        last_name varchar(25) NOT NULL,
        username varchar(20) unique NOT null,
        "password" varchar(255) NOT NULL,
        email varchar(255) unique NOT null
    );*/

    /**
     * @author Thomas
     */
    public static void addColumns(Object o, Connection conn) {
            //check a class to determine columns for table.
            Class<?> clazz = o.getClass();

            System.out.println("Running buildTable()...");

            if (clazz.isAnnotationPresent(Entity.class)) {//if annotated has entity that has attributes to draw from
                if (clazz.isAnnotationPresent(Table.class)) {
                    Table table = clazz.getAnnotation(Table.class);//Table annotation
                    String tableName = table.name();//EX: tableName.name == "users"
                    System.out.println("Inside createTable: " + tableName);


                    StringBuilder addPrimaryKey = new StringBuilder().append("ALTER TABLE ").append(tableName)
                            .append(" add constraint ");

                    boolean primaryKeyAssigned = false;

                    LinkedList<String> sqlStatements = new LinkedList<>();

                    for (Field f : clazz.getDeclaredFields()) {
                        if (f.isAnnotationPresent(Column.class)) {
                            Column column = f.getAnnotation(Column.class);

                            StringBuilder preparedStatement = new StringBuilder().append("ALTER TABLE ").append(tableName)
                                    .append(" ADD COLUMN IF NOT EXISTS ");

                            preparedStatement.append(f.getAnnotation(Column.class).name());//append name of column

                            switch (f.getAnnotation(Column.class).type()) {
                                case "varchar":
                                    preparedStatement.append(" VARCHAR(");
                                    preparedStatement.append(f.getAnnotation(Column.class).length());
                                    preparedStatement.append(")");
                                    //System.out.printf(" VARCHAR(%s)", f.getAnnotation(Column.class).length());//checking
                                    break;
                                case "date":
                                    preparedStatement.append(" DATE");
                                    //System.out.print(" DATE");//checking
                                    break;
                                case "double":
                                    preparedStatement.append(" DOUBLE(");
                                    preparedStatement.append(f.getAnnotation(Column.class).length());
                                    preparedStatement.append(")");
                                    break;
                                case "serial":
                                    preparedStatement.append(" SERIAL");
                                    //System.out.print(" SERIAL");//checking
                                    break;
                                default: //if no type is found...
                                    throw new InvalidFieldException("Invalid data type!");
                            }

                            if (!f.getAnnotation(Column.class).nullable()) {
                                preparedStatement.append(" not null");
                                //System.out.print(" not null");//checking
                            }

                            if (f.getAnnotation(Column.class).unique()) {
                                preparedStatement.append(" unique");
                                //System.out.print(" unique");//checking
                            }

                            if (f.isAnnotationPresent(Id.class) && !primaryKeyAssigned) {
                                primaryKeyAssigned = true;
                                addPrimaryKey.append(f.getAnnotation(Column.class).name()).append("_pk ")
                                    .append("PRIMARY KEY (").append(f.getAnnotation(Column.class).name())
                                    .append(")");
                                //System.out.print(primaryKey);//checking
                            }

                            //TODO make composite key
                            //TODO: restore this if non-unique entries become an issue.
                            //if(!sqlStatements.contains(preparedStatement.toString())){
                                sqlStatements.add(preparedStatement.toString());//no duplicates allowed
                            //}
                        }
                    }//end for loop

                    //TODO Doesn't handle composite keys yet
                    if (primaryKeyAssigned) {
                        sqlStatements.add(addPrimaryKey.toString());
                    } /*else {
                        throw new IllegalArgumentException("There is no primary key!");
                    }*/

                    //TODO: figure out why duplicate statements exist in sqlStatements
                    //execute an sql statement for each column needing to be added
                    if(sqlStatements.size() > 0) {//must be at least 1 column to add
                        try {
                            //reverse order so that DROP COLUMN statements are executed first.
                            for(String q : sqlStatements){
                                PreparedStatement pstmt = conn.prepareStatement(q);
                                System.out.println(pstmt.toString());
                                pstmt.executeUpdate();
                            }
                        } catch (SQLException e) {//a statement couldn't be executed
                            System.out.println("Couldn't execute current SQL statement.  May be trying to add on a primary key that already exists." +
                                    "  Finished adding/editing columns!");
                            //e.printStackTrace();
                        }
                    }
                }//end if for checking if class has @Table
            }//end if for checking if class has @Entity
            else {
                System.out.println("Cannot create a table from class " + clazz.getName());
            }

    }//end createTable


   

    /*
    INSERT INTO table_name (column1, column2, column3, ...)
    VALUES (value1, value2, value3, ...);
     */

    //TODO: Thomas needs to work on this
    //TODO: verification that table, and columns, already exist?
    //rename from save() to insert()
    //build a query that genetically queries the objects fields
    //just edit this
  public void insert(Object o, Connection conn) {

            Class<?> clazz = o.getClass();//holds object instance of a class with annotated values to be inserted into table

            if (clazz.isAnnotationPresent(Entity.class)) {//if annotated as entity that has attributes to draw from
                if (clazz.isAnnotationPresent(Table.class)) {//if there's a table that can be build from this
                    Table table = clazz.getAnnotation(Table.class);
                    String tableName = table.name();//if tableName.name == "users"
                    System.out.println(tableName);

                    StringBuilder preparedStatement = new StringBuilder().append("INSERT INTO ")
                            .append(tableName).append(" (");

                    //insert into "tablename" ('username', 'password', 'email', 'firstName', 'lastName', 'dob')
                    //values (username, password, email, firstName, lastName, dob)
                    LinkedList<String> columnsUsed = new LinkedList<>();//holds column names
                    //LinkedList<String> columnData = new LinkedList<>();//holds each value to be inserted into the row for each column
                    for (Field f : clazz.getDeclaredFields()) {
                        Column column = f.getAnnotation(Column.class);
                        if (column != null) {
                            if (!f.isAnnotationPresent(Id.class)) {//because this is serial and automated

                                //add column name to list
                                columnsUsed.add(f.getAnnotation(Column.class).name());

                            }
                        }
                    }


                    for(String col : columnsUsed){
                        preparedStatement.append(col + ", ");
                    }

                    preparedStatement.deleteCharAt(preparedStatement.lastIndexOf(","));
                    preparedStatement.append(") VALUES (");

                    for (int i = 0; i < columnsUsed.size(); i++) {
                        preparedStatement.append("?, ");
                    }

                    preparedStatement.deleteCharAt(preparedStatement.lastIndexOf(","));
                    preparedStatement.append(")");

                    String sql = preparedStatement.toString();

                    //So we need to make a connection
                    try {
                        PreparedStatement pstmt = conn.prepareStatement(sql, new String[]{"user_id"});

                        Field[] fields = clazz.getDeclaredFields();
                        //We start at i = 1, because sql is indexed at 1, and because field[0] is id
                        //for (int i = 1; i <= columnNodes.size(); i++) {
                        for (int i = 1; i <= columnsUsed.size(); i++) {
                            String type = fields[i].getAnnotation(Column.class).type();
                            if (type.equals("date")) {
                                fields[i].setAccessible(true);
                                System.out.println(fields[i].get(o));
                                pstmt.setDate(i, java.sql.Date.valueOf(String.valueOf(fields[i].get(o))));
                                fields[i].setAccessible(false);
                            }
                            else if(type.equals("varchar")) {
                                fields[i].setAccessible(true);
                                System.out.println(fields[i].get(o));
                                pstmt.setString(i, String.valueOf(fields[i].get(o)));
                                fields[i].setAccessible(false);
                            }
                            else if(type.equals("int")) {
                                fields[i].setAccessible(true);
                                System.out.println(fields[i].get(o));
                                pstmt.setInt(i, Integer.valueOf(String.valueOf(fields[i].get(o))));
                                fields[i].setAccessible(false);
                            }
                        }

                        System.out.println("Executing this statement:\n" + pstmt.toString());
                        int rowsInserted = pstmt.executeUpdate();

                        //from some AppUser instance passed into here from elsewhere, we construct an accounts table
                        if (rowsInserted != 0 && tableName.equals("users")) {
                            ResultSet rs = pstmt.getGeneratedKeys();
                            while (rs.next()) {
                                Field idField = Arrays
                                        .stream(fields)
                                        .filter((field) -> field.isAnnotationPresent(Id.class))
                                        .findFirst()
                                        .orElseThrow(() -> new InvalidFieldException("This field does not exist in your Class!"));

                                if (idField.isAnnotationPresent(Id.class)) {
                                    //if user Id annotation is present (plus no duplicate)
                                    Id id = idField.getAnnotation(Id.class);
                                    String idName = id.name();
                                    System.out.println(idName);
                                    String account_insert = "INSERT INTO accounts (" + idName + ") values (?)";
                                    PreparedStatement prepState = conn.prepareStatement(account_insert, new String[]{"account_num"});
                                    prepState.setInt(1, rs.getInt(idName));
                                    int account_num = prepState.executeUpdate();
                                }

                            }
                        }
                    } catch (java.sql.SQLException throwables) {
                        System.out.println("You cannot insert duplicate key values!  Stopping insertion...");
                        //throwables.printStackTrace();
                    } catch (java.lang.IllegalAccessException throwables){
                        throwables.printStackTrace();
                    }

                }//end if
            }//end if

    }


    /*
    DELETE FROM tableName WHERE columnName='value';
     */
    public void delete(Object o, Connection conn){
        Class<?> clazz = o.getClass();//holds object instance of a class with annotated values to be inserted into table

        if (clazz.isAnnotationPresent(Entity.class)) {//if annotated as entity that has attributes to draw from
            if (clazz.isAnnotationPresent(Table.class)) {//if there's a table that can be build from this
                Table table = clazz.getAnnotation(Table.class);
                String tableName = table.name();//if tableName.name == "users"
                System.out.println("Deleting from " + tableName);

                StringBuilder removeRow = new StringBuilder().append("DELETE FROM " + tableName + " WHERE ");

                String columnUsed = null;

                for (Field f : clazz.getDeclaredFields()) {
                    Column column = f.getAnnotation(Column.class);
                    if (column != null) {
                        if (f.isAnnotationPresent(Column.class)) {//because this is serial and automated
                            if(f.getAnnotation(Column.class).name().equals("username")) {
                                columnUsed = f.getAnnotation(Column.class).name();
                                removeRow.append(columnUsed + " = \'");

                                f.setAccessible(true);
                                try {
                                    System.out.println("Deleting row with value: " + f.get(o));
                                    removeRow.append(f.get(o) + "\'");
                                    PreparedStatement pstmt = conn.prepareStatement(removeRow.toString());
                                    System.out.println("Executing statement: " + pstmt);
                                    pstmt.executeUpdate();
                                    break;
                                } catch (IllegalAccessException | SQLException e) {
                                    e.printStackTrace();
                                    break;
                                }
                            }
                        }
                    }
                }
            }//end if
        }//end if
    }



    /*
    SELECT * FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_NAME = N'Employees'

    or

    IF OBJECT_ID('table_name', 'U') IS NOT NULL

    "CREATE TABLE IF NOT EXISTS " + tablename
     */

    public void create(Object o){
        Class<?> clazz = o.getClass();
        try(Connection conn = ConnectionFactory.getInstance().getConnection(o)) {

            if (clazz.isAnnotationPresent(Entity.class)) {//if annotated as entity that has attributes to draw
                if (clazz.isAnnotationPresent(Table.class)) {
                    Table table = clazz.getAnnotation(Table.class);
                    String tableName = table.name();//if tableName.name == "users"
                    String sql = ("CREATE TABLE IF NOT EXISTS " + tableName + "()");
                    Statement stmt = conn.createStatement();
                    stmt.execute(sql);
                    addColumns(o, conn);
                    insert(o, conn);//added by Thomas; in case new object has data needing inserted
                }
            }
        } catch (SQLException throwables) {

            //throwables.printStackTrace();
        }
    }

    /**
     * @author Chris Levano
     * @author Kevin Chang
     */
    //TODO implement a SELECT method
    public Object select(Object o){
        Class<?> clazz = o.getClass();
        //This will serve as the return object, returns null if proper algorithm does not execute
        Object returnObject = null;

        try(Connection conn = ConnectionFactory.getInstance().getConnection(o)){
            if(clazz.isAnnotationPresent((Entity.class))){
                if (clazz.isAnnotationPresent(Table.class)){
                    String tableName = clazz.getAnnotation(Table.class).name();
                    StringBuilder select = new StringBuilder("SELECT * FROM " + tableName + " WHERE ");
                    Field[] fields = clazz.getDeclaredFields();
                    for (Field field: fields) {
                        field.setAccessible(true);
                        Object fieldVal = field.get(o);
                        field.setAccessible(false);
                        if (field.getAnnotation(Column.class).type().equals("varchar")) {
                            if (fieldVal != null) {
                                String columnName = field.getAnnotation(Column.class).name();
                                select.append(columnName + " = ? AND ");
                            }
                        } else if (field.getAnnotation(Column.class).type().equals("date")) {
                            if (fieldVal != null) {
                                String columnName = field.getAnnotation(Column.class).name();
                                select.append(columnName + " = ? AND ");
                            }
                        } else if (field.getAnnotation(Column.class).type().equals("serial")) {
                            if (Integer.parseInt(String.valueOf(fieldVal)) != 0) {
                                String columnName = field.getAnnotation(Column.class).name();
                                select.append(columnName + " = ? AND ");
                            }
                        } else if (field.getAnnotation(Column.class).type().equals("double")) {
                            if (Double.parseDouble(String.valueOf(fieldVal)) != 0) {
                                String columnName = field.getAnnotation(Column.class).name();
                                select.append(columnName + " = ? AND ");
                            }
                        }
                    }
                    select.deleteCharAt(select.lastIndexOf("A"));
                    select.deleteCharAt(select.lastIndexOf("N"));
                    select.deleteCharAt(select.lastIndexOf("D"));

                    PreparedStatement pstmt = conn.prepareStatement(select.toString());

                    int pstmtCount = 1;
                    for (int i = 0; i < fields.length; i++) {
                        fields[i].setAccessible(true);
                        Object currentFieldVal = fields[i].get(o);
                        fields[i].setAccessible(false);
                        String type = fields[i].getAnnotation(Column.class).type();

                        if (type.equals("date")) {
                            if (currentFieldVal != null) {
                                fields[i].setAccessible(true);
                                pstmt.setDate(pstmtCount, java.sql.Date.valueOf(String.valueOf(currentFieldVal)));
                                pstmtCount++;
                            }
                        }
                        else if (type.equals("varchar")) {
                            if (currentFieldVal != null) {
                                //System.out.println(fields[i].get(o));
                                pstmt.setString(pstmtCount, String.valueOf(currentFieldVal));
                                pstmtCount++;
                            }
                        } else if (type.equals("int")) {
                            if (Integer.parseInt(String.valueOf(currentFieldVal)) != 0) {
                                //System.out.println(fields[i].get(o));
                                pstmt.setInt(pstmtCount, Integer.parseInt(String.valueOf(currentFieldVal)));
                                pstmtCount++;
                            }
                        }
                    }
                    ResultSet rs = pstmt.executeQuery();
                    while (rs.next()){
                        //Creates an instance of the object to then invoke its methods
                        returnObject = clazz.newInstance();
                        //gathers all the methods
                        Method[] methods = clazz.getDeclaredMethods();
                        for (int i = 0; i <methods.length; i++) {
                            System.out.println(methods[i]);
                            if(methods[i].isAnnotationPresent(Setter.class)){
                                String methodName = methods[i].getAnnotation(Setter.class).name();
                                    for (Field field : fields) {
                                        String columnName = field.getAnnotation(Column.class).name();
                                        if(methodName.equals(columnName)){
                                            String type = field.getAnnotation(Column.class).type();
                                            switch(type) {
                                                case "varchar":
                                                    methods[i].setAccessible(true);
                                                    //invoke method on the returnObject to set its values
                                                    methods[i].invoke(returnObject, rs.getString(columnName));
                                                    methods[i].setAccessible(false);
                                                    break;
                                                case "date":
                                                    methods[i].setAccessible(true);
                                                    methods[i].invoke(returnObject, String.valueOf(rs.getDate(columnName)));
                                                    methods[i].setAccessible(false);
                                                    break;
                                                case "double":
                                                    methods[i].setAccessible(true);
                                                    methods[i].invoke(returnObject, rs.getDouble(columnName));
                                                    methods[i].setAccessible(false);
                                                    break;
                                                case "int"://TODO: something for this?
                                                case "serial":
                                                    methods[i].setAccessible(true);
                                                    methods[i].invoke(returnObject, rs.getInt(columnName));
                                                    methods[i].setAccessible(false);
                                                    break;
                                                default:
                                            }

                                        }
                                    }
                            }
                        }
                    }
                }
            }

        } catch (SQLException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
        return returnObject;
    }

    //TODO if at all
    public Object findUserByUsernameAndPassword(Object o){
        Object result = null;
        //reflect object

        //get fields username and password

        //check with db, if doesn't exist, return null

        //if does exist:

        //get all fields of object
        //extract constructor and get a .newInstance()
        //use reflection to get all the setters of the constructor and set values with the
        //field values from database (rs.getString("column_label"))
            //note: make sure you have an if statement that controls .getString or .getInt or .getDate depending on constructor parameters
            //you can read what parameters a given constructor takes using Class[] parameterTypes = constructor.getParameterTypes();

        return result;
    }
  

    //TODO pending implementation
    public boolean isEmailAvailable(Object o){
        //you can still assume you're looking email, because it's embedded in theo bject
        //you need reflection to get the column names and run queries
        return false;
    }
  
  
    //TODO pending implementation
    public boolean isUsernameAvailable(Object o){
        //you can still assume you're looking email, because it's embedded in theo bject
        //you need reflection to get the column names and run queries
        return false;
    }


    //TODO: implement this, update
    public void update(){

    }

}

