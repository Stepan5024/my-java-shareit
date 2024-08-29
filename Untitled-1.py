import os

# Путь к директории, которую нужно просканировать
directory_path = r'E:\StudyMai\Java\Спринт 13 Share It\java-shareit\my-java-shareit'

# Расширения файлов, которые нужно найти
file_extensions = ['.class', '.jar', '.DS_Store']

# Поиск файлов с указанными расширениями
def find_files(directory, extensions):
    matched_files = []
    for root, dirs, files in os.walk(directory):
        for file in files:
            if any(file.endswith(ext) for ext in extensions):
                matched_files.append(os.path.join(root, file))
    return matched_files

# Получаем список найденных файлов
found_files = find_files(directory_path, file_extensions)

# Выводим найденные файлы
for file in found_files:
    print(file)
