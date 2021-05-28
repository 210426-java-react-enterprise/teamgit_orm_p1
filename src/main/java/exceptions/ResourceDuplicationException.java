package exceptions;

public class ResourceDuplicationException extends RuntimeException {

    public ResourceDuplicationException(){
        super("You have inputted duplicate resources that are already taken!");
    }
}
