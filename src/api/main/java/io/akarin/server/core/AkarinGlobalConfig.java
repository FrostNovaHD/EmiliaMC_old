package io.akarin.server.core;

import com.google.common.base.Throwables;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

@SuppressWarnings({"UnusedIsStillUsed", "unused"})
public class AkarinGlobalConfig {
    private static File CONFIG_FILE;
    private static final String HEADER = "This is the global configuration file for Akarin.\n"
            + "Some options may impact gameplay, so use with caution,\n"
            + "and make sure you know what each option does before configuring.\n"
            + "\n"
            + "Akarin website: https://akarin.io/ \n";
    /*========================================================================*/
    public static YamlConfiguration config;
    static int version;
    /*========================================================================*/
    public static void init(File configFile) {
        CONFIG_FILE = configFile;
        config = new YamlConfiguration();
        try { 
            config.load(CONFIG_FILE);
        } catch (IOException ex) {
        } catch (InvalidConfigurationException ex) {
            Bukkit.getLogger().severe("Could not load akarin.yml, please correct your syntax errors");
            ex.printStackTrace();
            throw Throwables.propagate(ex);
        }
        config.options().header(HEADER);
        config.options().copyDefaults(true);

        version = getInt("config-version", 2);
        set("config-version", 2);
        readConfig(AkarinGlobalConfig.class, null);
    }

    static void readConfig(Class<?> clazz, Object instance) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (Modifier.isPrivate(method.getModifiers())) {
                if (method.getParameterTypes().length == 0 && method.getReturnType() == Void.TYPE) {
                    try {
                        method.setAccessible(true);
                        method.invoke(instance);
                    } catch (InvocationTargetException ex) {
                        throw Throwables.propagate(ex.getCause());
                    } catch (Exception ex) {
                        Bukkit.getLogger().severe("Error invoking " + method);
                        ex.printStackTrace();
                    }
                }
            }
        }

        try {
            config.save(CONFIG_FILE);
        } catch (IOException ex) {
            Bukkit.getLogger().severe("Could not save " + CONFIG_FILE);
            ex.printStackTrace();
        }
    }

    private static final Pattern SPACE = Pattern.compile(" ");
    private static final Pattern NOT_NUMERIC = Pattern.compile("[^-\\d.]");
    public static int getSeconds(String str) {
        str = SPACE.matcher(str).replaceAll("");
        final char unit = str.charAt(str.length() - 1);
        str = NOT_NUMERIC.matcher(str).replaceAll("");
        double num;
        try {
            num = Double.parseDouble(str);
        } catch (Exception e) {
            num = 0D;
        }
        switch (unit) {
            case 'd': num *= (double) 60*60*24; break;
            case 'h': num *= (double) 60*60; break;
            case 'm': num *= 60; break;
            default: case 's': break;
        }
        return (int) num;
    }

    protected static String timeSummary(int seconds) {
        String time = "";

        if (seconds > 60 * 60 * 24) {
            time += TimeUnit.SECONDS.toDays(seconds) + "d";
            seconds %= 60 * 60 * 24;
        }

        if (seconds > 60 * 60) {
            time += TimeUnit.SECONDS.toHours(seconds) + "h";
            seconds %= 60 * 60;
        }

        if (seconds > 0) {
            time += TimeUnit.SECONDS.toMinutes(seconds) + "m";
        }
        return time;
    }

    public static void set(String path, Object val) {
        config.set(path, val);
    }

    private static boolean getBoolean(String path, boolean def) {
        config.addDefault(path, def);
        return config.getBoolean(path, config.getBoolean(path));
    }

    private static double getDouble(String path, double def) {
        config.addDefault(path, def);
        return config.getDouble(path, config.getDouble(path));
    }

    private static float getFloat(String path, float def) {
        // TODO: Figure out why getFloat() always returns the default value.
        return (float) getDouble(path, def);
    }

    private static int getInt(String path, int def) {
        config.addDefault(path, def);
        return config.getInt(path, config.getInt(path));
    }

    private static <T> List getList(String path, T def) {
        config.addDefault(path, def);
        return config.getList(path, config.getList(path));
    }

    private static String getString(String path, String def) {
        config.addDefault(path, def);
        return config.getString(path, config.getString(path));
    }
    /*========================================================================*/
    public static boolean noResponseDoGC = true;
    private static void noResponseDoGC() {
        noResponseDoGC = getBoolean("alternative.gc-before-stuck-restart", noResponseDoGC);
    }

    public static String serverBrandName = "";
    private static void serverBrandName() {
        serverBrandName = getString("alternative.modified-server-brand-name", serverBrandName);
    }

    public static int fileIOThreads = 2;
    private static void fileIOThreads() {
        fileIOThreads = getInt("core.chunk-save-threads", fileIOThreads);
        fileIOThreads = fileIOThreads < 1 ? 1 : fileIOThreads;
        fileIOThreads = fileIOThreads > 8 ? 8 : fileIOThreads;
    }
    
    public static boolean fixPhysicsEventBehaviour = false;
    private static void fixPhysicsEventBehavior() {
        fixPhysicsEventBehaviour = getBoolean("alternative.fix-physics-event-behaviour", fixPhysicsEventBehaviour);
    }
    
    public static boolean lazyThreadAssertion = true;
    private static void lazyThreadAssertion() {
        lazyThreadAssertion = getBoolean("core.lazy-thread-assertion", lazyThreadAssertion);
    }
    
    public static int userCacheExpireDays = 30;
    private static void userCacheExpireDays() {
        userCacheExpireDays = getSeconds(getString("core.user-cache-expire-time", "30d"));
    }
    
    public static double blockbreakAnimationVisibleDistance = 1024;
    private static void blockbreakAnimationVisibleDistance() {
        blockbreakAnimationVisibleDistance = Math.sqrt(getDouble("alternative.block-break-animation-visible-distance", 32.00));
    }
    
    public static boolean enableAsyncLighting = true;
    private static void enableAsyncLighting() {
        enableAsyncLighting = getBoolean("core.async-lighting.enable", enableAsyncLighting);
    }
}