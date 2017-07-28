package org.jahia.modules.autocompletedsearch.actions;



import org.jahia.modules.autocompletesearch.utils.AutoCompleteSearchCacheUtils;
import org.jahia.services.content.JCRNodeIteratorWrapper;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.query.QueryResultWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;
import org.json.JSONArray;
import org.json.JSONObject;

import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;

import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by julianmaurel on 2017-07-27.
 */
public class SearchAction extends AbstractFilter {


    protected CacheService cacheService;
    protected String nodetype;
    protected String propertyName;

    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {


        Cache<String, String> autoCompleteSearchCache = AutoCompleteSearchCacheUtils.initCache(resource.getNode().getSession(), cacheService, resource.getNode().getResolveSite().getSiteKey(), nodetype, propertyName);

        List<String> result = autoCompleteSearchCache.getKeys().stream()
                .filter(item -> item.contains(renderContext.getRequest().getParameter("term").toLowerCase()))
                .collect(Collectors.toList());

        JSONArray json = new JSONArray();

       for (int i = 0; i < result.size() ; i++) {

           String jsonString = (String) autoCompleteSearchCache.get(result.get(i));
           JSONObject jsonObject = new JSONObject(jsonString);
           jsonObject.put("value", jsonObject.get("title"));
           json.put(jsonObject);
        }

        renderContext.getRequest().getSession().setAttribute("jsonArray", json.toString());
        return null;
    }
    public CacheService getCacheService() {
        return cacheService;
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public String getNodetype() {
        return nodetype;
    }

    public void setNodetype(String nodetype) {
        this.nodetype = nodetype;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
}
