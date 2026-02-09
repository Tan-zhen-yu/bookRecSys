package com.tzy;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.ingest.simulate.Document;
import com.example.bookrec.BookRecApplication;
import com.example.bookrec.entity.BookInfo;
import com.example.bookrec.entity.User;
import com.example.bookrec.mapper.BookInfoMapper;
import com.example.bookrec.mapper.UserMapper;
import org.apache.http.client.HttpClient;
import org.dromara.easyes.core.conditions.select.LambdaEsQueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import java.net.URI;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest(classes = BookRecApplication.class)
public  class UserDataInsertTest {

    @Autowired
    private DataSource dataSource; // 只要能连上数据库，这个 Bean 一定存在
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private BookInfoMapper bookInfoMapper;

    @Test
    void stableInsert() {
        String sql = "INSERT INTO sys_user (username, password, nickname, email, role, tags) VALUES (?, ?, ?, ?, ?, ?)";

        long startTime = System.currentTimeMillis();

        // 1. 使用 try-with-resources 自动关闭连接
        try (Connection conn = dataSource.getConnection()) {
            // 2. 关键：关闭自动提交，手动控制事务
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                System.out.println("开始稳定插入 1,000,000 条数据...");

                for (int i = 1; i <= 10000000; i++) {
                    // 3. 填充参数
                    ps.setString(1, "user_" + i + "_" + UUID.randomUUID().toString().substring(0, 8));
                    ps.setString(2, "e10adc3949ba59abbe56e057f20f883e"); // 123456
                    ps.setString(3, "测试昵称" + i);
                    ps.setString(4, i + "@example.com");
                    ps.setInt(5, 0);
                    ps.setString(6, "Java,仿真,AI");

                    // 4. 添加到批处理包中
                    ps.addBatch();

                    // 5. 每 2000 条数据执行一次物理写入并提交
                    if (i % 2000 == 0) {
                        ps.executeBatch(); // 批量发送到数据库
                        conn.commit();     // 提交事务
                        ps.clearBatch();   // 清空当前批次，准备下一批
                    }

                    if (i % 100000 == 0) {
                        System.out.println("进度反馈：已成功插入 " + i + " 条数据");
                    }
                }

                // 6. 处理剩余不满 2000 条的数据
                ps.executeBatch();
                conn.commit();

            } catch (Exception e) {
                conn.rollback(); // 出错回滚
                throw e;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        System.out.println("插入任务圆满结束！总耗时：" + (endTime - startTime) / 1000 + " 秒");
    }

    @Test
    void measureFirstTime() {
        // 第一次查询计时
        String name="user_98";
        long start = System.currentTimeMillis();
        userMapper.getIdOnly(name);
        long end = System.currentTimeMillis();

        System.out.println("第一次查询（冷启动）耗时：" + (end - start) + "ms");

        // 第二次查询对照
        start = System.currentTimeMillis();
        userMapper.getIdOnly(name);
        System.out.println("第二次查询（热数据）耗时：" + (System.currentTimeMillis() - start) + "ms");
        return;
    }

    @Test
    void measureBookseel() throws InterruptedException{
        int threadCount = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        // 发令枪：用于保证 1000 个线程同时起跑
        CountDownLatch startLatch = new CountDownLatch(1);
        // 计数器：用于统计所有线程是否跑完
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        String urlString = "http://localhost:8080/book/test/borrow/unsafe?id=101";

        System.out.println("正在创建 1000 个并发线程...");

        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    // 所有线程在这里阻塞，等待 startLatch.countDown()
                    startLatch.await();

                    // 发起 HTTP 请求
                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    // 读取响应（必须读取，否则请求可能不会真正发出）
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String result = in.readLine();
                    System.out.println(Thread.currentThread().getName() + " -> " + result);
                    in.close();

                } catch (Exception e) {
                    System.err.println("请求失败: " + e.getMessage());
                } finally {
                    // 无论成功失败，线程执行完毕，endLatch 减 1
                    endLatch.countDown();
                }
            });
        }

        System.out.println("线程准备完毕，3秒后准时开火！");
        Thread.sleep(3000);

        long startTime = System.currentTimeMillis();

        // --- 核心点：砰！发令枪响，1000 个线程瞬间被唤醒 ---
        startLatch.countDown();

        // 等待所有线程执行完毕
        endLatch.await();

        long endTime = System.currentTimeMillis();
        executorService.shutdown();

        System.out.println("\n--- 测试完成 ---");
        System.out.println("并发请求数：" + threadCount);
        System.out.println("总耗时：" + (endTime - startTime) + "ms");
        System.out.println("请现在去数据库检查库存 count 是否为负数！");
    }

    @Test
    public void testSearchAfterFirst() {

    }

    // 后续查询
    @Test
    public void testSearchAfter() {

    }



}