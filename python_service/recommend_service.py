from flask import Flask, request, jsonify
import pandas as pd
import numpy as np
from sqlalchemy import create_engine, text
from sklearn.metrics.pairwise import cosine_similarity
import jieba
import jieba.analyse

app = Flask(__name__)

# -------------------------
# 1. 数据库配置
# -------------------------
DB_URI = 'mysql+pymysql://root:root@localhost:3306/book_rec_db?charset=utf8mb4'
db_engine = create_engine(DB_URI)

item_similarity_df = pd.DataFrame()
user_item_matrix = pd.DataFrame()

def init_data():
    """
    服务启动时加载数据。
    改进：融合 评分(Score)、收藏(Shelf)、浏览(History) 三种行为数据。
    """
    global item_similarity_df, user_item_matrix
    print(">>> [Init] 正在加载多维度行为数据...")
    
    # 使用 SQL UNION ALL 将三张表的数据合并
    # 权重设定：
    # 1. 评分：使用实际分数 (1-5)
    # 2. 收藏：视为 5 分 (强兴趣)
    # 3. 浏览：视为 1 分 (弱兴趣)
    
    query = """
    SELECT user_id, book_id, score FROM user_book_rating
    UNION ALL
    SELECT user_id, book_id, 5.0 as score FROM user_book_shelf
    UNION ALL
    SELECT user_id, book_id, 1.0 as score FROM user_browsing_history
    """
    
    try:
        # 1. 读取混合数据
        raw_df = pd.read_sql(query, db_engine)
        
        if raw_df.empty:
            print(">>> [Init] 警告：数据库中没有任何行为数据！")
            return

        # 2. 数据聚合 (Data Aggregation)
        # 一个用户可能对同一本书既浏览过，又收藏过，又评过分。
        # 我们取最大值 (MAX) 作为最终兴趣分。
        # 例如：浏览(1.0) + 收藏(5.0) -> 取 5.0
        unified_df = raw_df.groupby(['user_id', 'book_id'])['score'].max().reset_index()

        # 3. 构建矩阵
        user_item_matrix = unified_df.pivot_table(index='user_id', columns='book_id', values='score').fillna(0)
        
        # 4. 计算相似度
        item_similarity = cosine_similarity(user_item_matrix.T)
        item_similarity_df = pd.DataFrame(item_similarity, index=user_item_matrix.columns, columns=user_item_matrix.columns)
        
        print(f">>> [Init] 多维数据加载完成！处理记录数: {len(unified_df)}, 物品数: {len(item_similarity_df)}")
        
    except Exception as e:
        print(f">>> [Init] 初始化失败: {e}")

# -------------------------
# 逻辑1: 多维度 ItemCF
# -------------------------
def get_user_unified_vector(user_id):
    """
    辅助函数：获取用户实时的综合兴趣向量 (评分 + 收藏 + 浏览)
    返回: dict {book_id: score}
    """
    try:
        # 联合查询用户的实时行为
        sql = text("""
        SELECT book_id, score FROM user_book_rating WHERE user_id = :uid
        UNION ALL
        SELECT book_id, 5.0 as score FROM user_book_shelf WHERE user_id = :uid
        UNION ALL
        SELECT book_id, 1.0 as score FROM user_browsing_history WHERE user_id = :uid
        """)
        
        with db_engine.connect() as conn:
            user_data = conn.execute(sql, {"uid": user_id}).fetchall()
        
        if not user_data:
            return {}
            
        # 聚合：如果有重复记录，取最大值
        user_vector = {}
        for row in user_data:
            bid = row[0]
            score = float(row[1]) # 强制转 float
            
            if bid in user_vector:
                user_vector[bid] = max(user_vector[bid], score)
            else:
                user_vector[bid] = score
                
        return user_vector
    except Exception as e:
        print(f"获取用户向量失败: {e}")
        return {}

def get_item_cf_recommend(target_user_id, top_n=6):
    global item_similarity_df
    
    if item_similarity_df.empty:
        return []

    try:
        # 1. 获取用户综合兴趣向量 (不再只是评分)
        user_ratings_dict = get_user_unified_vector(target_user_id)
        
        if not user_ratings_dict:
            return []
        
        recommend_scores = {}

        # 2. 经典的 ItemCF 计算公式
        for rated_book, rating in user_ratings_dict.items():
            if rated_book not in item_similarity_df.index: continue
            
            # 找相似书
            sim_books = item_similarity_df[rated_book].sort_values(ascending=False).head(20)
            
            for sim_book, similarity in sim_books.items():
                if sim_book in user_ratings_dict or similarity < 0.1: continue
                
                recommend_scores.setdefault(sim_book, 0)
                # 核心：相似度 * (评分/收藏/浏览权重)
                recommend_scores[sim_book] += similarity * rating

        sorted_books = sorted(recommend_scores.items(), key=lambda x: x[1], reverse=True)
        max_score = sorted_books[0][1] if sorted_books else 1.0
        
        results = []
        for book_id, score in sorted_books[:top_n]:
            # 归一化逻辑
            percent = int((score / max_score) * 99)
            if percent < 80: percent = 80
            results.append({"book_id": int(book_id), "score": percent})
        
        print(f">>> [ItemCF] 基于多维行为算出 {len(results)} 本推荐")
        return results

    except Exception as e:
        print(f">>> [ItemCF] 出错: {e}")
        return []

# -------------------------
# 逻辑2: Tags (基于标签)
# -------------------------
def get_books_by_user_tags(user_id, limit=6):
    try:
        sql = text("SELECT tags FROM sys_user WHERE id = :uid")
        with db_engine.connect() as conn:
            res = conn.execute(sql, {"uid": user_id}).fetchone()
        
        if not res or not res[0]:
            print(f">>> [Tags] 用户 {user_id} 没有设置标签")
            return []
            
        tags_str = res[0] 
        tag_list = [t.strip() for t in tags_str.split(',') if t.strip().isdigit()]
        if not tag_list: return []
        
        clean_tags = ",".join(tag_list)
        
        # 标签推荐也给个较高的分数
        book_sql = text(f"SELECT id FROM book_info WHERE category_id IN ({clean_tags}) ORDER BY rating_count DESC LIMIT :limit")
        with db_engine.connect() as conn:
            books = conn.execute(book_sql, {"limit": limit}).fetchall()
            
        results = [{"book_id": int(r[0]), "score": 90} for r in books]
        print(f">>> [Tags] 根据标签找到了 {len(results)} 本书")
        return results

    except Exception as e:
        print(f">>> [Tags] 出错: {e}")
        return []

# -------------------------
# 接口
# -------------------------
@app.route('/recommend', methods=['GET'])
def recommend():
    try:
        user_id = request.args.get('userId')
        if not user_id: return jsonify({'code': 400, 'msg': 'missing userId'})
        user_id = int(user_id)
        
        final_list = []
        seen = set()

        # 1. 多维 ItemCF
        cf_list = get_item_cf_recommend(user_id, top_n=6)
        for item in cf_list:
            if item['book_id'] not in seen:
                final_list.append(item)
                seen.add(item['book_id'])

        # 2. Tags 补齐
        if len(final_list) < 6:
            need = 6 - len(final_list)
            print(f">>> [Mix] ItemCF 只有 {len(final_list)} 本，正在用 Tags 补齐...")
            tag_list = get_books_by_user_tags(user_id, limit=need + 5)
            for item in tag_list:
                if len(final_list) >= 6: break
                if item['book_id'] not in seen:
                    final_list.append(item)
                    seen.add(item['book_id'])

        return jsonify({'code': 200, 'msg': 'success', 'data': final_list})

    except Exception as e:
        print(f">>> [Error] {e}")
        return jsonify({'code': 500, 'msg': str(e), 'data': []})

# ... related, keywords, reload 接口保持不变 (一定要保留) ...
@app.route('/related', methods=['GET'])
def get_related_books():
    """ 详情页：看了这本书的人也喜欢 (ItemCF) """
    try:
        book_id = request.args.get('bookId')
        if not book_id:
            return jsonify({'code': 400, 'msg': 'missing bookId', 'data': []})
        
        book_id = int(book_id)
        
        global item_similarity_df
        if item_similarity_df.empty or book_id not in item_similarity_df.index:
            return jsonify({'code': 200, 'msg': 'no data', 'data': []})
            
        sim_series = item_similarity_df[book_id].sort_values(ascending=False)
        related_books = sim_series[(sim_series < 0.99) & (sim_series > 0)].head(6)
        
        results = []
        for sim_id, similarity in related_books.items():
            score = int(similarity * 100)
            if score < 60: score = 60
            results.append({"book_id": int(sim_id), "score": score})
            
        return jsonify({'code': 200, 'msg': 'success', 'data': results})

    except Exception as e:
        return jsonify({'code': 500, 'msg': str(e), 'data': []})
    
@app.route('/keywords', methods=['POST'])
def extract_keywords():
    """ 词云接口 """
    try:
        data = request.get_json()
        text = data.get('text', '')
        if not text: return jsonify({'code': 200, 'msg': 'ok', 'data': []})
        keywords = jieba.analyse.extract_tags(text, topK=20, withWeight=True)
        result = [{"name": w, "value": int(v*100)} for w, v in keywords if len(w)>1]
        return jsonify({'code': 200, 'msg': 'success', 'data': result})
    except Exception as e:
        return jsonify({'code': 500, 'msg': str(e), 'data': []})

@app.route('/reload', methods=['GET'])
def reload_data():
    init_data()
    return jsonify({'code': 200, 'msg': 'ok'})

if __name__ == '__main__':
    init_data()
    app.run(host='0.0.0.0', port=5000, debug=False)