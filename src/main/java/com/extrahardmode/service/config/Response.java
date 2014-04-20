/*
 * This file is part of
 * ExtraHardMode Server Plugin for Minecraft
 *
 * Copyright (C) 2012 Ryan Hamshire
 * Copyright (C) 2013 Diemex
 *
 * ExtraHardMode is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ExtraHardMode is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero Public License
 * along with ExtraHardMode.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.extrahardmode.service.config;


/**
 * Attach some status information to a returned value For example Status.OK when everything went fine
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
     */
    public Response(Status status, T response)
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
        } else
            return false;
    }


    /**
     * Get the Status of this Response
     */
    public Status getStatusCode()
    {
        return status;
    }


    /**
     * Get the actual content of the response
     */
    public T getContent()
    {
        return response;
    }


    /**
     * Set the status of the Response
     *
     * @param status code to set
     */
    public void setStatus(Status status)
    {
        this.status = status;
    }


    /**
     * Set the returned content of the Response
     *
     * @param response to set
     */
    public void setContent(T response)
    {
        this.response = response;
    }
}
