# SCA - 통합 학습 관리 시스템
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Groovy](https://img.shields.io/badge/Apache_Groovy-4298B8?style=for-the-badge&logo=apachegroovy&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)

학생과 교사를 위한 게이미피케이션 기반 통합 학습 관리 플랫폼입니다.

## 프로젝트 개요

SCA(Smart Community Academy)는 학생의 학습 동기를 부여하고 교사의 학습 관리를 효율화하기 위해 개발된 웹 애플리케이션입니다. 게이미피케이션 요소(퀘스트, 가챠, 레이드)를 활용하여 학습 과정을 재미있고 체계적으로 관리할 수 있습니다.

## 주요 기능

### 학생 기능
- **퀘스트 시스템**: 교사가 제시한 과제를 수행하고 보상(코랄, 탐사 데이터) 획득
- **가챠 시스템**: 획득한 코랄로 물고기 가챠를 뽑아 컬렉션 수집
- **레이드 시스템**: 반 전체가 협력하여 보스 레이드에 참여하고 보상 획득
- **도감 시스템**: 수집한 물고기를 확인하고 관리
- **대시보드**: 개인 진도, 성취도, 보유 재화 확인

### 교사 기능
- **클래스 관리**: 학급 생성, 학생 초대 및 관리
- **퀘스트 관리**: 과제 생성, 난이도 설정, 제출물 승인/반려
- **레이드 관리**: 반 단위 레이드 생성 및 모니터링
- **학생 모니터링**: 학생별 진도, 성적, 활동 내역 조회
- **성적 분석**: 반체 및 개별 학생 성적 통계 및 분석

## 시스템 아키텍처
* **API 서버**: 도메인(Auth, Quest, Gacha, Raid) 주도 설계(DDD)를 적용한 계층형 구조
* **언어**: Groovy의 간결한 문법을 활용하여 생산성 향상
* **데이터베이스**: H2 (개발용) / MySQL (배포용) 연동

## 기술 스택
* **Framework**: Spring Boot 3.x
* **Language**: Apache Groovy
* **Build Tool**: Gradle
* **Database**: H2 Database (In-Memory), MySQL
* **Container**: Docker

## 폴더 구조
```bash
SCA-BE/
├── src/main/groovy/com/example/sca_be/
│   ├── domain/           # 도메인별 비즈니스 로직
│   │   ├── ai/           # ai 보상 추천 로직
│   │   ├── auth/         # 로그인 및 JWT 인증
│   │   ├── classroom/    # 학급 관리
│   │   ├── gacha/        # 확률형 아이템(가챠) 로직
│   │   ├── groupquest/   # 단체퀘스트 CRUD 및 상태 관리
│   │   ├── notification/ # 공지 기능
│   │   ├── personalquest/# 개인퀘스트 CRUD 및 상태 관리
│   │   └── raid/         # 협동 레이드 로직
│   └── global/           # 전역 예외 처리 및 설정
├── src/main/resources/
│   ├── application.yaml  # 스프링 설정 파일
│   └── schema-h2.sql     # DB 초기 스키마
├── Dockerfile            # 서버 컨테이너 이미지 설정
└── build.gradle          # Gradle 빌드 스크립트
```
