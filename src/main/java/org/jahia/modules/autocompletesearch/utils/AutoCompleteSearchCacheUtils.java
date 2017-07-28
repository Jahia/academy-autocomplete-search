package org.jahia.modules.autocompletesearch.utils;

import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeIteratorWrapper;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.query.QueryResultWrapper;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

/**
 * Created by julianmaurel on 2017-07-28.
 */
public class AutoCompleteSearchCacheUtils {


    public AutoCompleteSearchCacheUtils() {}

    public static Cache<String, String> initCache(JCRSessionWrapper session, CacheService cacheService, String siteKey) {
        Cache<String, String> autoCompleteSearchCache = null;
        try {
            autoCompleteSearchCache = cacheService.getCache("autoCompleteSearchCache", true);
        } catch (JahiaInitializationException e) {
            e.printStackTrace();
            return null;
        }

        System.out.println("cache keys:" + autoCompleteSearchCache.getKeys().size() +" is empty: " +autoCompleteSearchCache.isEmpty());
        if (autoCompleteSearchCache.isEmpty()) {
            try {
                QueryManager queryManager= session.getWorkspace().getQueryManager();
                Query query = queryManager.createQuery("SELECT * FROM [jacademy:document] as news WHERE ISDESCENDANTNODE('/sites/"+siteKey+"')", Query.JCR_SQL2);
              //  Query query = queryManager.createQuery("SELECT * FROM [jnt:news] as news WHERE ISDESCENDANTNODE('/sites/"+siteKey+"')", Query.JCR_SQL2);
                QueryResultWrapper queryResult = (QueryResultWrapper) query.execute();
                JCRNodeIteratorWrapper nodes = queryResult.getNodes();


                JSONArray json = new JSONArray();
                while (nodes.hasNext()) {
                    JSONObject jsonObject = new JSONObject();
                    JCRNodeWrapper node = (JCRNodeWrapper) nodes.next();
                    jsonObject.put("title", node.getPropertyAsString("jcr:title"));
                    jsonObject.put("path", node.getPath());
                    jsonObject.put("url", JCRContentUtils.getParentOfType(node, "jnt:page").getUrl());
                    JCRNodeWrapper category = JCRContentUtils.getParentOfType(node, "jmix:autoCompletedSearchCategory");
                    if (category == null) {
                        jsonObject.put("category", "");
                    } else {
                        jsonObject.put("category", category.getPropertyAsString("jcr:title"));
                    }
                    autoCompleteSearchCache.put(node.getPropertyAsString("jcr:title").toLowerCase(), jsonObject.toString());
                    System.out.println("adding key: " + node.getPropertyAsString("jcr:title") );
                }

            } catch (Exception e) { // FIXME
                e.printStackTrace();
            }
        }
        return autoCompleteSearchCache;
    }
}
