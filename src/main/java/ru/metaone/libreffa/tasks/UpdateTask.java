package ru.metaone.libreffa.tasks;

public class UpdateTask {

    public static boolean isOutdated = false;
    public static int latestVersion;
//
//    public static void run() {
//        try {
//            String remoteVersion = fetchRemoteVersion();
//            if (remoteVersion != null) {
//                latestVersion = Integer.parseInt(remoteVersion.replaceAll("[^0-9]", ""));
//                String pluginVersion = Main.getInstance().getDescription().getVersion();
//                if (!remoteVersion.equals(pluginVersion)) {
//                    Bukkit.getConsoleSender().sendMessage(formatColors(prefix + "&cThe plugin is not up to date, please update to the latest version, v" + remoteVersion));
//                    isOutdated = true;
//                } else {
//                    Bukkit.getConsoleSender().sendMessage(formatColors(prefix + "&aThe plugin is up to date."));
//                    isOutdated = false;
//                }
//            } else {
//                Bukkit.getConsoleSender().sendMessage(formatColors(prefix + "&cFailed to fetch remote version. Please check your internet connection."));
//            }
//        } catch (IOException | URISyntaxException ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    private static String fetchRemoteVersion() throws IOException, URISyntaxException {
//        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
//            HttpGet httpGet = new HttpGet();
//            httpGet.setURI(new URI("https://darkxx.xyz/minecraft/ffa/version.txt"));
//            HttpResponse httpResponse = httpClient.execute(httpGet, new BasicHttpContext());
//            BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
//            return reader.readLine();
//        }
//    }
}
