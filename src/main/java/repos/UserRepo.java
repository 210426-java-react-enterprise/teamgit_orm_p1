package repos;

import models.AppUser;

import java.util.List;
import annotations.Entity;
import annotations.Table;
import annotations.Column;
import annotations.Id;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import models.ColumnNode;

/**
 * Takes in any object and constructs a query
 * @author Kevin Chang
 * @author Thomas Diendorf
 * @author Chris Levano
 */
public class UserRepo {

    public void testMethod(){
        Class appUserClass = AppUser.class;
    }


    public static void save(Object o){
        Class<?> clazz = o.getClass();
        if(clazz.isAnnotationPresent(Entity.class)){//if annotated as entity that has attributes to draw from
            if(clazz.isAnnotationPresent(Table.class)){
                Table table = clazz.getAnnotation(Table.class);
                String tableName = table.name();//table name == "users"
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

                String finalString = preparedStatement.toString();


            }
        }

    }

}

