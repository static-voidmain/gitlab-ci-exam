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
- Nexus 배포 저장소 업로드

## CodeRay XG MR 코멘트 설정

- `CODERAY_CLI_COMMAND`: 실제 운영 환경에서 검증된 CodeRay XG CLI 호출문을 GitLab CI/CD protected variable로 설정합니다.
- `GITLAB_PRIVATE_TOKEN` 또는 `GITLAB_TOKEN` 또는 `GITLAB_API_TOKEN`: MR note 생성/삭제 권한이 있는 토큰을 masked/protected variable로 설정합니다.
- `CODERAY_TIMEOUT_SECONDS`: CLI 요청 후 결과 txt 파일을 기다릴 최대 시간입니다. 기본값은 `1800`초입니다.
- `CODERAY_POLL_INTERVAL_SECONDS`: 결과 파일 확인 주기입니다. 기본값은 `15`초입니다.
- 스크립트는 CLI 실행 전에 `CODERAY_GIT_REPOSITORY_URL`, `CODERAY_BRANCH`, `CODERAY_COMMIT_SHA`, `CODERAY_RESULT_DIR`, `CODERAY_RESULT_FILE` 환경 변수를 export합니다.
- 새 결과 txt 파일이 안정적으로 저장된 뒤에만 이전 CodeRay XG MR 댓글을 삭제하고 새 댓글을 등록합니다. 더 최신 MR 커밋이 이미 존재하면 오래된 파이프라인은 댓글을 갱신하지 않습니다.

## Nexus Artifact 업로드 설정

- `NEXUS_UPLOAD_ENABLED`: Nexus 업로드 job을 활성화하려면 `true`로 설정합니다. 기본값은 `false`입니다.
- `NEXUS_URL`: Nexus base URL입니다. 예: `https://nexus.example.com`
- `NEXUS_REPOSITORY`: 업로드 대상 hosted repository 이름입니다. 예: `maven-releases`
- `NEXUS_USERNAME`, `NEXUS_PASSWORD`: 업로드 권한이 있는 계정을 GitLab CI/CD masked/protected variable로 설정합니다.
- `NEXUS_REPOSITORY_FORMAT`: 기본값은 `maven2`이며, raw repository를 사용할 때는 `raw`로 설정합니다.
- `NEXUS_PROJECT_DIR`: 업로드 대상 앱 디렉터리입니다. 예: `maven-app`, `gradle-app`, `ant-app`
- `NEXUS_ARTIFACT_ID`, `NEXUS_PROJECT_VERSION`, `NEXUS_PACKAGING`: 미설정 시 Maven `pom.xml`, Gradle `build.gradle`/`settings.gradle`, Ant `build.xml`에서 자동으로 읽습니다.
- `NEXUS_ARTIFACT_FILE`: 미설정 시 `${outputDir}/${NEXUS_ARTIFACT_ID}-${NEXUS_PROJECT_VERSION}.${NEXUS_PACKAGING}` 형식으로 자동 계산합니다. Maven은 `target`, Gradle은 `build/libs`, Ant는 `build`를 사용합니다.
- `NEXUS_COMPONENT_VERSION`: 명시하면 해당 버전으로 업로드합니다. 미설정 시 tag pipeline은 tag명, branch pipeline은 빌드 파일의 project version에 `${CI_COMMIT_SHORT_SHA}`를 붙인 형식으로 업로드합니다.
- Nexus 업로드 job은 `main`/`release/*` push에서 manual로 노출되며, feature/MR pipeline에서는 배포 저장소 오염을 막기 위해 실행되지 않습니다.
