/*******************************************************************************
 * Copyright 2013-2015 Esri
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 ******************************************************************************/
package com.esri.militaryapps.controller;

import com.esri.militaryapps.model.DomNodeAndDocument;
import com.esri.militaryapps.model.Geomessage;
import com.esri.militaryapps.util.Utilities;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Sends chem light messages to listening clients.
 */
public class ChemLightController {
    
    /**
     * The type string for this controller's Geomessages.
     */
    public static final String REPORT_TYPE = "chemlight";
    
    private static final Logger logger = Logger.getLogger(ChemLightController.class.getName());

    private final MessageController messageController;
    
    private String uniqueDesignation = null;

    /**
     * Creates a new ChemLightController for the application.
     * @param messageController the controller that this controller will
     *                                  use to send the message.
     * @param uniqueDesignation the unique designation of the sender. Typically
     *        this is a human-readable username but it could be a UUID if desired.
     */
    public ChemLightController(MessageController messageController, String uniqueDesignation) {
        this.messageController = messageController;
        this.uniqueDesignation = uniqueDesignation;
    }

    /**
     * Sends a chem light message to listening clients, using longitude and latitude.
     * This is shorthand for <code>sendChemLight(longitude, latitude, 4326, rgbColor)</code>.
     * @param longitude the chem light's longitude.
     * @param latitude the chem light's latitude.
     * @param color the chem light's color, represented as an aRGB integer. See
     *              Utilities.getAFMGeoEventColorString to learn more about chem
     *              light colors in ArcGIS Runtime.
     * @throws ParserConfigurationException if a DocumentBuilder cannot be created
     *         for rendering the message's XML.
     * @see com.esri.militaryapps.controller.ChemLightController#sendChemLight(double, double, int, int)
     */
    public void sendChemLight(double longitude, double latitude, int rgbColor) throws ParserConfigurationException {
        sendChemLight(longitude, latitude, 4326, rgbColor);
    }
    
    /**
     * Sends a chem light message to listening clients, using longitude and latitude.
     * If <code>id</code> is not null or empty and matches the ID of an existing chem light,
     * this method updates the location and color of that chem light; otherwise,
     * this method sends a new chem light.
     * This is shorthand for <code>sendChemLight(longitude, latitude, 4326, rgbColor, id)</code>.
     * @param longitude the chem light's longitude.
     * @param latitude the chem light's latitude.
     * @param color the chem light's color, represented as an aRGB integer. See
     *              Utilities.getAFMGeoEventColorString to learn more about chem
     *              light colors in ArcGIS Runtime.
     * @param id a unique ID for the chem light to be updated, or null to send a new chem light.
     * @throws ParserConfigurationException if a DocumentBuilder cannot be created
     *         for rendering the message's XML.
     * @see com.esri.militaryapps.controller.ChemLightController#sendChemLight(double, double, int, int)
     */
    public void sendChemLight(double longitude, double latitude, int rgbColor, String id) {
        sendChemLight(longitude, latitude, 4326, rgbColor, id);
    }
    
    /**
     * Sends a chem light message to listening clients. If this application has a
     * UDPMessageGraphicsLayerController or other mechanism to receive UDP messages,
     * the chem light should appear on the map soon after this method returns.
     * This is shorthand for <code>sendChemLight(x, y, spatialReferenceWkid, rgbColor, null)</code>.
     * @param x the chem light's X-coordinate.
     * @param y the chem light's Y-coordinate.
     * @param spatialReferenceWkid the WKID of the chem light's spatial reference.
     * @param color the chem light's color, represented as an aRGB integer. See
     *              Utilities.getAFMGeoEventColorString to learn more about chem
     *              light colors in ArcGIS Runtime.
     * @throws ParserConfigurationException if a DocumentBuilder cannot be created
     *         for rendering the message's XML.
     * @see com.esri.militaryapps.util.Utilities#getAFMGeoEventColorString(int)
     */
    public void sendChemLight(double x, double y, int spatialReferenceWkid, int rgbColor) {
        sendChemLight(x, y, spatialReferenceWkid, rgbColor, null);
    }
    
    /**
     * Sends a chem light message to listening clients. If this application has a
     * UDPMessageGraphicsLayerController or other mechanism to receive UDP messages,
     * the chem light should appear on the map soon after this method returns.
     * If <code>id</code> is not null or empty and matches the ID of an existing chem light,
     * this method updates the location and color of that chem light; otherwise,
     * this method sends a new chem light.
     * @param x the chem light's X-coordinate.
     * @param y the chem light's Y-coordinate.
     * @param spatialReferenceWkid the WKID of the chem light's spatial reference.
     * @param color the chem light's color, represented as an aRGB integer. See
     *              Utilities.getAFMGeoEventColorString to learn more about chem
     *              light colors in ArcGIS Runtime.
     * @param id a unique ID for the chem light to be updated, or null to send a new chem light.
     * @throws ParserConfigurationException if a DocumentBuilder cannot be created
     *         for rendering the message's XML.
     * @see com.esri.militaryapps.util.Utilities#getAFMGeoEventColorString(int)
     */
    public void sendChemLight(double x, double y, int spatialReferenceWkid, int rgbColor, String id) {
        try {
            if (null == id) {
                id = UUID.randomUUID().toString();
            }
            DomNodeAndDocument nodeAndDocument = Utilities.createGeomessageDocument();
            Document doc = nodeAndDocument.getDocument();
            Node geomessageElement = nodeAndDocument.getNode();
            
            Utilities.addTextElement(doc, geomessageElement,
                    Geomessage.TYPE_FIELD_NAME, REPORT_TYPE);
            Utilities.addTextElement(doc, geomessageElement,
                    Geomessage.ID_FIELD_NAME, id);
            Utilities.addTextElement(doc, geomessageElement,
                    Geomessage.WKID_FIELD_NAME, Integer.toString(spatialReferenceWkid));
            Utilities.addTextElement(doc, geomessageElement,
                    Geomessage.CONTROL_POINTS_FIELD_NAME, x + "," + y);
            Utilities.addTextElement(doc, geomessageElement,
                    Geomessage.ACTION_FIELD_NAME, "UPDATE");
            if (null != uniqueDesignation) {
                Utilities.addTextElement(doc, geomessageElement, "uniquedesignation", uniqueDesignation);
            }
            Utilities.addTextElement(doc, geomessageElement, "color", Utilities.getAFMGeoEventColorString(rgbColor));
            String dateString = Utilities.DATE_FORMAT_GEOMESSAGE.format(new Date());
            Utilities.addTextElement(doc, geomessageElement, "datetimesubmitted", dateString);
            Utilities.addTextElement(doc, geomessageElement, "datetimemodified", dateString);
            
            messageController.sendMessage(doc);
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Could not send chem light", t);
        }
    }
    
    /**
     * Sends a REMOVE message for a chem light with the specified unique ID.
     * @param id the chem light's unique ID.
     */
    public void removeChemLight(String id) {
        if (null != id) {
            try {
                DomNodeAndDocument nodeAndDocument = Utilities.createGeomessageDocument();
                Document doc = nodeAndDocument.getDocument();
                Node geomessageElement = nodeAndDocument.getNode();

                Utilities.addTextElement(doc, geomessageElement,
                        Geomessage.TYPE_FIELD_NAME, REPORT_TYPE);
                Utilities.addTextElement(doc, geomessageElement,
                        Geomessage.ID_FIELD_NAME, id);
                Utilities.addTextElement(doc, geomessageElement,
                        Geomessage.ACTION_FIELD_NAME, "REMOVE");

                messageController.sendMessage(doc);
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "Could not send chem light remove message", t);
            }
        }
    }

    /**
     * Returns the sender's unique designation.
     * @return the unique designation of the sender. Typically this is a human-readable
     *         username but it could be a UUID if desired.
     */
    public String getUniqueDesignation() {
        return uniqueDesignation;
    }

    /**
     * Sets the sender's unique designation.
     * @param uniqueDesignation the unique designation of the sender. Typically
     *        this is a human-readable username but it could be a UUID if desired.
     */
    public void setUniqueDesignation(String uniqueDesignation) {
        this.uniqueDesignation = uniqueDesignation;
    }
    
}
