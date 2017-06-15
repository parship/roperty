package com.parship.roperty.jmx;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by dheid on 6/14/17.
 */
public class RopertyManagerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(RopertyManagerFactory.class);

    private static final RopertyManager ropertyManager = createRopertyManager();

    private static RopertyManager createRopertyManager() {
        RopertyManager ropertyManager = new RopertyManager();
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            mbs.registerMBean(ropertyManager, new ObjectName("com.parship.roperty", "type", RopertyManagerMBean.class.getSimpleName()));
        } catch (InstanceAlreadyExistsException e) {
            // nothing to do
        } catch (Exception e) {
            LOGGER.warn("Could not register MBean for Roperty", e);
        }
        return ropertyManager;
    }

    public static RopertyManager getRopertyManager() {
        return ropertyManager;
    }
}
