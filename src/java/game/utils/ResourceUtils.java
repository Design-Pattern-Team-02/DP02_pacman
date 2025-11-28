package game.utils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;

public class ResourceUtils {

    /**
     * resources/level 폴더 내의 파일들(.csv 등) 이름(확장자 제외)을 반환
     */
    public List<String> listLevelNames() {
        List<String> names = new ArrayList<>();
        try {
            // IDE 실행 시 프로젝트 루트 기준 상대 경로 사용
            Path dir = Paths.get("src/resources/level");
            if (!Files.exists(dir)) {
                // JAR 실행 시 클래스패스 리소스 폴백
                System.out.println(1);
                URL dirURL = ResourceUtils.class.getClassLoader().getResource("level");
                if (dirURL != null && dirURL.getProtocol().equals("file")) {
                    dir = Paths.get(dirURL.toURI());
                } else {
                    return names;
                }
            }

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
        } catch (Exception e) {
            e.printStackTrace();
        }

        Collections.sort(names);
        return names;
    }

    public BufferedImage loadMapImage(String mapName) {
        String[] exts = {".png", ".jpg", ".jpeg"};
        for (String ext : exts) {
            try {
                // 외부 파일 먼저 확인
                Path imgPath = Paths.get("src/resources/img/" + mapName + "_bg" + ext);
                if (Files.exists(imgPath)) {
                    return ImageIO.read(imgPath.toFile());
                }

                // 클래스패스 리소스 폴백
                String path = "img/" + mapName + "_bg" + ext;
                InputStream is = ResourceUtils.class.getClassLoader().getResourceAsStream(path);
                if (is != null) {
                    try (is) {
                        return ImageIO.read(is);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
