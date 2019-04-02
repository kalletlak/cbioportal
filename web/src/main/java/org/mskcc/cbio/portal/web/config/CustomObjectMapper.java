package org.mskcc.cbio.portal.web.config;

import java.util.HashMap;
import java.util.Map;

import org.cbioportal.web.parameter.Session;
import org.cbioportal.web.parameter.SessionData;
import org.cbioportal.web.parameter.VirtualStudyData;
import org.cbioportal.weblegacy.mixin.SessionDataMixin;
import org.cbioportal.weblegacy.mixin.SessionMixin;
import org.cbioportal.weblegacy.mixin.VirtualStudyDataMixin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


public class CustomObjectMapper extends ObjectMapper {

    public CustomObjectMapper() {

        super.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        super.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        Map<Class<?>, Class<?>> mixinMap = new HashMap<>();
        mixinMap.put(Session.class, SessionMixin.class);
        mixinMap.put(SessionData.class, SessionDataMixin.class);
        mixinMap.put(VirtualStudyData.class, VirtualStudyDataMixin.class);
        super.setMixIns(mixinMap);
    }
}