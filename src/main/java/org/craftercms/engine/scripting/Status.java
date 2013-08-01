package org.craftercms.engine.scripting;

/**
 * Object used in REST service scripts to indicate the status result of the service.
 *
 * @author Alfonso Vásquez
 */
public class Status {

    private int code;
    private String message;
    private Throwable exception;
    private boolean redirect;

    public Status() {
        code = 200;
        redirect = false;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    public boolean isRedirect() {
        return redirect;
    }

    public void setRedirect(boolean redirect) {
        this.redirect = redirect;
    }

}
