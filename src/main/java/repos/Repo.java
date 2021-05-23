package repos;

import java.lang.reflect.*;
import java.util.List;
import annotations.*;
import java.util.ArrayList;


import models.*;
import util.ConnectionFactory;


import java.sql.Connection;
import java.util.*;
import java.sql.*;
import exceptions.*;



public class Repo {

    /**
     * @author Chris Levano
     * @author Kevin Chang
     */
    public ArrayList<Object> update(Object o) {

        //the object array that has been updated will be returned
        ArrayList<Object> objArr = null;
        ArrayList<Field> pstmtFields = null;
        String idName = null;

        try (Connection conn = ConnectionFactory.getInstance().getConnection(o)) {
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
                                pstmtFields.add(f);
                                preparedStatement.append(f.getAnnotation(Column.class).name());
                                preparedStatement.append(" = ? ");
                                preparedStatement.append(" , ");
                            }
                        }
                    }
                    //use stream method to extract out primary key id
                    Field idField = streamIdField(fields);

                    //essentially, updates are set to occur on matching serial id's
                    if (idField != null) {
                        idField.setAccessible(true);
                        preparedStatement.append(" WHERE ").append(idField.getAnnotation(Id.class).name()).append(" = ").append(idField.get(o));
                        idField.setAccessible(false);
                    }
                    preparedStatement.deleteCharAt(preparedStatement.lastIndexOf(","));

                    //Sends in a new String[] containing the Id field's name, retrieves newly generated Id
                    PreparedStatement pstmt = conn.prepareStatement(preparedStatement.toString(), new String[]{idField.getAnnotation(Id.class).name()});

                    //made a separate method to prepare a pstmt from an ArrayList of relevant fields
                    pstmt = preparePreparedStatement(o, pstmtFields, pstmt);
                    ResultSet rs = pstmt.executeQuery();

                    //constructs an object ArrayList from the provided object
                    objArr = createObjectArrayFromResultSet(o, rs);

                }//end if
            }//end if

        } catch (SQLException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }

        return objArr;
    }//end update()

    /**
     * @author Thomas
     */
    public void addColumns(Object o, Connection conn) {
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

    /**
     * Takes in any object and constructs an insert statement
     * @author Kevin Chang
     * @author Thomas Diendorf
     * @author Chris Levano

     * Inserts a row of data into an SQL table.
     * @param o Object that has a Table, Entity, and Column annotations.  Must contain data to reference for insertion.
     */
  public ArrayList<Object> insert(Object o) {

            Class<?> clazz = o.getClass();//holds object instance of a class with annotated values to be inserted into table

            //hold an ArrayList of fields that will be passed into pstmt
            ArrayList<Field> pstmtFields = new ArrayList<>();

            //return object array
            ArrayList<Object> objArr = null;

      try (Connection conn = ConnectionFactory.getInstance().getConnection(o)) {
          if (clazz.isAnnotationPresent(Entity.class)) {//if annotated as entity that has attributes to draw from
              if (clazz.isAnnotationPresent(Table.class)) {//if there's a table that can be build from this
                  Table table = clazz.getAnnotation(Table.class);
                  String tableName = table.name();//if tableName.name == "users"
                  System.out.println(tableName);

                  StringBuilder preparedStatement = new StringBuilder().append("INSERT INTO ")
                          .append(tableName).append(" (");

                  //insert into "tablename" ('username', 'password', 'email', 'firstName', 'lastName', 'dob')
                  //values (username, password, email, firstName, lastName, dob)

                  //LinkedList<String> columnsUsed = new LinkedList<>();//holds column names
                  //LinkedList<String> columnData = new LinkedList<>();//holds each value to be inserted into the row for each column


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
                  Field idField = streamIdField(fields);

                  try {
                      PreparedStatement pstmt = conn.prepareStatement(preparedStatement.toString(), new String[]{idField.getAnnotation(Id.class).name()});

                      pstmt = preparePreparedStatement(o, pstmtFields, pstmt);

                      System.out.println("Executing this statement:\n" + pstmt.toString());
                      pstmt.executeUpdate();

                      ResultSet rs = pstmt.getGeneratedKeys();

//                      //from some AppUser instance passed into here from elsewhere, we construct an accounts table
//                      if (rowsInserted != 0 && tableName.equals("users")) {
//                          ResultSet rs = pstmt.getGeneratedKeys();
//                          while (rs.next()) {
//                              Field idField = Arrays
//                                      .stream(fields)
//                                      .filter((field) -> field.isAnnotationPresent(Id.class))
//                                      .findFirst()
//                                      .orElseThrow(() -> new InvalidFieldException("This field does not exist in your Class!"));
//
//                              if (idField.isAnnotationPresent(Id.class)) {
//                                  //if user Id annotation is present (plus no duplicate)
//                                  Id id = idField.getAnnotation(Id.class);
//                                  String idName = id.name();
//                                  System.out.println(idName);
//                                  String account_insert = "INSERT INTO accounts (" + idName + ") values (?)";
//                                  PreparedStatement prepState = conn.prepareStatement(account_insert, new String[]{"account_num"});
//                                  prepState.setInt(1, rs.getInt(idName));
//                                  int account_num = prepState.executeUpdate();
//                              }
//
//                          }
//                      }
                  } catch (java.sql.SQLException throwables) {
                      System.out.println("You cannot insert duplicate key values!  Stopping insertion...");
                      //throwables.printStackTrace();
                  } catch (java.lang.IllegalAccessException throwables) {
                      throwables.printStackTrace();
                  }

              }//end if
          }//end if
      } catch (SQLException e) {
          e.printStackTrace();
      }
    return objArr;
  }

    private Field streamIdField(Field[] fields) {
        Field idField = Arrays
                .stream(fields)
                .filter((field) -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(() -> new InvalidFieldException("This field does not exist in your Class!"));
        return idField;
    }


    /*
    DELETE FROM tableName WHERE columnName='value';
     */

    /**
     * Deletes a row of data from an SQL table.
     * @param o Object that has a Table, Entity, and Column annotations.  Must contain data to reference for deletion.
     * @param conn A Connection to the SQL database.
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
                        if (f.isAnnotationPresent(Column.class)) {
                            if(f.getAnnotation(Column.class).unique() && !f.isAnnotationPresent(Id.class)){//don't delete based on serials
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


    /**
     * Creates an SQL table, then calls on other methods to add columns and a row of data (if any).
     * @param o Object that has a Table and entity annotations (with optional Column annotations).
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
  
    public ArrayList<Object> select(Object o) {
        Class<?> clazz = o.getClass();
        //This will serve as the return object, returns null if proper algorithm does not execute
        ArrayList<Object> objArr = null;
        ArrayList<Field> pstmtFields = null;

        try (Connection conn = ConnectionFactory.getInstance().getConnection(o)) {
            if (clazz.isAnnotationPresent((Entity.class))) {
                if (clazz.isAnnotationPresent(Table.class)) {
                    String tableName = clazz.getAnnotation(Table.class).name();
                    StringBuilder select = new StringBuilder("SELECT * FROM " + tableName + " WHERE ");
                    Field[] fields = clazz.getDeclaredFields();
                    for (Field field : fields) {
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
                        } else if (field.getAnnotation(Column.class).type().equals("double")) {
                            if (Double.parseDouble(String.valueOf(fieldVal)) != 0) {
                                String columnName = field.getAnnotation(Column.class).name();
                                select.append(columnName + " = ? AND ");

                                pstmtFields.add(field);
                            }
                        }
                    }
                    //deletes the last 'and'

                    select.deleteCharAt(select.lastIndexOf("A"));
                    select.deleteCharAt(select.lastIndexOf("N"));
                    select.deleteCharAt(select.lastIndexOf("D"));

                    PreparedStatement pstmt = conn.prepareStatement(select.toString());

                    //this method written specifically to adds to the '?' of the pstmt
                    pstmt = preparePreparedStatement(o, pstmtFields, pstmt);


                    ResultSet rs = pstmt.executeQuery();

                    //this method should return a new object from the database
                    objArr = createObjectArrayFromResultSet(o, rs);

                }
            }

        } catch (SQLException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
        return objArr;
    }

    //private class-restricted method for setting the '?' values of the pstmt
    private PreparedStatement preparePreparedStatement(Object o, ArrayList<Field> fields, PreparedStatement pstmt) throws SQLException, IllegalAccessException {
        int pstmtCount = 1;

        for (int i = 0; i < fields.size(); i++) {
            fields.get(i).setAccessible(true);
            Object currentFieldVal = fields.get(i).get(o);
            fields.get(i).setAccessible(false);
            String type = fields.get(i).getAnnotation(Column.class).type();

            if (type.equals("date")) {
                if (currentFieldVal != null) {
                    fields.get(i).setAccessible(true);
                    pstmt.setDate(pstmtCount, java.sql.Date.valueOf(String.valueOf(currentFieldVal)));
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
            objArr.add(returnObject);
        }
        return objArr;
    }
}

