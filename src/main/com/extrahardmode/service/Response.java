package com.extrahardmode.service;

import com.extrahardmode.service.config.Status;

/**
 * Attach some status information to a returned value
 * For example Status.OK when everything went fine
 */
public class Response<T>
{
    /**
     * Statuscode of this Response
     */
    private Status status;
    /**
     * Object to return
     */
    private T response;

    /**
     * A parameterized Response with StatusCode
     * @param status
     * @param response
     */
    public Response (Status status, T response)
    {
        this.status = status;
        this.response = response;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other instanceof Response)
        {
            Response otherR = (Response) other;
            return otherR.getStatusCode() == this.getStatusCode() && otherR.getContent() == this.getContent();
        }
        else
            return false;
    }

    /**
     * Get the Status of this Response
     * @return
     */
    public Status getStatusCode()
    {
        return status;
    }

    /**
     * Get the actual content of the response
     * @return
     */
    public T getContent ()
    {
        return response;
    }

    /**
     * Set the status of the Response
     * @param status code to set
     */
    public void setStatus(Status status)
    {
        this.status = status;
    }

    /**
     * Set the returned content of the Response
     * @param response to set
     */
    public void setContent (T response)
    {
        this.response = response;
    }
}
