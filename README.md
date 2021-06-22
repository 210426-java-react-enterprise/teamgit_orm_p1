# Custom Object Relational Mapping (ORM) Framework
Our custom ORM is used to perform basic CRUD functions through Java using the Reflections API.

# Technologies Used

Java - version 1.8
PostgreSQL - version 42.2.12
Maven - version 3.8.1
Maven - version 3.8.1
Git - version 2.31.1
JUnit - version 4.13.2
Postman - version 8.3.0
Tomcat - version 9.0.46
JSON Web Tokens - version 0.9.1
AWS RDS
AWS CodeBuild
AWS CodePipeline
AWS S3
AWS Elastic Beanstalk

# Features
- Agnostic table creation
- Agnostic data insert, update, delete, and select from the database

To-do list:
- Clean up functionality to ensure agnostic functionality for all kinds of Entities

# Getting Started
Clone remote repository of the ORM onto your local repository using the following command:
```
git clone https://github.com/210426-java-react-enterprise/teamgit_orm_p1.git
```
If you want to have access to the webapp that leverages the ORM, make sure to clone the repository:
```
git clone https://github.com/210426-java-react-enterprise/teamgit_webapp_p1.git
```

# Usage
Once you've installed the API, make sure to run a maven install to add the dependency to your local repository:
```
mvn install
```
Once the dependency is setup, and you want to try out the webapp, you can then import the dependency as a maven dependency in the webapp:
```
       <dependency>
            <groupId>teamgit.orm</groupId>
            <artifactId>teamgit_orm_p1</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
```


# Contributors
Kevin Chang  
Christopher Levano  
Thomas Diendorf
