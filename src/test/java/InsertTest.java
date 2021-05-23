import models.AppUser;
import org.junit.After;
import org.junit.Before;
import static org.mockito.Mockito.*;
import org.junit.Test;
import org.mockito.Mock;
import repos.UserRepo;
import util.ConnectionFactory;

import java.sql.Connection;

public class InsertTest {

    UserRepo userRepo;
    AppUser appUser;
    Connection conn;

    /*@Mock
    AppUser mockAppUser;
    Connection mockConn;*/

    @Before
    public void setupTest(){
        userRepo = new UserRepo();
        appUser = new AppUser("tester2", "password", "test2@revature.net",
                "test", "person", "1990-01-01");

        conn = ConnectionFactory.getInstance().getConnection(appUser);

        /*mockAppUser = mock(AppUser.class);
        mockAppUser.setUsername("MockTester");
        mockAppUser.setPassword("password");
        mockAppUser.setEmail("mocktest@revature.net");
        mockAppUser.setFirstName("Mock");
        mockAppUser.setLastName("Person");
        mockAppUser.setDob("1990-01-01");

        mockConn = ConnectionFactory.getInstance().getConnection(mockAppUser);*/
    }

    @After
    public void tearDownTest(){

        userRepo = null;
        appUser = null;
        conn = null;
        /*mockConn = null;
        mockAppUser = null;*/
    }

    @Test
    public void insertValuesTest(){
        userRepo.insert(appUser, conn);
        userRepo.delete(appUser, conn);
        //userRepo.create(mockAppUser);
        //userRepo.insert(mockAppUser, mockConn);
    }


}
