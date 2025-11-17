#!/bin/bash

# Pacman Map Editor 실행 스크립트

echo "Pacman Map Editor를 시작합니다..."

# 컴파일 (필요한 경우)
if [ ! -d "build" ]; then
    echo "컴파일 중..."
    mkdir -p build
    javac -cp src/java -d build src/java/mapeditor/*.java src/java/mapeditor/**/*.java

    if [ $? -ne 0 ]; then
        echo "컴파일 실패!"
        exit 1
    fi
fi

# 실행
echo "Map Editor 실행 중..."
java -cp build mapeditor.MapEditorLauncher

echo "Map Editor가 종료되었습니다."