package org.pepppt.core;

import org.json.JSONException;
import org.junit.Test;
import org.pepppt.core.exceptions.InvalidBackendEndpointException;
import org.pepppt.core.exceptions.InvalidBackendEndpointFormatException;
import org.pepppt.core.exceptions.MissingBackendEndpointException;

import static org.junit.Assert.*;

public class CoreApiEndpointsTest {

    @Test
    public void set() {
        String json = "{ \"endpoints\":[ " +
                "{ \"backend_API_Base_Url\" : \"ENDPOINT_URL\" }, " +
                "{ \"backend_API_broadcastkeys\" : \"ENDPOINT_URL\" }, " +
                "{ \"backend_API_GetMe_Url\" : \"ENDPOINT_URL\" }, " +
                "{ \"backend_API_registration_Url\" : \"ENDPOINT_URL\" }, " +
                "{ \"backend_API_auth_Url\" : \"ENDPOINT_URL\" }, " +
                "{ \"backend_API_getTan_Url\" : \"ENDPOINT_URL\" }, " +
                "{ \"backend_API_finish_Url\" : \"ENDPOINT_URL\" }, " +
                "{ \"backend_API_testLab_Url\" : \"ENDPOINT_URL\" }, " +
                "{ \"backend_API_sendData_Url\" : \"ENDPOINT_URL\" }, " +
                "{ \"backend_API_checkMessages_Url\" : \"ENDPOINT_URL\" }, " +
                "{ \"backend_API_getMessage_Url\" : \"ENDPOINT_URL\" } " +
                "{ \"backend_API_confirmMessage_Url\" : \"ENDPOINT_URL\" } " +
                "] }";

        String invalidnumber = "{ \"endpoints\":[ " +
                "{ \"backend_API_Base_Url\" : \"ENDPOINT_URL\" }, " +
                "{ \"backend_API_broadcastkeys\" : \"ENDPOINT_URL\" }, " +
                "{ \"backend_API_GetMe_Url\" : \"ENDPOINT_URL\" }, " +
                "{ \"backend_API_registration_Url\" : \"ENDPOINT_URL\" } " +
                "] }";

        String wrongname = "{ \"endpoints\":[ " +
                "{ \"wrong\" : \"ENDPOINT_URL\" }, " +
                "{ \"backend_API_broadcastkeys\" : \"ENDPOINT_URL\" }, " +
                "{ \"backend_API_GetMe_Url\" : \"ENDPOINT_URL\" }, " +
                "{ \"backend_API_registration_Url\" : \"ENDPOINT_URL\" }, " +
                "{ \"backend_API_auth_Url\" : \"ENDPOINT_URL\" } " +
                "] }";

        String duplicate = "{ \"endpoints\":[ " +
                "{ \"backend_API_Base_Url\" : \"ENDPOINT_URL\" }, " +
                "{ \"backend_API_Base_Url\" : \"ENDPOINT_URL\" }, " +
                "{ \"backend_API_GetMe_Url\" : \"ENDPOINT_URL\" }, " +
                "{ \"backend_API_registration_Url\" : \"ENDPOINT_URL\" }, " +
                "{ \"backend_API_auth_Url\" : \"ENDPOINT_URL\" } " +
                "] }";
        try {
            CoreApiEndpoints ae = new CoreApiEndpoints();
            ae.set(json);
        } catch (JSONException | InvalidBackendEndpointException | MissingBackendEndpointException | InvalidBackendEndpointFormatException e) {
            fail();
            e.printStackTrace();
        }

        try {
            CoreApiEndpoints ae = new CoreApiEndpoints();
            ae.set(invalidnumber);
            fail();
        } catch (JSONException e) {
            e.printStackTrace();
            fail();
        } catch (InvalidBackendEndpointException e) {
            e.printStackTrace();
            fail();
        } catch (MissingBackendEndpointException e) {
            e.printStackTrace();
            fail();
        } catch (InvalidBackendEndpointFormatException e) {
            e.printStackTrace();
        }

        try {
            CoreApiEndpoints ae = new CoreApiEndpoints();
            ae.set(wrongname);
            fail();
        } catch (JSONException e) {
            e.printStackTrace();
            fail();
        } catch (InvalidBackendEndpointException e) {
            e.printStackTrace();
        } catch (MissingBackendEndpointException e) {
            e.printStackTrace();
            fail();
        } catch (InvalidBackendEndpointFormatException e) {
            e.printStackTrace();
            fail();
        }

        try {
            CoreApiEndpoints ae = new CoreApiEndpoints();
            ae.set(duplicate);
            fail();
        } catch (JSONException e) {
            e.printStackTrace();
            fail();
        } catch (InvalidBackendEndpointException e) {
            e.printStackTrace();
            fail();
        } catch (MissingBackendEndpointException e) {
            e.printStackTrace();
        } catch (InvalidBackendEndpointFormatException e) {
            e.printStackTrace();
            fail();
        }

    }

    @Test
    public void check() {
    }
}