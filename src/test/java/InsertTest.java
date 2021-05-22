import models.AppUser;
import org.junit.After;
import org.junit.Before;
//import static org.mockito.Mockito.mock;
import org.junit.Test;
import repos.UserRepo;

public class InsertTest {

    UserRepo userRepo;
    AppUser appUser;

    @Before
    public void setupTest(){
        userRepo = new UserRepo();

        appUser = new AppUser("tester", "password", "test@revature.net",
                "test", "person", "1990-01-01");
    }

    @After
    public void tearDownTest(){
        userRepo = null;
        appUser = null;
    }

    @Test
    public void insertValuesTest(){
        userRepo.create(appUser);
    }

}
