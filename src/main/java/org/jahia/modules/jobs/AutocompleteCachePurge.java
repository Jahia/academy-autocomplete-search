package org.jahia.modules.jobs;

import org.jahia.modules.autocompletesearch.utils.AutoCompleteSearchCacheUtils;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.render.filter.cache.ModuleCacheProvider;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.scheduler.JobHistoryPurgeJob;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutocompleteCachePurge  extends BackgroundJob {

    private static final Logger logger = LoggerFactory.getLogger(AutocompleteCachePurge.class);

    @Override
    public void executeJahiaJob(JobExecutionContext ctx) throws Exception {
        CacheService cacheService = (CacheService) SpringContextSingleton.getBean("JahiaCacheService");
        Cache autoCompleteSearchCache = cacheService.getCache("autoCompleteSearchCache", true);
        logger.info("Flushing " + autoCompleteSearchCache.size() + " autoCompleteSearchCache entries...");
        autoCompleteSearchCache.flush();
    }
}
