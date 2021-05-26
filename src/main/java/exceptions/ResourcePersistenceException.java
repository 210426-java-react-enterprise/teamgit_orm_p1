package exceptions;

public class ResourcePersistenceException extends RuntimeException{

    public ResourcePersistenceException(){
        super("There was a problem when trying to persist the resource.");
    }

}
