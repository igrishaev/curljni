package org.curljni;

public class OS {

    public enum OSFamily {
        WINDOWS, MAC, LINUX
    }

    private static RuntimeException error(String template, Object... args) {
        return new RuntimeException(String.format(template, args));
    }

    public static OSFamily getOSFamily() {
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("win")) {
            return OSFamily.WINDOWS;
        } else if (osName.contains("mac")) {
            return OSFamily.MAC;
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            return OSFamily.LINUX;
        } else {
            throw error("unknown OS: %s", osName);
        }
    }

    public static String getLibName() {
        OSFamily osFamily = getOSFamily();
        String osArch = System.getProperty("os.arch");
        return "curl_"
                + (osFamily.toString() + "_" + osArch)
                    .replace("-", "_")
                    .replace(" ", "_")
                    .toLowerCase()
                + ".lib";
    }

    public static void main(String... args) {
        System.out.print(getLibName());
    }
}
