//package services;
//
//import repos.*;
//import util.*;
//
//import java.sql.*;
//import java.util.*;
//
//public class UserService {
//
//    private UserRepo userRepo;
//
//    public UserService(UserRepo userRepo) {
//        this.userRepo = userRepo;
//    }
//
//    public void authenticate(Object o, Map<String, String> applicationProperties){
//        try(Connection conn = ConnectionFactory.getInstance().getConnection(applicationProperties)){
//
//            userRepo.save(o, conn);
//
//        }catch(SQLException e){
//            e.printStackTrace();
//        }
//    }
//
//
//

//}

