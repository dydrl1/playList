# 🎵 Playlist Manager Project
> 유튜브 API를 연동한 개인 맞춤형 음악 플레이리스트 관리 서비스

## 📑 프로젝트 개요
- **설명**: 유튜브의 방대한 영상 데이터를 검색하고, 나만의 플레이리스트를 구성하여 관리할 수 있는 백엔드 중심 프로젝트입니다.
- **개발 기간**: 202X.XX ~ 202X.XX (N주)
- **주요 역할**: 백엔드 API 설계 및 구현, 인프라 구축

## 🛠 Tech Stack
- **Language & Framework**: Java 21, Spring Boot 3.x
- **Database**: MySQL (RDB), Redis (Cache)
- **Security**: Spring Security, JWT (JSON Web Token)
- **Infrastructure**: Cloudtype (Deployment)
- **External API**: YouTube Data API v3

## ✨ Key Features
1. **YouTube API 연동**: 검색어 기반 영상 정보 수집 및 실시간 재생 데이터 제공.
2. **플레이리스트 관리**: 검색된 트랙을 사용자별 데이터베이스에 저장 및 커스텀 리스트화.
3. **JWT 인증 시스템**: Stateless 환경에서의 보안 로그인/로그아웃 구현.
4. **조회수 및 좋아요 기능**: 
   - **Redis를 활용한 조회수 최적화**: 빈번한 DB 쓰기 작업을 방지하기 위해 Redis에서 조회수를 캐싱 처리.
   - 사용자 기반 좋아요 기능 구현.

## 🚀 Technical Challenges (백엔드 핵심 역량)
- **Redis 활용**: 단순 DB 저장 대신 Redis를 선택한 이유(성능 최적화, 동시성 고려 등) 기술.
- **JWT 보안**: 만료 기간 설정 및 인증 필터 구현 시 고려했던 보안 사항.
- **API 에러 핸들링**: 유튜브 저작권 정책(재생 제한)에 따른 예외 처리 및 사용자 안내 로직.

## 🖥 Frontend
- 본 프로젝트의 프론트엔드는 **바이브코딩**을 활용하여 구현되었으며, 본인은 **백엔드 API 개발 및 서버 아키텍처**를 전담하였습니다.
