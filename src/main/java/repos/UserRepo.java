package repos;

import models.AppUser;

import java.util.List;
import annotations.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import util.ConnectionFactory;
import models.ColumnNode;
import java.sql.Connection;
import java.util.*;
import java.sql.*;import java.util.stream.*;


/**
 * Takes in any object and constructs a query
 * @author Kevin Chang
 * @author Thomas Diendorf
 * @author Chris Levano
 */
public class UserRepo {


    //EX: saving instance of AppUser with a ConnectionFactory
    public static void save(Object o, Connection conn){

        Class<?> clazz = o.getClass();//EX: AppUser class

        if(clazz.isAnnotationPresent(Entity.class)){//if annotated has entity that has attributes to draw from
            if(clazz.isAnnotationPresent(Table.class)){
                Table table = clazz.getAnnotation(Table.class);//Table annotation
                String tableName = table.name();//EX: tableName.name == "users"
                System.out.println(tableName);

                StringBuilder preparedStatement = new StringBuilder().append("INSERT INTO ").append(tableName);

                List<ColumnNode> columnNodes = new ArrayList<>();


                for (Field f: clazz.getDeclaredFields()) {
                    Column column = f.getAnnotation(Column.class);
                    if (column != null) {
                        if(!f.isAnnotationPresent(Id.class)) {
                            System.out.println(column.name() + " " + column.nullable() + " " + column.unique());
                            columnNodes.add(new ColumnNode(column.name(),column.nullable(), column.unique()));
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

                for (int i = 0; i < columnNodes.size(); i++){
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
                        //because Date in SQL needs some conversion
                        if (fields[i].isAnnotationPresent(annotations.Date.class)){
                            fields[i].setAccessible(true);
                            System.out.println(fields[i].get(o));
                            pstmt.setDate(i, java.sql.Date.valueOf(String.valueOf(fields[i].get(o))));
                            fields[i].setAccessible(false);
                        }else{
                            fields[i].setAccessible(true);
                            System.out.println(fields[i].get(o));
                            pstmt.setString(i, String.valueOf(fields[i].get(o)));
                            fields[i].setAccessible(false);
                        }
                    }

                    int rowsInserted = pstmt.executeUpdate();

                    //from some AppUser instance passed into here from elsewhere?

                    if (rowsInserted != 0 && tableName.equals("users")) {//if at least 1 row was inserted into "users" table
                        ResultSet rs = pstmt.getGeneratedKeys();
                        while (rs.next()) {
                            List<Field> idField = Arrays
                                    .stream(fields)
                                    .filter((field) -> field.isAnnotationPresent(Id.class))
                                    .collect(java.util.stream.Collectors.toList());

                            if (idField.get(0).isAnnotationPresent(Id.class)) {
                                //if user Id annotation is present (plus no duplicate)
                                Id id = idField.get(0).getAnnotation(Id.class);
                                String idName = id.name();
                                System.out.println(idName);
                                String account_insert = "INSERT INTO accounts (" + idName + ") values (?)";
                                PreparedStatement prepState = conn.prepareStatement(account_insert, new String[] { "account_num" });
                                prepState.setInt(1, rs.getInt(idName));
                                int account_num = prepState.executeUpdate();//0 or 1, any use for account_num?
                            }
                        }
                    }
                }catch (java.sql.SQLException | java.lang.IllegalAccessException throwables) {
                    throwables.printStackTrace();
                }
            }//end if, checking for table
        }//end if, checking for entity
    }
}