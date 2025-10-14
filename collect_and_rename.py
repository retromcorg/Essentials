"""
DISCLAIMER

This code was written by ChatGPT and DeepSeek.
Any garbage code practices in place in this file are to be blamed on AI.
"""

import os
import shutil
import re

SOURCE_DIR = os.getcwd()
TARGET_DIR = os.path.join(SOURCE_DIR, "BuiltJars")

def rename_file(old_name: str) -> str:
    pattern = r"^([a-zA-Z-]+)-[\d.]+(?:-[a-zA-Z-]+)*\.jar$"
    match = re.match(pattern, old_name)
    
    if match:
        name_part = match.group(1)
        parts = name_part.split('-')
        capitalized = ''.join(part.capitalize() for part in parts)
        return f"{capitalized}.jar"
    else:
        return old_name
    
def clear_target_directory():
    if os.path.exists(TARGET_DIR):
        for file_name in os.listdir(TARGET_DIR):
            file_path = os.path.join(TARGET_DIR, file_name)
            if os.path.isfile(file_path):
                os.remove(file_path)
                print(f"Removed: {file_path}")

def collect_jar_files():
    os.makedirs(TARGET_DIR, exist_ok=True)

    clear_target_directory()

    for root, _, files in os.walk(SOURCE_DIR):
        if os.path.basename(root) == "target":
            for file in files:
                if file.endswith(".jar"):
                    old_path = os.path.join(root, file)
                    new_name = rename_file(file)
                    new_path = os.path.join(TARGET_DIR, new_name)

                    shutil.move(old_path, new_path)

if __name__ == "__main__":
    collect_jar_files()
