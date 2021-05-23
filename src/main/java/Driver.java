import models.AppUser;
import repos.*;


import java.net.MalformedURLException;

public class Driver {


    public static void main(String[] args) throws MalformedURLException, ClassNotFoundException {

        Repo repo = new Repo();

        //AppUser contains @Connection with application properties injected

       // AppUser appUser = new AppUser("swekevin", "password!", "kevin.chang@revature.net", "Kevin", "Chang", "1998-12-20");
//        System.out.println("+-----------------+");
//        userRepo.execute(appUser);
//        System.out.println("+-----------------+");
        AppUser appUser2 = new AppUser();
        AppUser a = new AppUser();

        repo.create(a);

        //users data: PK user_id, username, password, firstname, lastname, email, dob
        //accounts: PK account_id, FK user_id, balance
        //transactions: FK account_id, transaction_id, date/time, previous_balance, change (deposit + or withdrawal -)

        //ORM takes in Object
        //Extract from Object to access its data using reflection
        //ORM should create a table for each new Class that is passed in
        //Each Class instance will make up a row of the table


        //Object has annotations that denote what fields can be persisted
        //ORM scrapes the Class for certain annotations that label what data/fields it's looking for
        //ORM takes those fields and persists it to database

        /* This code was before we implemented annotation "Connection.class"

        AppUser appUser = new AppUser("usernametest2", "pw", "emailtest2@gmail.com", "User", "Name", "1998-12-20");

   
        appUser.setId(34);
        UserAccount userAcc = new UserAccount(7, 0);
        TransactionValues transactionValues = new TransactionValues(0, 700);
        Map<String, String> applicationProperties = new HashMap<>();
        applicationProperties.put("host-url", "jdbc:postgresql://project0.cmu8byclpwye.us-east-1.rds.amazonaws.com:5432/postgres?currentSchema=public");
        applicationProperties.put("username", "postgres");
        applicationProperties.put("password", "Octopu5!");

        try(Connection conn = util.ConnectionFactory.getInstance().getConnection(applicationProperties)){
            Driver driver = new Driver();
            System.out.println("+-----------------+");


            //repos.UserRepo.save(appUser, conn);//repos is a package, UserRepo is class in the package
            //repos.UserRepo.createTable(appUser, conn);
            repos.AccountRepo.withdraw(appUser, userAcc, conn);
            System.out.println("+-----------------+");
        }catch (SQLException throwables) {
            throwables.printStackTrace();
        };

         */



        /*

        Class<?> appClass = appUser.getClass();
        String className = appClass.getSimpleName();
        System.out.println("This is the name of the class: " + className);
        if(appClass.isAnnotationPresent(Entity.class)){
            //anyClass.getAnnotations();
            //anyClass.getDeclaredFields();

            //driver.exploreClassMembers(anyClass);
        }
        Field[] fields = appClass.getDeclaredFields();



        //the names we gave the column on the annotation
        for (Field f: appClass.getDeclaredFields()) {
            Column column = f.getAnnotation(Column.class);
            if (column != null)
                System.out.println(column.name());
        }

        //the names of the actual field
        /*for(Field field : fields){
            System.out.println(field.getName());
        }


        for(Field field : fields){
            System.out.println(field.getType());


       */





//        String packageName = "src.main.java.models";
//
//        List<Class<?>> entityClasses = driver.getClassesInPackageWithConstraints(packageName,
//                clazz -> clazz.isAnnotationPresent(Entity.class));
//
//
//        for (Class<?> entity : entityClasses) {
//            driver.exploreClassMembers(entity);
//            System.out.println();
//        }
//
//        driver.exploreClassMembers(driver.getClass());





    }//end main


    /*
    public List<Class<?>> getClassesInPackageWithConstraints(String packageName, Predicate<Class<?>> predicate) throws MalformedURLException, ClassNotFoundException {

        List<Class<?>> packageClasses = new ArrayList<>();
        List<String> classNames = new ArrayList<>();

        File packageDirectory = new File("target/classes/" + packageName.replace('.', '/'));

        for (File file : Objects.requireNonNull(packageDirectory.listFiles())) {
            if (file.isDirectory()) {
                packageClasses.addAll(getClassesInPackageWithConstraints(packageName + "." + file.getName(), predicate));
            } else if (file.getName().contains(".class")) {
                classNames.add(file.getName());
            }
        }
        return packageClasses;
    }

    public void exploreClassMembers(Class<?> clazz) {
        System.out.println("Class name: " + clazz);

        Annotation[] classAnnotations = clazz.getAnnotations();
        printMembers(classAnnotations, "Annotations");

        Field[] declaredClassFields = clazz.getDeclaredFields();
        printMembers(declaredClassFields, "Declared Fields");

        Method[] declaredClassMethods = clazz.getDeclaredMethods();
        printMembers(declaredClassMethods, "Declared Methods");

        Field[] classFields = clazz.getFields();
        printMembers(classFields, "All Fields");

        Method[] classMethods = clazz.getMethods();
        printMembers(classMethods, "All Methods");

    }

    private void printMembers(Object[] members, String memberType) {
        if (members.length != 0) {
            System.out.println("\t" + memberType + " on class: ");
            for (Object o : members) {
                System.out.println("\t\t- " + o);
            }
        }
    }

    */
}

