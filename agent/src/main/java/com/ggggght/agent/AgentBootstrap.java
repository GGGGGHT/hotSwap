package com.ggggght.agent;

// import com.intellij.ide.plugins.PluginManagerCore;
// import com.intellij.openapi.extensions.PluginId;
import java.io.File;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;

/**
 * 代理启动类
 */
public class AgentBootstrap {
    private static final String BOOTSTRAP = "com.ggggght.core.CoreBootstrap";
    private static final String MAINCLASS = "com.ggggght.retransform.Main";
    private static final String GET_INSTANCE = "getInstance";
    private static final String IS_BIND = "isBind";
    private static volatile ClassLoader classLoader;
    
    public static void premain(String args, Instrumentation inst) {
        try {
            main(args, inst);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static synchronized void main(String args, final Instrumentation inst) throws Throwable{
        String agentPath = "/Users/wangzheng/github/hotswap/core/build/idea-sandbox/plugins/hotswap/lib/core-0.0.1.jar";

        File file = new File(agentPath);
        if (!file.exists()) {
            System.out.println("core-0.0.1.jar not found");
            return;
        }
        final ClassLoader agentLoader = getClassLoader(file);
        Class<?> bootstrapClass = agentLoader.loadClass(BOOTSTRAP);
        Object bootstrap = bootstrapClass.getMethod(GET_INSTANCE, Instrumentation.class, String.class).invoke(null, inst, args);
        boolean isBind = (Boolean) bootstrapClass.getMethod(IS_BIND).invoke(bootstrap);
        Class<?> mainClass = agentLoader.loadClass(MAINCLASS);
        mainClass.getDeclaredField("classLoader").set(null, agentLoader);

        if (!isBind) {
            String errorMsg = "binding failed! .log for more details.";
            throw new RuntimeException(errorMsg);
        }
        System.out.println("[Hotswap] successfully bind .");
    }

    private static ClassLoader getClassLoader(File coreJarFile) throws Throwable {
        return loadOrDefineClassLoader(coreJarFile);
    }

    private static ClassLoader loadOrDefineClassLoader(File arthasCoreJarFile) throws Throwable {
        if (classLoader == null) {
            classLoader = new AgentClassloader(new URL[]{arthasCoreJarFile.toURI().toURL()});
        }

        return classLoader;
    }
    
}
