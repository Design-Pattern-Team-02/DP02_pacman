package mapeditor.utils;

import mapeditor.model.EntityType;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * CsvMapWriter - CSV 파일 저장 유틸리티
 * 맵 데이터를 CSV 형식으로 저장
 *
 * 기존 CsvReader와 호환되는 형식으로 저장
 * 구분자: 세미콜론 (;)
 */
public class CsvMapWriter {

    private static final String LEVEL_FOLDER = "src/resources/level/";
    private static final String FILE_PREFIX = "custom_map_";
    private static final String FILE_EXTENSION = ".csv";

    /**
     * 맵 데이터를 CSV 파일로 저장
     * @param mapData 저장할 맵 데이터 (2D 배열)
     * @param filePath 저장 경로 (null이면 자동 생성)
     * @return 저장된 파일 경로
     * @throws IOException 파일 저장 실패
     */
    public static String saveMap(EntityType[][] mapData, String filePath) throws IOException {
        if (filePath == null) {
            filePath = generateFilePath();
        }

        File file = new File(filePath);
        File parentDir = file.getParentFile();

        // 디렉토리가 없으면 생성
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writeMapData(writer, mapData);
        }

        return filePath;
    }

    /**
     * 자동으로 파일 경로 생성
     * @return 생성된 파일 경로
     */
    private static String generateFilePath() {
        File levelFolder = new File(LEVEL_FOLDER);
        if (!levelFolder.exists()) {
            levelFolder.mkdirs();
        }

        // 사용되지 않은 번호 찾기
        int fileNumber = 1;
        File file;
        do {
            String fileName = String.format("%s%03d%s", FILE_PREFIX, fileNumber, FILE_EXTENSION);
            file = new File(levelFolder, fileName);
            fileNumber++;
        } while (file.exists() && fileNumber < 1000);

        return file.getAbsolutePath();
    }

    /**
     * 맵 데이터를 BufferedWriter에 작성
     */
    private static void writeMapData(BufferedWriter writer, EntityType[][] mapData) throws IOException {
        int height = mapData.length;
        int width = height > 0 ? mapData[0].length : 0;

        for (int y = 0; y < height; y++) {
            StringBuilder line = new StringBuilder();

            for (int x = 0; x < width; x++) {
                EntityType entity = mapData[y][x];
                char symbol = entity != null ? entity.getSymbol() : ' ';

                // 심볼 추가
                line.append(symbol);

                // 마지막 열이 아니면 구분자 추가
                if (x < width - 1) {
                    line.append(';');
                }
            }

            // 라인 작성
            writer.write(line.toString());

            // 마지막 행이 아니면 줄바꿈 추가
            if (y < height - 1) {
                writer.newLine();
            }
        }
    }

    /**
     * 맵 데이터 검증 (저장 전 확인용)
     * @param mapData 검증할 맵 데이터
     * @return 검증 통과 여부
     */
    public static boolean validateBeforeSave(EntityType[][] mapData) {
        if (mapData == null || mapData.length == 0) {
            return false;
        }

        // 필수 엔티티 개수 확인
        int pacmanCount = 0;
        int blinkyCount = 0;
        int pinkyCount = 0;
        int inkyCount = 0;
        int clydeCount = 0;

        for (EntityType[] row : mapData) {
            for (EntityType entity : row) {
                if (entity == null) continue;

                switch (entity) {
                    case PACMAN:
                        pacmanCount++;
                        break;
                    case BLINKY:
                        blinkyCount++;
                        break;
                    case PINKY:
                        pinkyCount++;
                        break;
                    case INKY:
                        inkyCount++;
                        break;
                    case CLYDE:
                        clydeCount++;
                        break;
                    default:
                        break;
                }
            }
        }

        // 각 필수 엔티티가 정확히 1개씩 있는지 확인
        return pacmanCount == 1 && blinkyCount == 1 && pinkyCount == 1 &&
               inkyCount == 1 && clydeCount == 1;
    }

    /**
     * 파일 이름에서 맵 번호 추출
     * @param fileName 파일 이름
     * @return 맵 번호 (추출 실패 시 -1)
     */
    public static int extractMapNumber(String fileName) {
        if (fileName == null || !fileName.startsWith(FILE_PREFIX)) {
            return -1;
        }

        String numberPart = fileName.replace(FILE_PREFIX, "").replace(FILE_EXTENSION, "");
        try {
            return Integer.parseInt(numberPart);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}