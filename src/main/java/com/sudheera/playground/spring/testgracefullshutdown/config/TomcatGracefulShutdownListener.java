package com.sudheera.playground.spring.testgracefullshutdown.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.catalina.connector.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

/**
 * Custom Tomcat protocol handler's executor service shutdown listner implementation to avoid spring application context
 * to shutdown before shutting down the tomcat server threads. This bean will do following when the application context
 * closed event occurred.
 *
 * 1. Pausing connector to stop accepting new connections
 * 2. Invoke shutdown of the ThreadPoolExecutor used in the Tomcat protocol handler
 * 3. Await until ThreadPoolExecutor is completely shutdown
 *
 * Read : https://github.com/spring-projects/spring-boot/issues/4657 for more information regarding the issue, this
 * implementation is based on `wilkinsona`s proposed solution in the issue thread
 */
@Component
public class TomcatGracefulShutdownListener implements TomcatConnectorCustomizer, ApplicationListener<ContextClosedEvent> {

    private static final Logger log = LoggerFactory.getLogger(TomcatGracefulShutdownListener.class);
    private static final int AWAIT_SECONDS = 100;

    private volatile Connector connector;

    @Override
    public void customize(Connector connector) {
        this.connector = connector;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        connector.pause();
        Executor executor = connector.getProtocolHandler().getExecutor();
        if (executor == null) {
            return;
        }

        if (!(executor instanceof ThreadPoolExecutor)) {
            log.warn("{} is not an instance of ThreadPoolExecutor class, Can't ensure graceful shutdown..!!", executor.getClass());
            return;
        }

        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;
        log.info("Shutting down Tomcat thread pool executor. Active thread count : {}", threadPoolExecutor.getActiveCount());
        threadPoolExecutor.shutdown();
        try {
            if (threadPoolExecutor.awaitTermination(AWAIT_SECONDS, TimeUnit.SECONDS)) {
                log.info("Tomcat shutdown gracefully.!");
                return;
            }

            threadPoolExecutor.shutdownNow();
            if (!threadPoolExecutor.awaitTermination(AWAIT_SECONDS, TimeUnit.SECONDS)) {
                log.warn("Tomcat thread pool executor did not terminate. Active thread count : {}", threadPoolExecutor.getActiveCount());
            }

        } catch (InterruptedException ex) {
            log.error("Got interrupted while waiting for graceful shutdown", ex);
            Thread.currentThread().interrupt();
        }
    }
}
