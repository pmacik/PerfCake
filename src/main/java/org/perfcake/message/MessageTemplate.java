/*
 * -----------------------------------------------------------------------\
 * PerfCake
 *  
 * Copyright (C) 2010 - 2013 the original author or authors.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -----------------------------------------------------------------------/
 */
package org.perfcake.message;

import org.perfcake.util.StringTemplate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.List;
import java.util.Properties;

/**
 * Holds a message template based on a provided sample, keeps references to configured validators and renders the message before it is actually sent.
 * Rendering means properties substitution in the message payload.
 * Logging is very minimalistic as this class is very simple and we want to maximize its performance.
 *
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 * @author <a href="mailto:pavel.macik@gmail.com">Pavel Macík</a>
 */
public class MessageTemplate implements Serializable {
   private static final long serialVersionUID = 6172258079690233417L;

   private transient Logger log = LogManager.getLogger(MessageTemplate.class);

   /**
    * Original message sample.
    */
   private final Message message;

   /**
    * How many times the message should be sent in one iteration?
    */
   private final long multiplicity;

   /**
    * The list of validator references that should be used to validate a response to this message.
    */
   private final List<String> validatorIds;

   /**
    * Does the original message contains anything to be replaced?
    */
   private final boolean isStringMessage;

   /**
    * A template prepared to be rendered.
    */
   private transient StringTemplate template;

   /**
    * Creates a new template based on the message sample. Multiplicity and validator references can be provided as well.
    *
    * @param message
    *       A sample message.
    * @param multiplicity
    *       How many times the message should be sent in one iteration.
    * @param validatorIds
    *       List of validators to validate a response.
    */
   public MessageTemplate(final Message message, final long multiplicity, final List<String> validatorIds) {
      this.message = message;
      this.isStringMessage = message.getPayload() instanceof String;
      if (isStringMessage) {
         prepareTemplate();
      }
      this.multiplicity = multiplicity;
      this.validatorIds = validatorIds;
   }

   /**
    * Gets the original sample message.
    *
    * @return The original sample message.
    */
   public Message getMessage() {
      return message;
   }

   /**
    * Gets a new message instance.
    *
    * @return A new message.
    */
   private static Message newMessage() {
      return new Message();
   }

   /**
    * Renders the message template.
    *
    * @param properties
    *       Properties to be replaced in the message payload.
    * @return A new message instance with the rendered payload.
    */
   public Message getFilteredMessage(final Properties properties) {
      if (isStringMessage && template != null && template.hasPlaceholders()) {
         final Message m = newMessage();

         m.setPayload(template.toString(properties));
         m.setHeaders(message.getHeaders());
         m.setProperties(message.getProperties());

         return m;
      } else {
         return message;
      }
   }

   private void prepareTemplate() {
      // find out if there are any attributes in the text message to be replaced
      final StringTemplate tmpTemplate = new StringTemplate((String) message.getPayload());

      if (tmpTemplate.hasPlaceholders()) {
         if (log.isDebugEnabled()) {
            log.debug("Created matching pattern for the message payload with properties.");
         }
         this.template = tmpTemplate;
      } else {
         // return the rendered template back, it might not have any placeholders now, but there could have been some math replacements etc.
         message.setPayload(tmpTemplate.toString());
      }
   }

   /**
    * How many times the message should be sent in one iteration?
    *
    * @return The number of how many times the message should be sent in one iteration.
    */
   public Long getMultiplicity() {
      return multiplicity;
   }

   /**
    * Gets the list of validator references that should be used to validate a response to this message.
    *
    * @return The list of validator references that should be used to validate a response to this message.
    */
   public List<String> getValidatorIds() {
      return validatorIds;
   }

   // fill in the transient field
   private void readObject(java.io.ObjectInputStream stream) throws java.io.IOException, ClassNotFoundException {
      stream.defaultReadObject();
      if (isStringMessage) {
         prepareTemplate();
      }
   }
}
