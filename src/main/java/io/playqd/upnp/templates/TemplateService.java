package io.playqd.upnp.templates;

import java.util.Map;

public interface TemplateService {

    String processToString(String templateName, Map<String, Object> data);
}
