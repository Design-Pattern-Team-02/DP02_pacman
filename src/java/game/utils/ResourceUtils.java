package game.utils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.imageio.ImageIO;

public class ResourceUtils {

    /**
     * resources/level 폴더 내의 파일들(.csv 등) 이름(확장자 제외)을 반환
     */
    public static List<String> listLevelNames() {
        List<String> names = new ArrayList<>();
        try {
            URL dirURL = ResourceUtils.class.getClassLoader().getResource("level");
            if (dirURL == null) return names;

            if (dirURL.getProtocol().equals("file")) {
                Path dir = Paths.get(dirURL.toURI());
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                    for (Path entry : stream) {
                        if (Files.isRegularFile(entry)) {
                            String filename = entry.getFileName().toString();
                            int dot = filename.lastIndexOf('.');
                            if (dot > 0) filename = filename.substring(0, dot);
                            names.add(filename);
                        }
                    }
                }
            } else if (dirURL.getProtocol().equals("jar")) {
                String path = dirURL.getPath();
                String jarPath = path.substring(5, path.indexOf("!")); // remove "file:" and after "!"
                try (JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"))) {
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();
                        if (name.startsWith("level/") && !entry.isDirectory()) {
                            String filename = name.substring("level/".length());
                            int dot = filename.lastIndexOf('.');
                            if (dot > 0) filename = filename.substring(0, dot);
                            names.add(filename);
                        }
                    }
                }
            }
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }

        Collections.sort(names);
        return names;
    }

    /**
     * resources/img/map/{mapName}.[png|jpg|jpeg] 로부터 이미지를 로드
     * null이 반환될 수 있으니 호출 쪽에서 체크
     */
    public static BufferedImage loadMapImage(String mapName) {
        String[] exts = {".png", ".jpg", ".jpeg"};
        for (String ext : exts) {
            String path = "img/" + mapName + "_bg" + ext;
            try (InputStream is = ResourceUtils.class.getClassLoader().getResourceAsStream(path)) {
                if (is != null) {
                    return ImageIO.read(is);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
