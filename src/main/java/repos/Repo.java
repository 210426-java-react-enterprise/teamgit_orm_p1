package repos;

import java.lang.reflect.*;
import java.sql.Array;
import java.sql.Date;
import java.time.*;
import java.util.List;
import annotations.*;
import java.util.ArrayList;


import models.*;
import util.*;

import java.sql.Connection;
import java.util.*;
import java.sql.*;
import exceptions.*;


public class Repo {

    ConnectionPool connectionPool = new ConnectionPool().getInstance();
    /**
     * @author Chris Levano
     * @author Kevin Chang
     */

    public void update(Object o) throws IllegalAccessException {

        //the object array that has been updated will be returned
        //ArrayList<Object> objArr = null;
        ArrayList<Field> pstmtFields = new ArrayList<>();
        ArrayList<Field> whereFields = new ArrayList<>();

        //keeps track of the initial parameter of pstmt.setParameter(pstmtCount, val); so that you can use call to
        //preparePreparedStatement twice
        int pstmtCount = 1;

        String idName = null;

        try (Connection conn = connectionPool.getConnection()) {
            Class<?> clazz = o.getClass();

            if (clazz.isAnnotationPresent(Entity.class)) {//if annotated as entity that has attributes to draw from
                if (clazz.isAnnotationPresent(Table.class)) {
                    Table table = clazz.getAnnotation(Table.class);
                    String tableName = table.name();
                    StringBuilder preparedStatement = new StringBuilder().append("UPDATE ").append(tableName);

                    preparedStatement.append(" SET ");
                    Field[] fields = clazz.getDeclaredFields();
                  
                    for (Field f : fields) {
                        Column column = f.getAnnotation(Column.class);
                        if (column != null) {
                            if (f.getAnnotation(Column.class).updateable()) {
                                //if (f != null) {
                                    pstmtFields.add(f);
                                    preparedStatement.append(f.getAnnotation(Column.class).name());
                                    preparedStatement.append(" = ?");
                                    preparedStatement.append(", ");
                                    pstmtCount++;
                                //}
                            }
                        }
                    }
                    preparedStatement.deleteCharAt(preparedStatement.lastIndexOf(","));
                    //use stream method to extract out primary key id if an id exists

                    //Field idField = streamIdField(fields);
                    if(fields != null){
                        preparedStatement.append(" WHERE ");
                    }
                    for (Field f : fields) {
                        //essentially, updates are set to occur on matching serial id's
                        //id cannot be zero, must be unique and non-updateable, to check where clause
                        if(f.isAnnotationPresent(Id.class)) {
                            f.setAccessible(true);
                            if (Integer.parseInt(String.valueOf(f.get(o))) != 0) {
                                preparedStatement.append(f.getAnnotation(Id.class).name()).append(" = ").append("?").append(", ");
                                whereFields.add(f);
                            }
                            f.setAccessible(false);
                        }else if(!f.getAnnotation(Column.class).updateable()){
                            f.setAccessible(true);
                            preparedStatement.append(f.getAnnotation(Column.class).name()).append(" = ").append("?").append(" AND ");
                            f.setAccessible(false);
                            whereFields.add(f);
                        }
                    }

                    preparedStatement.deleteCharAt(preparedStatement.lastIndexOf("A"));
                    preparedStatement.deleteCharAt(preparedStatement.lastIndexOf("N"));
                    preparedStatement.deleteCharAt(preparedStatement.lastIndexOf("D"));


                    System.out.println(preparedStatement.toString());

                    //Sends in a new String[] containing the Id field's name, retrieves newly generated Id
                    PreparedStatement pstmt = conn.prepareStatement(preparedStatement.toString());

                    //made a separate method to prepare a pstmt from an ArrayList of relevant fields

                    pstmt = preparePreparedStatement(o, pstmtFields, pstmt, 1);
                    pstmt = preparePreparedStatement(o, whereFields, pstmt, pstmtCount);
                    pstmt.executeUpdate();



                }//end if
            }//end if


        } catch (SQLException e) {
            throw new ResourcePersistenceException();
        }

         catch (IllegalAccessException e) {
            throw new ResourceNotAccessibleException();
        }



    }//end update()
    /**
     * Takes in any object and constructs an insert statement
     * @author Kevin Chang
     * @author Thomas Diendorf
     * @author Chris Levano
     * Inserts a row of data into an SQL table.
     * @param o Object that has a Table, Entity, and Column annotations.  Must contain data to reference for insertion.
     */
    public void insert(Object o) {

            Class<?> clazz = o.getClass();//holds object instance of a class with annotated values to be inserted into table

            //hold an ArrayList of fields that will be passed into pstmt
            ArrayList<Field> pstmtFields = new ArrayList<>();

            //return object array
            ArrayList<Object> objArr = null;

      try (Connection conn = connectionPool.getConnection()) {
          if (clazz.isAnnotationPresent(Entity.class)) {//if annotated as entity that has attributes to draw from
              if (clazz.isAnnotationPresent(Table.class)) {//if there's a table that can be build from this
                  Table table = clazz.getAnnotation(Table.class);
                  String tableName = table.name();//if tableName.name == "users"
                  System.out.println(tableName);

                  StringBuilder preparedStatement = new StringBuilder().append("INSERT INTO ")
                          .append(tableName).append(" (");


                  Field[] fields = clazz.getDeclaredFields();

                  for (Field f : fields) {
                      Column column = f.getAnnotation(Column.class);
                      if (column != null) {
                          if (!f.isAnnotationPresent(Id.class)) {//because this is serial and automated
                              preparedStatement.append(f.getAnnotation(Column.class).name() + ", ");
                              pstmtFields.add(f);
                          }

                      }
                  }

                  preparedStatement.deleteCharAt(preparedStatement.lastIndexOf(","));
                  preparedStatement.append(") VALUES (");
                  for (Field f: pstmtFields) {
                      preparedStatement.append("?, ");
                  }

                  preparedStatement.deleteCharAt(preparedStatement.lastIndexOf(","));
                  preparedStatement.append(")");

                  //This method uses STREAMS to extract the primary key of annotation Id.class from an array of fields
                  //Field idField = streamIdField(fields);

                  try {
                      PreparedStatement pstmt = conn.prepareStatement(preparedStatement.toString());

                      pstmt = preparePreparedStatement(o, pstmtFields, pstmt, 1);

                      System.out.println("Executing this statement:\n" + pstmt.toString());
                      pstmt.executeUpdate();


//                      if (rowsInserted != 0) {
//                          ResultSet rs = pstmt.getGeneratedKeys();
//                          while (rs.next()) {
//                              Method idMethod = Arrays.stream(clazz
//                                      .getDeclaredMethods())
//                                      .filter((method) -> method.isAnnotationPresent(Setter.class) && method.isAnnotationPresent(Id.class))
//                                      .findFirst()
//                                      .orElseThrow(() -> new InvalidMethodException("This method does not exist in your Class!"));
//
//                              //invokes setter on passed in object to place in newly generated id, adds to objArr for return
//                              //TODO Fix this later
//                              //objArr.add(idMethod.invoke(o, rs.getInt(idField.getAnnotation(Id.class).name())));
//
//                          }
//                      }

                  } catch (SQLException e) {
                      throw new ResourceDuplicationException();
                  } catch (IllegalAccessException e) {
                      throw new ResourceNotAccessibleException();
                  }

              }//end if
          }//end if
      } catch (SQLException e) {
          e.printStackTrace();
      }
    //return objArr;
  }

  /*
    private Field streamIdField(Field[] fields) {
        Field idField = Arrays
                .stream(fields)
                .filter((field) -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(() -> new InvalidFieldException("This field does not exist in your Class!"));
        return idField;
    }

   */


    /*
    DELETE FROM tableName WHERE columnName='value'
    AND WHERE columnName='value';
     */


    /**
     * Deletes one or more rows of data from an SQL table.
     * @param o Object that has a Table, Entity, and Column annotations.  Must contain data to reference for deletion.
     * @return Number of rows successfully deleted.  0 means none were deleted.
     */
    public int delete(Object o) throws IllegalAccessException {
        Class<?> clazz = o.getClass();//holds object instance of a class with annotated values to be inserted into table
        ArrayList<Field> fieldList = new ArrayList<>();

        if (clazz.isAnnotationPresent(Entity.class)) {//if annotated as entity that has attributes to draw from
            if (clazz.isAnnotationPresent(Table.class)) {//if there's a table that can be build from this
                Table table = clazz.getAnnotation(Table.class);
                String tableName = table.name();//if tableName.name == "users"
                System.out.println("Deleting from " + tableName);

                StringBuilder removeRow = new StringBuilder().append("DELETE FROM ").append(tableName).append(" WHERE ");

                String columnUsed = null;


                for (Field f : clazz.getDeclaredFields()) {
                    f.setAccessible(true);
                    if(f.get(o) != null) {//if there is a value to be referenced for row deletion
                        Column column = f.getAnnotation(Column.class);
                        if (column != null) {
                            if (f.isAnnotationPresent(Column.class)) {
                                if (f.isAnnotationPresent(Id.class)) {//if id serial is 0, then it is not to be used for this
                                    if(f.get(o).equals(0)){//SQL rows start at 1, not 0, so 0 indicates Id is null
                                        continue;//skip Id if it's not considered
                                    }
                                }//else continue on as normal, with Id being added

                                columnUsed = f.getAnnotation(Column.class).name();
                                removeRow.append(columnUsed).append(" = \'");//column where matching value will be
                                //System.out.println("Deleting row with value: " + f.get(o));
                                f.setAccessible(true);
                                removeRow.append(f.get(o)).append("\'");//matching value in the colomn to indicate the row
                                fieldList.add(f);
                                f.setAccessible(false);
                                removeRow.append(" AND ");//if we go for another loop
                            }
                        }
                    }
                }//end foreach

                removeRow.deleteCharAt(removeRow.lastIndexOf(" "));
                removeRow.deleteCharAt(removeRow.lastIndexOf("D"));
                removeRow.deleteCharAt(removeRow.lastIndexOf("N"));
                removeRow.deleteCharAt(removeRow.lastIndexOf("A"));
                removeRow.deleteCharAt(removeRow.lastIndexOf(" "));


                //should have removeRow ready at this point; put it into a PreparedStatement

                try(Connection conn = connectionPool.getConnection()) {
                    PreparedStatement pstmt = conn.prepareStatement(removeRow.toString());
                    return pstmt.executeUpdate();//returns number of rows deleted
                } catch (SQLException e){
                    throw new ResourceNotFoundException();

                }

            }//end if Table present
        }//end if Entity present
        return 0;
    }


    /**
     * Creates an SQL table, then calls on other methods to add columns and a row of data (if any).
     * @param o Object that has a Table and entity annotations (with optional Column annotations).
     */
    public void create(Object o){
        Class<?> clazz = o.getClass();
        try(Connection conn = connectionPool.getConnection()) {
            if (clazz.isAnnotationPresent(Entity.class)) {//if annotated as entity that has attributes to draw
                if (clazz.isAnnotationPresent(Table.class)) {
                    Table table = clazz.getAnnotation(Table.class);
                    String tableName = table.name();//if tableName.name == "users"
                    String sql = ("CREATE TABLE IF NOT EXISTS " + tableName + "()");
                    Statement stmt = conn.createStatement();
                    stmt.executeUpdate(sql);
                    addColumns(o, conn);

                }
            }
        } catch (SQLException e) {
            throw new ResourcePersistenceException();
        }
    }

    /**
     * @author Thomas
     */
    private void addColumns(Object o, Connection conn) {
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
                        if (column != null) {
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
                                    preparedStatement.append(" DEFAULT 0.00");

                                    break;
                                case "serial":
                                    preparedStatement.append(" SERIAL");
                                    //System.out.print(" SERIAL");//checking
                                    break;
                                case "int":
                                    preparedStatement.append(" INTEGER");
                                    preparedStatement.append(" DEFAULT 0");
                                    break;
                                default: //if no type is found...
                                    throw new InvalidFieldException("Invalid data type!");
                            }
                        }

                        if (!f.getAnnotation(Column.class).nullable()) {
                            preparedStatement.append(" not null");
                            //System.out.print(" not null");//checking
                        }

                        if (f.getAnnotation(Column.class).unique()) {
                            preparedStatement.append(" unique");
                            //System.out.print(" unique");//checking
                        }

                        //handles foreign keys
                        if (f.isAnnotationPresent(ForeignKey.class)) {
                            preparedStatement.append(" foreign key references"
                                    + f.getAnnotation(ForeignKey.class).references()
                                    + "("
                                    + f.getAnnotation(ForeignKey.class).name()
                                    + ") "
                                    + "on delete cascade");

                        }

                        //TODO make composite key, put it before Id.class check, and set primaryKeyAssigned to true
                        //TODO new CompositeKey annotation necessary

                        if (f.isAnnotationPresent(Id.class) && !primaryKeyAssigned) {
                            primaryKeyAssigned = true;
                            addPrimaryKey.append(f.getAnnotation(Column.class).name()).append("_pk ")
                                    .append("PRIMARY KEY (").append(f.getAnnotation(Column.class).name())
                                    .append(")");
                            //System.out.print(primaryKey);//checking
                        }



                        sqlStatements.add(preparedStatement.toString());//no duplicates allowed
                    }
                }//end for loop

                if (primaryKeyAssigned) {
                    sqlStatements.add(addPrimaryKey.toString());
                } /*else {
                        throw new IllegalArgumentException("There is no primary key!");
                    }*/

                //execute an sql statement for each column needing to be added
                if (sqlStatements.size() > 0) {//must be at least 1 column to add
                    try {
                        //reverse order so that DROP COLUMN statements are executed first.
                        for (String q : sqlStatements) {
                            PreparedStatement pstmt = conn.prepareStatement(q);
                            System.out.println(pstmt.toString());
                            pstmt.executeUpdate();
                        }
                    } catch (SQLException e) {//a statement couldn't be executed

                        throw new ResourceDuplicationException();

                    }
                }
            }//end if for checking if class has @Table
        }//end if for checking if class has @Entity
        else {
            System.out.println("Cannot create a table from class " + clazz.getName());
        }

    }//end addColumns


    /**
     * @author Chris Levano
     * @author Kevin Chang
     */
    public ArrayList<Object> select(Object o) {
        Class<?> clazz = o.getClass();
        //This will serve as the return object, returns null if proper algorithm does not execute
        ArrayList<Object> objArr = null;
        ArrayList<Field> pstmtFields = new ArrayList<>();

        try (Connection conn = connectionPool.getConnection()) {
            if (clazz.isAnnotationPresent((Entity.class))) {
                if (clazz.isAnnotationPresent(Table.class)) {
                    String tableName = clazz.getAnnotation(Table.class).name();
                    boolean where = false;
                    StringBuilder select = new StringBuilder("SELECT * FROM " + tableName);
                    Field[] fields = clazz.getDeclaredFields();
                    for (Field field : fields) {
                        Column column = field.getAnnotation(Column.class);
                        if (column != null) {
                            if (where != true){
                                select.append(" WHERE ");
                                where = true;
                            }
                            field.setAccessible(true);
                            Object fieldVal = field.get(o);
                            field.setAccessible(false);


                            if (field.getAnnotation(Column.class).type().equals("varchar")) {
                                if (fieldVal != null) {
                                    String columnName = field.getAnnotation(Column.class).name();
                                    select.append(columnName + " = ? AND ");
                                    pstmtFields.add(field);

                                }
                            } else if (field.getAnnotation(Column.class).type().equals("date")) {
                                if (fieldVal != null) {
                                    String columnName = field.getAnnotation(Column.class).name();
                                    select.append(columnName + " = ? AND ");
                                    pstmtFields.add(field);

                                }
                            } else if (field.getAnnotation(Column.class).type().equals("serial")) {
                                if (Integer.parseInt(String.valueOf(fieldVal)) != 0) {
                                    String columnName = field.getAnnotation(Column.class).name();
                                    select.append(columnName + " = ? AND ");
                                    pstmtFields.add(field);

                                }
                                //TODO this makes it impossible to query balances of 0.00
                            }
                            else if (field.getAnnotation(Column.class).type().equals("double")) {
                                if (Double.parseDouble(String.valueOf(fieldVal)) != 0) {
                                    String columnName = field.getAnnotation(Column.class).name();
                                    select.append(columnName + " = ? AND ");
                                    pstmtFields.add(field);

                                }
                            }else if (field.getAnnotation(Column.class).type().equals("int")) {
                                if (Integer.parseInt(String.valueOf(fieldVal)) != 0) {
                                    String columnName = field.getAnnotation(Column.class).name();
                                    select.append(columnName + " = ? AND ");
                                    pstmtFields.add(field);

                                }
                            }

                        }
                    }
                    //deletes the last 'and' if where is true
                    if(where == true) {
                        select.deleteCharAt(select.lastIndexOf("A"));
                        select.deleteCharAt(select.lastIndexOf("N"));
                        select.deleteCharAt(select.lastIndexOf("D"));
                    }

                    PreparedStatement pstmt = conn.prepareStatement(select.toString());

                    //this method written specifically to adds to the '?' of the pstmt
                    pstmt = preparePreparedStatement(o, pstmtFields, pstmt, 1);

                    ResultSet rs = pstmt.executeQuery();

                    //this method should return a new object from the database
                    objArr = createObjectArrayFromResultSet(o, rs);
                }
            }

        } catch (SQLException | InstantiationException | InvocationTargetException e) {
            throw new ResourceNotFoundException();
        } catch (IllegalAccessException e) {
            throw new ResourceNotAccessibleException();
        }
        return objArr;
    }

    //private class-restricted method for setting the '?' values of the pstmt
    private PreparedStatement preparePreparedStatement(Object o, ArrayList<Field> fields, PreparedStatement pstmt, int pstmtCount) throws SQLException, IllegalAccessException {

        for (int i = 0; i < fields.size(); i++) {
            fields.get(i).setAccessible(true);
            Object currentFieldVal = fields.get(i).get(o);
            fields.get(i).setAccessible(false);
            String type = fields.get(i).getAnnotation(Column.class).type();

            if (type.equals("date")) {
                if (currentFieldVal != null) {
                    fields.get(i).setAccessible(true);

//                    String date = String.valueOf(currentFieldVal);
//                    date = date.substring(0, date.length()-2);
                    pstmt.setDate(pstmtCount, Date.valueOf(String.valueOf(currentFieldVal)));
                    pstmtCount++;
                }
            } else if (type.equals("varchar")) {
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
            else if (type.equals("double")) {
                if (Double.parseDouble(String.valueOf(currentFieldVal)) != 0) {
                    //System.out.println(fields[i].get(o));
                    pstmt.setDouble(pstmtCount, Double.parseDouble(String.valueOf(currentFieldVal)));
                    pstmtCount++;
                }
            }
        }
        return pstmt;
    }

    //private class-restricted method for acquiring data from database and using object setters to return an array of the object instances
    private ArrayList<Object> createObjectArrayFromResultSet(Object o, ResultSet rs) throws SQLException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<?> clazz = o.getClass();
        Field[] fields = clazz.getDeclaredFields();
        //Creates an instance of the object to then invoke its methods
        Object returnObject = null;
        ArrayList<Object> objArr = new ArrayList<>();

        while (rs.next()) {
            //gathers all the methods
            returnObject = clazz.newInstance();
            Method[] methods = clazz.getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                System.out.println(methods[i]);
                if (methods[i].isAnnotationPresent(Setter.class)) {
                    String methodName = methods[i].getAnnotation(Setter.class).name();
                    for (Field field : fields) {
                        Column column = field.getAnnotation(Column.class);
                        if (column != null) {
                            String columnName = field.getAnnotation(Column.class).name();
                            if (methodName.equals(columnName)) {
                                String type = field.getAnnotation(Column.class).type();
                                switch (type) {
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
                                    case "int":
                                    case "serial":
                                        methods[i].setAccessible(true);
                                        methods[i].invoke(returnObject, rs.getInt(columnName));
                                        methods[i].setAccessible(false);
                                        break;
                                    case "timestamp":
                                        methods[i].setAccessible(true);
                                        methods[i].invoke(returnObject, String.valueOf(rs.getTimestamp(columnName)));
                                        methods[i].setAccessible(false);
                                        break;
                                }

                            }
                        }
                    }
                }
            }
            objArr.add(returnObject);
        }
        return objArr;
    }
}
