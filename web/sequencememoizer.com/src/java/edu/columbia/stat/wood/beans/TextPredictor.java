/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.beans;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 *
 * @author fwood
 */
@WebService()
public class TextPredictor {

    /**
     * Web service operation
     */
    @WebMethod(operationName = "predict")
    public String predict(@WebParam(name = "context")
    String context) {
        //TODO write your implementation code here:
        return null;
    }

}
