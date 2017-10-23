package org.jahia.modules.autocompletesearch.utils;

import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.content.*;
import org.jahia.services.query.QueryResultWrapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.StringEscapeUtils;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import java.security.AuthProvider;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by julianmaurel on 2017-07-28.
 */
public class AutoCompleteSearchCacheUtils {

    private static Logger logger = LoggerFactory.getLogger(AutoCompleteSearchCacheUtils.class);

    public AutoCompleteSearchCacheUtils() {
    }

    private static JSONObject extractProperties(JCRNodeWrapper node) throws JSONException {

        JSONObject jsonObject = new JSONObject();
        String title = "";
        try {
            if (node.hasProperty("jcr:title")) {
                title = node.getPropertyAsString("jcr:title");
            }
        } catch (javax.jcr.RepositoryException e) {
            logger.warn("Could not get title for node " + node.getPath() + " " + e.getMessage());
        }
        if ("".equals(title)) {
            title = JCRContentUtils.getParentOfType(node, "jnt:page").getDisplayableName();
        }
        title = StringEscapeUtils.unescapeHtml(title);

        jsonObject.put("title", title);
        jsonObject.put("path", node.getPath());
        jsonObject.put("url", JCRContentUtils.getParentOfType(node, "jnt:page").getUrl());
        JCRNodeWrapper category = JCRContentUtils.getParentOfType(node, "jmix:autoCompletedSearchCategory");
        if (category == null) {
            jsonObject.put("category", "");
        } else {
            jsonObject.put("category", category.getDisplayableName());
        }
        return jsonObject;
    }

    public static Cache<String, String> initCache(JCRSessionWrapper session, CacheService cacheService, String siteKey, String nodetype, String propertyName) {
        Cache<String, String> autoCompleteSearchCache = null;
        try {
            autoCompleteSearchCache = cacheService.getCache("autoCompleteSearchCache", true);
        } catch (JahiaInitializationException e) {
            e.printStackTrace();
            return null;
        }


        if (autoCompleteSearchCache.isEmpty()) {
            String[] nodetypes = nodetype.split(" ");

            QueryManager queryManager = session.getWorkspace().getQueryManager();


            Query query = null;

            try {
                //query = queryManager.createQuery("SELECT * FROM [" + nodetype + "] as news WHERE ISDESCENDANTNODE('/sites/" + siteKey + "')", Query.JCR_SQL2);
                String statement = "/jcr:root//element(*,jacademix:isVersionPage)[@version='current']//element(*,jacademy:document)";
                logger.debug("statement is : " + statement);

                query = queryManager.createQuery(statement, Query.XPATH);

                QueryResultWrapper queryResult = (QueryResultWrapper) query.execute();
                JCRNodeIteratorWrapper nodes = queryResult.getNodes();


                JSONArray json = new JSONArray();
                while (nodes.hasNext()) {
                    JCRNodeWrapper node = (JCRNodeWrapper) nodes.next();
                    String nodeContent = node.getPropertyAsString(propertyName);
                    JSONObject jsonObject = null;

                    try {
                        jsonObject = extractProperties(node);

                        String title = "";
                        try {
                            if (node.hasProperty("jcr:title")) {
                                title = node.getPropertyAsString("jcr:title");
                            }
                        } catch (javax.jcr.RepositoryException e) {
                            logger.warn("Could not get title for node " + node.getPath() + " " + e.getMessage());
                        }

                        if ("".equals(title)) {
                            title = JCRContentUtils.getParentOfType(node, "jnt:page").getDisplayableName();
                        }

                        if (title != null) {
                            title = StringEscapeUtils.unescapeHtml(title);
                            autoCompleteSearchCache.put(title.toLowerCase(), jsonObject.toString());
                        }


                    } catch (JSONException e) { //FIXME
                        e.printStackTrace();
                    }
                    if (null != nodeContent) {
                        Pattern p = Pattern.compile("<h[1-6]>(.*?)</h[1-6]>", Pattern.DOTALL);

                        Matcher m = p.matcher(nodeContent);
                        try {

                            while (m.find()) {
                                AutoCompleteSearchCacheUtils.logger.debug("h:" + m.group(1));
                                JSONObject jsonSubTitleObject = extractProperties(node);
                                jsonSubTitleObject.put("type", "heading");
                                String headingName = m.group(1).replaceAll("\\<[^>]*>", "");
                                //jsonSubTitleObject.put("headingName", m.group(1).toLowerCase());
                                headingName = StringEscapeUtils.unescapeHtml(headingName);
                                jsonSubTitleObject.put("headingName", headingName.replaceAll("^[^A-Za-z]*", ""));
                                String fragment = headingName.replaceAll("[^A-Za-z0-9]+", "_").replaceAll("\\s+", "_").replaceAll("^[^A-Za-z]*", "");
                                String url = (String) jsonSubTitleObject.get("url");
                                jsonSubTitleObject.put("url", url + "#" + fragment);

                                autoCompleteSearchCache.put(m.group(1).toLowerCase(), jsonSubTitleObject.toString());
                            }
                        } catch (JSONException e) { //FIXME
                            e.printStackTrace();
                        }


                    }

                }
            } catch (RepositoryException e) { // FIXME
                e.printStackTrace();
            }

        }
        return autoCompleteSearchCache;
    }
}
