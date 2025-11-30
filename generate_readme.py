import os
import re
from datetime import datetime

# ==============================================================================
# 설정 및 템플릿
# ==============================================================================

# 분석 및 트리 출력에서 제외할 디렉토리
IGNORE_DIRS = {
    '.git', '.idea', '.gradle', 'build', 'gradle', 'out', 
    '.github', 'wrapper', 'target', 'node_modules', '__pycache__'
}
IGNORE_FILES = {
    '.gitignore', 'generate_readme.py', 'README.md', 
    'gradlew', 'gradlew.bat', '.DS_Store'
}

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
- **AWS (EC2, S3, Route53, Nginx)**
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
cd backend
./gradlew clean build
java -jar build/libs/silverbridge-backend.jar
# Test
./gradlew test
```

> **Last Updated:** {generated_at}
"""

# ==============================================================================
# 유틸리티: 소스 루트 찾기 (여기가 핵심 수정됨)
# ==============================================================================

def find_java_source_root(start_path="."):
    """
    Java 소스 경로를 찾습니다. backend 폴더가 있는 경우를 우선 처리합니다.
    """
    print(f"🔍 [DEBUG] 자바 소스 루트 찾는 중... (시작: {os.path.abspath(start_path)})")
    
    # 1. 사용자 프로젝트 구조에 맞춘 최우선 경로 확인 (backend/src/main/java)
    backend_path = os.path.join(start_path, "backend", "src", "main", "java")
    if os.path.exists(backend_path):
        print(f"✅ [DEBUG] 'backend' 폴더 내부 경로 발견: {backend_path}")
        return backend_path

    # 2. 표준 경로 확인 (src/main/java)
    standard_path = os.path.join(start_path, "src", "main", "java")
    if os.path.exists(standard_path):
        print(f"✅ [DEBUG] 표준 루트 경로 발견: {standard_path}")
        return standard_path
    
    # 3. 재귀 탐색 (혹시 다른 이름의 폴더에 있을 경우)
    print("⚠️ [DEBUG] 주요 경로에 없음. 재귀 탐색 시작...")
    for root, dirs, _ in os.walk(start_path):
        dirs[:] = [d for d in dirs if d not in IGNORE_DIRS]
        
        if "src" in dirs:
            potential_path = os.path.join(root, "src", "main", "java")
            if os.path.exists(potential_path):
                print(f"✅ [DEBUG] 깊은 경로에서 발견: {potential_path}")
                return potential_path

    print("❌ [DEBUG] 자바 소스 경로를 찾지 못했습니다. 분석이 제대로 되지 않을 수 있습니다.")
    return start_path

# ==============================================================================
# 1. 디렉토리 트리 생성 함수
# ==============================================================================

def get_project_structure(path, prefix=""):
    if not os.path.exists(path):
        return ""

    tree = ""
    try:
        # 정렬: 디렉토리 우선 표시 옵션은 뺌 (알파벳 순이 깔끔할 수 있음)
        # 하지만 보기 좋게 하기 위해 폴더와 파일을 섞어서 정렬하되 로직 유지
        items = sorted(os.listdir(path))
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

def summarize_controller(search_path):
    print(f"🔍 [DEBUG] Controller 분석 시작 (탐색 경로: {search_path})")
    summary = ""
    # @Annotation( ... ) 형태까지 잡기 위해 re.DOTALL 사용
    mapping_regex = re.compile(r'@(GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping|RequestMapping)\s*(\((?:[^)]*?)\))?', re.DOTALL)
    
    found_count = 0
    file_count = 0

    for root, dirs, files in os.walk(search_path):
        for file in files:
            if file.endswith("Controller.java"):
                file_count += 1
                full_path = os.path.join(root, file)
                
                try:
                    with open(full_path, "r", encoding="utf-8") as f:
                        content = f.read()
                        
                    matches = mapping_regex.findall(content)
                    class_name = file.replace(".java", "")
                    
                    if matches:
                        found_count += 1
                        summary += f"### 📌 {class_name}\n"
                        for method, params in matches:
                            # 파라미터 내 줄바꿈/공백 정리
                            clean_params = params.replace('\n', ' ').replace('"', '').strip()
                            clean_params = re.sub(r'\s+', ' ', clean_params)
                            summary += f"- **{method}** {clean_params}\n"
                        summary += "\n"
                    else:
                        summary += f"### 📌 {class_name}\n- (No API mappings detected)\n\n"
                        
                except Exception as e:
                    print(f"❌ Error reading {file}: {e}")

    if file_count == 0:
        return "No Controller files found. (Check if path is correct)"
    if found_count == 0:
        return "Controllers found but no mappings extracted."
        
    return summary

# ==============================================================================
# 3. Service 분석 함수
# ==============================================================================

def summarize_service(search_path):
    print(f"🔍 [DEBUG] Service 분석 시작 (탐색 경로: {search_path})")
    summary = ""
    # public 반환타입 메서드명(인자) 패턴
    method_regex = re.compile(r'public\s+(?:[\w<>?\[\]]+\s+)+(\w+)\s*\(', re.DOTALL)
    
    found_count = 0

    for root, dirs, files in os.walk(search_path):
        for file in files:
            # ServiceImpl.java 또는 Service.java
            if (file.endswith("Service.java") or file.endswith("ServiceImpl.java")):
                full_path = os.path.join(root, file)
                
                try:
                    with open(full_path, "r", encoding="utf-8") as f:
                        content = f.read()

                    # 인터페이스나 클래스인지 확인
                    if "interface " in content or "class " in content:
                        class_name = file.replace(".java", "")
                        
                        matches = method_regex.findall(content)
                        exclude_methods = {'toString', 'hashCode', 'equals', 'wait', 'notify', 'notifyAll', 'getClass'}
                        matches = [m for m in matches if m not in exclude_methods]

                        if matches:
                            found_count += 1
                            summary += f"### 🧩 {class_name}\n"
                            for method_name in matches:
                                summary += f"- `{method_name}()`\n"
                            summary += "\n"
                except Exception as e:
                    print(f"❌ Error reading {file}: {e}")
                    
    if found_count == 0:
        return "No Services found."

    return summary

# ==============================================================================
# 4. 메인 실행
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
    
    # [수정됨] backend 폴더 우선 탐색 로직 적용
    java_src_path = find_java_source_root(root)
    
    print(f"🚀 분석 시작 경로: {java_src_path}")
    
    # 프로젝트 구조는 전체 루트 기준으로 보여줌 (backend 폴더 포함)
    structure = get_project_structure(root)
    
    # 자바 분석은 java_src_path 기준
    controller_summary = summarize_controller(java_src_path)
    service_summary = summarize_service(java_src_path)
    toc = build_table_of_contents()
    
    readme_content = README_TEMPLATE.format(
        table_of_contents=toc,
        project_structure=structure,
        controller_summary=controller_summary,
        service_summary=service_summary,
        generated_at=datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    )
    
    with open("README.md", "w", encoding="utf-8") as f:
        f.write(readme_content)
    
    print("✅ README.md 자동 생성 완료!")

if __name__ == "__main__":
    main()
