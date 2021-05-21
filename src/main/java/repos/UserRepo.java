package repos;

import models.AppUser;

import java.util.List;
import annotations.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.postgresql.util.PSQLException;
import util.ConnectionFactory;
import models.ColumnNode;
import java.sql.Connection;
import java.util.*;
import java.sql.*;import java.util.stream.*;
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
    public static void buildTable(Object o, Connection conn) {
            //check a class to determine columns for table.
            Class<?> clazz = o.getClass();

            System.out.println("Running buildTable()...");

            if (clazz.isAnnotationPresent(Entity.class)) {//if annotated has entity that has attributes to draw from
                if (clazz.isAnnotationPresent(Table.class)) {
                    Table table = clazz.getAnnotation(Table.class);//Table annotation
                    String tableName = table.name();//EX: tableName.name == "users"
                    System.out.println("Inside createTable: " + tableName);

                    //check for columns the class has
                    List<ColumnNode> columnNodes = new ArrayList<>();

                    //String primaryKey = "PRIMARY KEY (";
                    boolean primaryKeyAssigned = false;

                    LinkedList<String> sqlStatements = new LinkedList<>();

                    for (Field f : clazz.getDeclaredFields()) {
                        if (f.isAnnotationPresent(Column.class)) {
                            Column column = f.getAnnotation(Column.class);

                            StringBuilder dropStatement = new StringBuilder().append("ALTER TABLE ").append(tableName)
                                    .append(" DROP COLUMN IF EXISTS ");
                            StringBuilder preparedStatement = new StringBuilder().append("ALTER TABLE ").append(tableName)
                                    .append(" ADD COLUMN IF NOT EXISTS ");


                            columnNodes.add(new ColumnNode(column.name(), column.nullable(), column.unique()));
                            //System.out.printf("Added column %s\n", f.getAnnotation(Column.class).name());//checking
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
                                    //System.out.println(" INVALID");
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
                                //primaryKey = primaryKey + f.getAnnotation(Column.class).name() + ")";
                                preparedStatement.append(" PRIMARY KEY");
                                primaryKeyAssigned = true;
                                dropStatement.append(f.getAnnotation(Column.class).name());
                                //System.out.print(primaryKey);//checking

                                //TODO make composite key
                            } else if (f.isAnnotationPresent(Id.class) && primaryKeyAssigned) {
                                throw new InvalidFieldException("Table cannot have more than one primary key!");
                            }

                            //preparedStatement.append(" END");
                            //preparedStatement.append(", ");
                            //preparedStatement.append(")");

                            String sql = preparedStatement.toString();
                            if(primaryKeyAssigned){
                                System.out.println(dropStatement);
                                sqlStatements.add(dropStatement.toString());
                            }

                            //System.out.println(sql);//Just to verify
                            if(!sqlStatements.contains(sql)){
                                sqlStatements.add(sql);//no duplicates allowed
                            }


                            primaryKeyAssigned = false;
                        }
                    }//end for loop

                    //TODO Doesn't handle composite keys yet
                    /*if (primaryKeyAssigned) {
                        preparedStatement.append(primaryKey);
                    } else {
                        throw new IllegalArgumentException("There is no primary key!");
                    }*/

                    //preparedStatement.deleteCharAt(preparedStatement.lastIndexOf(","));
                    //preparedStatement.append(")");

                    /*String sql = preparedStatement.toString();

                    System.out.println(sql);//Just to verify*/

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

                            /*for (String sql : sqlStatements) {
                                System.out.println(sql);
                                PreparedStatement pstmt = conn.prepareStatement(sql);
                                pstmt.executeUpdate();//table created; but if column already exists?
                            }*/
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }

                    //now apply primary key, if any


                }//end if for checking if class has @Table
            }//end if for checking if class has @Entity
            else {
                System.out.println("Cannot create a table from class " + clazz.getName());
            }

    }//end createTable


   

  
  public void save(Object o, Connection conn) {

            Class<?> clazz = o.getClass();

            if (clazz.isAnnotationPresent(Entity.class)) {//if annotated as entity that has attributes to draw from
                if (clazz.isAnnotationPresent(Table.class)) {
                    Table table = clazz.getAnnotation(Table.class);
                    String tableName = table.name();//if tableName.name == "users"
                    System.out.println(tableName);

                    StringBuilder preparedStatement = new StringBuilder().append("INSERT INTO ").append(tableName);

                    List<ColumnNode> columnNodes = new ArrayList<>();

                    for (Field f : clazz.getDeclaredFields()) {
                        Column column = f.getAnnotation(Column.class);
                        if (column != null) {
                            if (!f.isAnnotationPresent(Id.class)) {
                                System.out.println(column.name() + " " + column.nullable() + " " + column.unique());
                                columnNodes.add(new ColumnNode(column.name(), column.nullable(), column.unique()));
                                //insert into "tablename" ('username', 'password', 'email', 'firstName', 'lastName', 'dob')
                                //values (username, password, email, firstName, lastName, dob)
                            }
                        }
                    }

                    preparedStatement.append(" (");

                    for (int i = 0; i < columnNodes.size(); i++) {
                        preparedStatement.append(columnNodes.get(i).getName() + ", ");

                    }

                    preparedStatement.deleteCharAt(preparedStatement.lastIndexOf(","));
                    preparedStatement.append(") VALUES (");


                    for (int i = 0; i < columnNodes.size(); i++) {
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
                        for (int i = 1; i <= columnNodes.size(); i++) {
                            if (fields[i].isAnnotationPresent(annotations.Date.class)) {
                                fields[i].setAccessible(true);
                                System.out.println(fields[i].get(o));
                                pstmt.setDate(i, java.sql.Date.valueOf(String.valueOf(fields[i].get(o))));
                                fields[i].setAccessible(false);
                            } else {
                                fields[i].setAccessible(true);
                                System.out.println(fields[i].get(o));
                                pstmt.setString(i, String.valueOf(fields[i].get(o)));
                                fields[i].setAccessible(false);
                            }
                        }


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
                    } catch (java.sql.SQLException | java.lang.IllegalAccessException throwables) {
                        throwables.printStackTrace();
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

    public void execute(Object o){
        Class<?> clazz = o.getClass();
        System.out.println("Marker 1");
        try(Connection conn = ConnectionFactory.getInstance().getConnection(o)) {

            if (clazz.isAnnotationPresent(Entity.class)) {//if annotated as entity that has attributes to draw from
                System.out.println("Marker 2");
                if (clazz.isAnnotationPresent(Table.class)) {
                    System.out.println("Marker 3");
                    Table table = clazz.getAnnotation(Table.class);
                    String tablename = table.name();//if tableName.name == "users"
                    String sql = ("CREATE TABLE IF NOT EXISTS " + tablename + "()");
                    Statement stmt = conn.createStatement();
                    stmt.execute(sql);
                    buildTable(o, conn);

                    System.out.println("Marker 4");
                }
            }
        } catch (SQLException throwables) {

            //throwables.printStackTrace();
        }
    }


  
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
    public boolean isEmailAvailable(String email){
        return false;
    }
  
  
    //TODO pending implementation
    public boolean isUsernameAvailable(String username){
        return false;
    }
}

