import os
import re
from datetime import datetime

# ==============================================================================
# 설정 및 템플릿
# ==============================================================================

# 분석에서 제외할 디렉토리 및 파일
IGNORE_DIRS = {'.git', '.idea', '.gradle', 'build', 'gradle', 'out', '.github', 'wrapper'}
IGNORE_FILES = {'.gitignore', 'generate_readme.py', 'README.md', 'gradlew', 'gradlew.bat'}

README_TEMPLATE = """# Silver Bridge Backend
> 지역별 노인 맞춤형 사투리 음성인식 서비스를 제공하는 AI 기반 복지 플랫폼 **Silver Bridge**의 백엔드 서버입니다.

---

# 📚 Table of Contents
{table_of_contents}

---

# 🚀 프로젝트 개요
Silver Bridge Backend는 고령층의 디지털 소외를 해소하기 위해  
**사투리 기반 음성인식(STT), 감정 분석, TTS, 일정관리, 보호자 연동 기능** 등을 제공하는 Spring Boot 기반 서버입니다.

---

# ⚙️ 기술 스택
- **Java 17**
- **Spring Boot 3.x**
- **Gradle**
- **Spring Security + JWT**
- **JPA/Hibernate**
- **MariaDB**
- **AWS (EC2, Nginx)**
- **FastAPI (ASR/STT 서버)**

---

# 📁 1. 프로젝트 구조
<pre>
{project_structure}
</pre>

---

# 🧭 2. Controller & API Summary
{controller_summary}

---

# 🧩 3. Service Layer Summary
{service_summary}

---

# 🛠 4. Build & Run
```bash
./gradlew clean build
java -jar build/libs/silverbridge-backend.jar
# Test
./gradlew test
```

> **Last Updated:** {generated_at}
"""

# ==============================================================================
# 1. 디렉토리 트리 생성 함수
# ==============================================================================

def get_project_structure(path, prefix=""):
    """
    프로젝트의 디렉토리 구조를 문자열 트리 형태로 반환합니다.
    """
    if not os.path.exists(path):
        return ""

    tree = ""
    try:
        # 정렬하여 출력 (디렉토리 우선, 그 다음 파일)
        items = sorted(os.listdir(path))
        
        # 필터링
        items = [i for i in items if i not in IGNORE_DIRS and i not in IGNORE_FILES]
        
        count = len(items)
        for index, item in enumerate(items):
            full_path = os.path.join(path, item)
            is_last = (index == count - 1)
            connector = "└── " if is_last else "├── "
            
            if os.path.isdir(full_path):
                tree += f"{prefix}{connector}📁 {item}\n"
                extension = "    " if is_last else "│   "
                tree += get_project_structure(full_path, prefix + extension)
            else:
                tree += f"{prefix}{connector}📄 {item}\n"
    except PermissionError:
        pass
        
    return tree

# ==============================================================================
# 2. Controller 분석 함수
# ==============================================================================

def summarize_controller(src_path):
    """
    Controller 파일을 찾아 API 매핑 정보를 요약합니다.
    """
    summary = ""
    # 매핑 어노테이션 탐지 정규식 (파라미터 포함)
    mapping_regex = re.compile(r'@(GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping|RequestMapping)\s*(\((?:[^{]*?)\))?')
    
    found_controllers = False

    for root, dirs, files in os.walk(src_path):
        for file in files:
            if file.endswith("Controller.java"):
                full_path = os.path.join(root, file)
                class_name = file.replace(".java", "")
                
                try:
                    with open(full_path, "r", encoding="utf-8") as f:
                        content = f.read()
                        
                    # 매핑 찾기
                    matches = mapping_regex.findall(content)
                    
                    if matches:
                        found_controllers = True
                        summary += f"### 📌 {class_name}\n"
                        for method, params in matches:
                            # 파라미터 정제 (줄바꿈 제거 및 공백 정리)
                            clean_params = params.replace('"', '').replace('\n', '').strip() if params else ""
                            summary += f"- **{method}** {clean_params}\n"
                        summary += "\n"
                except Exception as e:
                    print(f"Error reading {file}: {e}")

    if not found_controllers:
        summary = "No Controllers found or parsed."
        
    return summary

# ==============================================================================
# 3. Service 분석 함수
# ==============================================================================

def summarize_service(src_path):
    """
    Service 파일을 찾아 주요 메서드 정보를 요약합니다.
    """
    summary = ""
    # public 메서드 탐지 정규식 (반환타입 메서드명)
    method_regex = re.compile(r'public\s+[\w<>?\[\]]+\s+(\w+)\s*\(')
    
    found_services = False

    for root, dirs, files in os.walk(src_path):
        for file in files:
            # ServiceImpl 또는 Service 인터페이스/클래스 탐색
            if file.endswith("Service.java") or file.endswith("ServiceImpl.java"):
                # 인터페이스는 제외하고 구현체나 클래스만 보고 싶다면 조건을 수정 가능
                full_path = os.path.join(root, file)
                class_name = file.replace(".java", "")
                
                try:
                    with open(full_path, "r", encoding="utf-8") as f:
                        content = f.read()

                    # @Service 어노테이션이 있거나 이름이 ServiceImpl인 경우만
                    if "@Service" in content or "ServiceImpl" in file:
                        matches = method_regex.findall(content)
                        # 생성자나 기본적인 메서드 제외 필터링 가능
                        matches = [m for m in matches if m not in ['toString', 'hashCode', 'equals']]

                        if matches:
                            found_services = True
                            summary += f"### 🧩 {class_name}\n"
                            # 너무 많으면 5개까지만 표시 등 조절 가능, 여기선 전부 나열
                            for method_name in matches:
                                summary += f"- `{method_name}()`\n"
                            summary += "\n"
                except Exception as e:
                    print(f"Error reading {file}: {e}")
                    
    if not found_services:
        summary = "No Services found."

    return summary

# ==============================================================================
# 4. 목차 생성 및 메인 실행
# ==============================================================================

def build_table_of_contents():
    return """
1. [프로젝트 구조](#-1-프로젝트-구조)
2. [Controller & API Summary](#-2-controller--api-summary)
3. [Service Layer Summary](#-3-service-layer-summary)
4. [Build & Run](#-4-build--run)
"""

def main():
    root = "."
    # Spring Boot 표준 소스 경로
    src_path = os.path.join(root, "src", "main", "java")
    
    print("⏳ 분석 시작...")
    
    # 각 섹션 데이터 생성
    structure = get_project_structure(root)
    controller_summary = summarize_controller(src_path)
    service_summary = summarize_service(src_path)
    toc = build_table_of_contents()
    
    # 템플릿 포맷팅
    readme_content = README_TEMPLATE.format(
        table_of_contents=toc,
        project_structure=structure,
        controller_summary=controller_summary,
        service_summary=service_summary,
        generated_at=datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    )
    
    # 파일 쓰기
    with open("README.md", "w", encoding="utf-8") as f:
        f.write(readme_content)
    
    print("✅ README.md 자동 생성 완료!")

if __name__ == "__main__":
    main()
