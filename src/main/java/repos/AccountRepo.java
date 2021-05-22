package repos;

import annotations.*;
import exceptions.InvalidFieldException;
import models.AppUser;
import models.ColumnNode;
import models.TransactionValues;
import models.UserAccount;
import util.*;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static models.AppUser.getId;

/**
 * @author Chris
 */
public class AccountRepo {
    public static void update(Object o, Object acc) {
        try (Connection conn = ConnectionFactory.getInstance().getConnection(o)) {
            Class<?> clazz = acc.getClass();
            Class<?> user = o.getClass();

            if (clazz.isAnnotationPresent(Entity.class)) {//if annotated as entity that has attributes to draw from
                if (clazz.isAnnotationPresent(Table.class)) {
                    Table table = clazz.getAnnotation(Table.class);
                    String tableName = table.name();
                    StringBuilder preparedStatement = new StringBuilder().append("UPDATE ").append(tableName);

                    String bal = null;
                    Field[] fields = clazz.getDeclaredFields();
                    for (Field f : fields) {
                        Column column = f.getAnnotation(Column.class);
                        if (column != null) {
                            if (f.getAnnotation(Column.class).name().equals("balance")) {
                                bal = f.getAnnotation(Column.class).name();
                            }
                        }
                    }
                    String uid = null;
                    int user_num;
                    Field[] userFields = user.getDeclaredFields();
                    for (Field uf : userFields) {
                        Id column = uf.getAnnotation(Id.class);
                        if (column != null) {
                            if (uf.getAnnotation(Id.class).name().equals("user_id")) {
                                uid = uf.getAnnotation(Id.class).name();
                            }
                        }//Set bal = bal + deposit
                        preparedStatement.append(" SET ").append(bal).append(" = ").append(bal).append(" + ").append(TransactionValues.getDeposit());
                        preparedStatement.append(" WHERE ").append(uid).append(" = ").append(AppUser.getId());

                        String sql = preparedStatement.toString();
                        try {
                            PreparedStatement pstmt = conn.prepareStatement(sql, new String[]{"user_id"});

                            pstmt.executeUpdate();
                            System.out.println("Check the database!");
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }
                    }//end if
                }//end if
            }


        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}