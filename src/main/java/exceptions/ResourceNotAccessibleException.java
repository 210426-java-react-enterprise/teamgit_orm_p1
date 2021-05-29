package exceptions;

public class ResourceNotAccessibleException extends RuntimeException{
    public ResourceNotAccessibleException(){
        super("One of the fields you are attempting to access is not accessible!");
    }
}