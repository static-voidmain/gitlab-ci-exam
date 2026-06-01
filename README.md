# GitLab CI Sample Repository

이 리포지토리는 GitLab EE Premium 환경에서 동작하는 빌드, 테스트, 코드 커버리지, SBOM, CodeRay XG 코드 스캔, 버전 태깅 샘플 구성을 제공합니다.

## 포함 내용

- `maven-app`: Spring Boot WAR 형태 프로젝트
- `gradle-app`: Spring Boot WAR 형태 프로젝트
- `ant-app`: Ant 기반 Java 애플리케이션과 테스트
- `gitlab-ci.yml`: GitLab CI 파이프라인 정의
- `scripts/`: 버전 bump, CodeRay 스캔, 커버리지/ SBOM 샘플 스크립트

## 요구 사항

- Maven, Gradle, Ant 빌드
- `*.war`, `*.jar` 생성
- `code coverage` 80% 이상
- `code scan` 결과 MR 코멘트
- `version bump` 및 Git 태그 생성
