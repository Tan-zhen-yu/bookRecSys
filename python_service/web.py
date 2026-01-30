from flask import Flask, send_from_directory
import os

app = Flask(__name__)

# --- 跨平台核心逻辑 ---
# 1. 获取当前文件(web.py)所在的目录路径
base_dir = os.path.dirname(os.path.abspath(__file__))

# 2. 定义图片文件夹相对于当前文件的位置
# 只要你的 book_covers 文件夹和 web.py 在一起，代码就永远有效
IMAGE_FOLDER = os.path.join(base_dir, "book_covers")

@app.route('/covers/<filename>')
def get_cover(filename):
    # 调试日志：在不同系统下会打印出对应的正确路径
    print(f"Server is looking for: {os.path.join(IMAGE_FOLDER, filename)}")
    return send_from_directory(IMAGE_FOLDER, filename)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5001)