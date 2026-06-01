# 개요(Overview)

너는 한국 금융권의 특히 카드사 DevOps 전문가야. 금감원에서 제안하는 보안사항을 보수적으로 적용하여 서비스가 유지되고 구성되고 있어. 아래 열거되는 내용들을 참고해서 GitLab EE의 `gitlab-ci-yml` 파일을 작성 및 테스트를 위한 샘플코드를 만들어줘.

# 요구사항(Requirement)

- `gitlab-ci.yml` 을 테스트 할 수 있는 샘플코드를 구성
- `maven`, `gradle`, `ant` 빌더로 각각 프로젝트 구성
- `*.war`, `*.jar` artifact 를 만들어 낼 수 있도록
- `version bump` 를 하고, git tag를 붙일 수 있도록 구성
- `code coverage` 를 할 수 있도록 각 빌더별 구성 필요

# 구현상세(Implementation Detail)

- `spring boot`, `POJO` 등을 빌더(`maven`,`gradle`,`ant`)별 소스코드 필요.
- `mockito` 를 활용해서 실제 DB나 외부 인터페이싱을 하지 않도록 테스트 코드 구성
- [Code-ray XG](https://www.ssrinc.co.kr/solution/coderay) 를 runner를 이용해서 shell 을 통해서 code scan 을 요청 결과를 comment를 달 수 있도록 구성
  - Merge Request 기준으로 추가적인 commit 등이 이뤄지면 다시 code scan을 하고, 이전 결과를 삭제하고, 새로받은 결과물을 comment 할 수 있도록 구성
- DB와 (API 등을 통한)외부 인터페이싱이 되도록 샘플코드 구성 필요 
- `feature/*`, `release/*`, `hotfix/*` 브랜치를 만들 땐 `code coverage`, `sbom` 체크(`Dependency-Track`) GitLab CI 파이프 라인이 작동하지 않도록 구성 필요
- `feature/*`, `release/*`, `hotfix/*` 브랜치에 push 할 때 `code coverage`, `sbom` 체크 `GitLab CI` 파이프 라인 작동 되도록 구성 필요 
- `release/*` 브랜치에 MR 할 때 `coderay` code scan 을 동작하도록 구성 필요

# 제약사항(Constraint)

- DB, 외부 인터페이스를 연결 할 수 없음

# 테스트 통과(Acceptance Tests)

- `code coverage`는 라인 usage 기준으로 "80%" 이상이 되도록
- `coderay` 기준으로 `Critical`, `High` 가 "0"이 되도록
- `GitLab EE` 을 `self-host` 로 사용중이야. `premium license`를 적용중인데, 설정에 `Merge Requests`를 설정함에 있어서 `code coverage` 설정이 연동될 수 있도록