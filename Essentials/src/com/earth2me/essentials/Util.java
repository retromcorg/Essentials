package com.earth2me.essentials;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Util {
    private final static Logger logger = Logger.getLogger("Minecraft");
    private static final Locale defaultLocale = Locale.getDefault();
    private static final DecimalFormat df = new DecimalFormat("#0.00", DecimalFormatSymbols.getInstance(Locale.US));
    private static Locale currentLocale = defaultLocale;
    private static ResourceBundle bundle = ResourceBundle.getBundle("messages", defaultLocale);
    private static final ResourceBundle defaultBundle = ResourceBundle.getBundle("messages", Locale.US);

    private Util() {
    }

    public static String sanitizeFileName(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9]", "_");
    }

    public static String formatDateDiff(long date) {
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(date);
        Calendar now = new GregorianCalendar();
        return Util.formatDateDiff(now, c);
    }

    public static String formatDateDiff(Calendar fromDate, Calendar toDate) {
        boolean future = false;
        if (toDate.equals(fromDate)) {
            return Util.i18n("now");
        }
        if (toDate.after(fromDate)) {
            future = true;
        }

        StringBuilder sb = new StringBuilder();
        int[] types = new int[]
                {
                        Calendar.YEAR,
                        Calendar.MONTH,
                        Calendar.DAY_OF_MONTH,
                        Calendar.HOUR_OF_DAY,
                        Calendar.MINUTE,
                        Calendar.SECOND
                };
        String[] names = new String[]
                {
                        Util.i18n("year"),
                        Util.i18n("years"),
                        Util.i18n("month"),
                        Util.i18n("months"),
                        Util.i18n("day"),
                        Util.i18n("days"),
                        Util.i18n("hour"),
                        Util.i18n("hours"),
                        Util.i18n("minute"),
                        Util.i18n("minutes"),
                        Util.i18n("second"),
                        Util.i18n("seconds")
                };
        for (int i = 0; i < types.length; i++) {
            int diff = dateDiff(types[i], fromDate, toDate, future);
            if (diff > 0) {
                sb.append(" ").append(diff).append(" ").append(names[i * 2 + (diff > 1 ? 1 : 0)]);
            }
        }
        if (sb.length() == 0) {
            return "now";
        }
        return sb.toString();
    }

    private static int dateDiff(int type, Calendar fromDate, Calendar toDate, boolean future) {
        int diff = 0;
        long savedDate = fromDate.getTimeInMillis();
        while ((future && !fromDate.after(toDate)) || (!future && !fromDate.before(toDate))) {
            savedDate = fromDate.getTimeInMillis();
            fromDate.add(type, future ? 1 : -1);
            diff++;
        }
        diff--;
        fromDate.setTimeInMillis(savedDate);
        return diff;
    }

    public static long parseDateDiff(String time, boolean future) throws Exception {
        Pattern timePattern = Pattern.compile(
                "(?:([0-9]+)\\s*y[a-z]*[,\\s]*)?"
                        + "(?:([0-9]+)\\s*mo[a-z]*[,\\s]*)?"
                        + "(?:([0-9]+)\\s*w[a-z]*[,\\s]*)?"
                        + "(?:([0-9]+)\\s*d[a-z]*[,\\s]*)?"
                        + "(?:([0-9]+)\\s*h[a-z]*[,\\s]*)?"
                        + "(?:([0-9]+)\\s*m[a-z]*[,\\s]*)?"
                        + "(?:([0-9]+)\\s*(?:s[a-z]*)?)?", Pattern.CASE_INSENSITIVE);
        Matcher m = timePattern.matcher(time);
        int years = 0;
        int months = 0;
        int weeks = 0;
        int days = 0;
        int hours = 0;
        int minutes = 0;
        int seconds = 0;
        boolean found = false;
        while (m.find()) {
            if (m.group() == null || m.group().isEmpty()) {
                continue;
            }
            for (int i = 0; i < m.groupCount(); i++) {
                if (m.group(i) != null && !m.group(i).isEmpty()) {
                    found = true;
                    break;
                }
            }
            if (found) {
                if (m.group(1) != null && !m.group(1).isEmpty()) {
                    years = Integer.parseInt(m.group(1));
                }
                if (m.group(2) != null && !m.group(2).isEmpty()) {
                    months = Integer.parseInt(m.group(2));
                }
                if (m.group(3) != null && !m.group(3).isEmpty()) {
                    weeks = Integer.parseInt(m.group(3));
                }
                if (m.group(4) != null && !m.group(4).isEmpty()) {
                    days = Integer.parseInt(m.group(4));
                }
                if (m.group(5) != null && !m.group(5).isEmpty()) {
                    hours = Integer.parseInt(m.group(5));
                }
                if (m.group(6) != null && !m.group(6).isEmpty()) {
                    minutes = Integer.parseInt(m.group(6));
                }
                if (m.group(7) != null && !m.group(7).isEmpty()) {
                    seconds = Integer.parseInt(m.group(7));
                }
                break;
            }
        }
        if (!found) {
            throw new Exception(Util.i18n("illegalDate"));
        }
        Calendar c = new GregorianCalendar();
        if (years > 0) {
            c.add(Calendar.YEAR, years * (future ? 1 : -1));
        }
        if (months > 0) {
            c.add(Calendar.MONTH, months * (future ? 1 : -1));
        }
        if (weeks > 0) {
            c.add(Calendar.WEEK_OF_YEAR, weeks * (future ? 1 : -1));
        }
        if (days > 0) {
            c.add(Calendar.DAY_OF_MONTH, days * (future ? 1 : -1));
        }
        if (hours > 0) {
            c.add(Calendar.HOUR_OF_DAY, hours * (future ? 1 : -1));
        }
        if (minutes > 0) {
            c.add(Calendar.MINUTE, minutes * (future ? 1 : -1));
        }
        if (seconds > 0) {
            c.add(Calendar.SECOND, seconds * (future ? 1 : -1));
        }
        return c.getTimeInMillis();
    }

    public static Location getSafeDestination(final Location loc) throws Exception {
        if (loc == null || loc.getWorld() == null) {
            throw new Exception(Util.i18n("destinationNotSet"));
        }
        final World world = loc.getWorld();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        while (isBlockAboveAir(world, x, y, z)) {
            y -= 1;
            if (y < 0) {
                break;
            }
        }

        while (isBlockUnsafe(world, x, y, z)) {
            y += 1;
            if (y >= 127) {
                x += 1;
                break;
            }
        }
        while (isBlockUnsafe(world, x, y, z)) {
            y -= 1;
            if (y <= 1) {
                y = 127;
                x += 1;
                if (x - 32 > loc.getBlockX()) {
                    throw new Exception(Util.i18n("holeInFloor"));
                }
            }
        }
        return new Location(world, x + 0.5D, y, z + 0.5D, loc.getYaw(), loc.getPitch());
    }

    private static boolean isBlockAboveAir(final World world, final int x, final int y, final int z) {
        return world.getBlockAt(x, y - 1, z).getType() == Material.AIR;
    }

    public static boolean isBlockUnsafe(final World world, final int x, final int y, final int z) {
        final Block below = world.getBlockAt(x, y - 1, z);
        if (below.getType() == Material.LAVA || below.getType() == Material.STATIONARY_LAVA) {
            return true;
        }

        if (below.getType() == Material.FIRE) {
            return true;
        }

        if ((world.getBlockAt(x, y, z).getType() != Material.AIR)
                || (world.getBlockAt(x, y + 1, z).getType() != Material.AIR)) {
            return true;
        }
        return isBlockAboveAir(world, x, y, z);
    }

    public static String formatCurrency(final double value, final IEssentials ess) {
        String str = ess.getSettings().getCurrencySymbol() + df.format(value);
        if (str.endsWith(".00")) {
            str = str.substring(0, str.length() - 3);
        }
        return str;
    }

    public static double roundDouble(final double d) {
        return Math.round(d * 100.0) / 100.0;
    }

    public static Locale getCurrentLocale() {
        return currentLocale;
    }

    public static String i18n(String string) {
        try {
            return bundle.getString(string);
        } catch (MissingResourceException ex) {
            logger.log(Level.WARNING, String.format("Missing translation key \"%s\" in translation file %s", ex.getKey(), bundle.getLocale().toString()), ex);
            return defaultBundle.getString(string);
        }
    }

    public static String format(String string, Object... objects) {
        MessageFormat mf = new MessageFormat(i18n(string));
        return mf.format(objects);
    }

    public static void updateLocale(String loc, IEssentials ess) {
        if (loc == null || loc.isEmpty()) {
            return;
        }
        String[] parts = loc.split("[_\\.]");
        if (parts.length == 1) {
            currentLocale = new Locale(parts[0]);
        }
        if (parts.length == 2) {
            currentLocale = new Locale(parts[0], parts[1]);
        }
        if (parts.length == 3) {
            currentLocale = new Locale(parts[0], parts[1], parts[2]);
        }
        logger.log(Level.INFO, String.format("Using locale %s", currentLocale.toString()));
        bundle = ResourceBundle.getBundle("messages", currentLocale, new ConfigClassLoader(Util.class.getClassLoader(), ess));
        if (!bundle.keySet().containsAll(defaultBundle.keySet())) {
            logger.log(Level.WARNING, String.format("Translation file %s does not contain all translation keys.", currentLocale.toString()));
        }
    }

    public static String joinList(Object... list) {
        return joinList(", ", list);
    }

    public static String joinList(String seperator, Object... list) {
        StringBuilder buf = new StringBuilder();
        for (Object each : list) {
            if (buf.length() > 0) {
                buf.append(seperator);
            }

            if (each instanceof List) {
                buf.append(joinList(seperator, ((List) each).toArray()));
            } else {
                try {
                    buf.append(each.toString());
                } catch (Exception e) {
                    buf.append(each.toString());
                }
            }
        }
        return buf.toString();
    }

    public static String capitalCase(String s) {
        return s.toUpperCase().charAt(0) + s.toLowerCase().substring(1);
    }

    private static class ConfigClassLoader extends ClassLoader {
        private final transient File dataFolder;
        private final transient ClassLoader cl;
        private final transient IEssentials ess;

        public ConfigClassLoader(final ClassLoader cl, final IEssentials ess) {
            this.ess = ess;
            this.dataFolder = ess.getDataFolder();
            this.cl = cl;
        }

        @Override
        public URL getResource(final String string) {
            final File file = new File(dataFolder, string);
            if (file.exists()) {
                try {
                    return file.toURI().toURL();
                } catch (MalformedURLException ex) {
                    return cl.getResource(string);
                }
            }
            return cl.getResource(string);
        }

        @Override
        public synchronized void clearAssertionStatus() {
            cl.clearAssertionStatus();
        }

        @Override
        public InputStream getResourceAsStream(final String string) {
            final File file = new File(dataFolder, string);
            if (file.exists()) {
                BufferedReader br = null;
                try {
                    br = new BufferedReader(new FileReader(file));
                    final String version = br.readLine();

                    if (version == null || !version.equals("#version: " + ess.getDescription().getVersion())) {
                        logger.log(Level.WARNING, String.format("Translation file %s is not updated for Essentials version. Will use default.", file));
                        return cl.getResourceAsStream(string);
                    }
                    return new FileInputStream(file);
                } catch (IOException ex) {
                    return cl.getResourceAsStream(string);
                } finally {
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException ex) {
                        }
                    }
                }
            }
            return cl.getResourceAsStream(string);
        }

        @Override
        public Enumeration<URL> getResources(final String string) throws IOException {
            return cl.getResources(string);
        }

        @Override
        public Class<?> loadClass(final String string) throws ClassNotFoundException {
            return cl.loadClass(string);
        }

        @Override
        public synchronized void setClassAssertionStatus(final String string, final boolean bln) {
            cl.setClassAssertionStatus(string, bln);
        }

        @Override
        public synchronized void setDefaultAssertionStatus(final boolean bln) {
            cl.setDefaultAssertionStatus(bln);
        }

        @Override
        public synchronized void setPackageAssertionStatus(final String string, final boolean bln) {
            cl.setPackageAssertionStatus(string, bln);
        }
    }
}
