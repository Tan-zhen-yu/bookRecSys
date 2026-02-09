package com.example.bookrec.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.bookrec.common.JwtUtils;
import com.example.bookrec.common.Result;
import com.example.bookrec.entity.BookCategory;
import com.example.bookrec.entity.BookInfo;
import com.example.bookrec.entity.User;
import com.example.bookrec.entity.UserBookRating;
import com.example.bookrec.service.IBookCategoryService;
import com.example.bookrec.service.IBookInfoService;
import com.example.bookrec.service.IUserBookRatingService;
import com.example.bookrec.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService userService;

    // 在 UserController 中添加
    @Autowired
    private JwtUtils jwtUtils; // 注入JWT工具类
    @Autowired
    private IUserBookRatingService ratingService;
    @Autowired
    private IBookInfoService bookInfoService;
    @Autowired
    private IBookCategoryService categoryService; // 记得注入分类Service

    @Autowired
    private RedisTemplate<String, Object> redisTemplate; // 注入 Redis

    @GetMapping("/radar/{userId}")
    public Result<Map<String, Object>> getUserRadar(@PathVariable Long userId) {
        // 1. 查出用户所有的评分记录
        LambdaQueryWrapper<UserBookRating> query = new LambdaQueryWrapper<>();
        query.eq(UserBookRating::getUserId, userId);
        List<UserBookRating> ratingList = ratingService.list(query);

        // 2. 统计各分类的数量
        // Key: 分类名, Value: 数量
        Map<String, Integer> categoryCountMap = new HashMap<>();

        // 初始化所有分类为0 (防止雷达图缺角)
        List<BookCategory> allCategories = categoryService.list();
        for (BookCategory cat : allCategories) {
            categoryCountMap.put(cat.getName(), 0);
        }

        for (UserBookRating rating : ratingList) {
            BookInfo book = bookInfoService.getById(rating.getBookId());
            if (book != null) {
                // 拿到这本书的分类名
                BookCategory cat = categoryService.getById(book.getCategoryId());
                if (cat != null) {
                    String catName = cat.getName();
                    categoryCountMap.put(catName, categoryCountMap.getOrDefault(catName, 0) + 1);
                }
            }
        }

        // 3. 封装成 ECharts 需要的格式
        // indicator: [{name: '科幻', max: 10}, ...]
        // values: [5, 2, ...]
        List<Map<String, Object>> indicators = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        // 找出最大值，用于设置雷达图的 max
        int maxCount = 0;
        for (Integer count : categoryCountMap.values()) {
            if (count > maxCount) maxCount = count;
        }
        int radarMax = maxCount == 0 ? 10 : maxCount + 2; //稍微大一点

        for (Map.Entry<String, Integer> entry : categoryCountMap.entrySet()) {
            Map<String, Object> indicator = new HashMap<>();
            indicator.put("name", entry.getKey());
            indicator.put("max", radarMax);
            indicators.add(indicator);

            values.add(entry.getValue());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("indicators", indicators);
        result.put("values", values);

        return Result.success(result);
    }

    // 登录接口: POST /user/login
    @PostMapping("/login")
    public Result<User> login(@RequestBody User user, HttpServletResponse response) {
        // 1. 业务登录逻辑 (保持不变)
        // 如果登录失败，userService.login 通常会抛出异常或返回 null，请根据你的逻辑处理
        User loginUser = userService.login(user);

        // ------------------- 新增逻辑开始 -------------------

        // 2. 生成 JWT 字符串
        // 这里的 createToken 方法需要你自己实现，通常传入 userId 或 username
        String jwtToken = jwtUtils.createToken(loginUser.getId(), loginUser.getUsername());

        // 3. 构建 HttpOnly Cookie
        // 使用 Spring 提供的 ResponseCookie 工具，因为它能方便地设置 SameSite 属性
        ResponseCookie cookie = ResponseCookie.from("auth_token", jwtToken) // cookie名称: auth_token
                .httpOnly(true)       // 【关键】禁止前端 JS 读取，防御 XSS
                .secure(false)        // 本地开发填 false，上线 HTTPS 后必须改为 true
                .path("/")            // 允许全站访问
                .maxAge(24 * 60 * 60) // Cookie 有效期（单位：秒），这里设为 24 小时
                .sameSite("Strict")   // 【关键】防御 CSRF，Strict 模式下第三方链接无法携带 Cookie
                .build();

        // 4. 将 Cookie 加入响应头
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // ------------------- 新增逻辑结束 -------------------

        // 5. 返回用户信息 (注意：返回体中不再包含 Token，因为 Token 已经在 Cookie 里了)
        // 为了安全，建议 loginUser 里不要包含 password 字段
        return Result.success(loginUser);
    }

    // 注册接口: POST /user/register
    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        User registerUser = userService.register(user);
        return Result.success(registerUser);
    }

    // 获取用户信息: GET /user/{id}
    // 1. 先放具体的路径
    @GetMapping("/list")
    public Result<List<User>> listUsers() {
        return Result.success(userService.list());
    }

    // 2. 再放带变量的路径
    @GetMapping("/{id}")
    public Result<User> getUserInfo(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteUser(@PathVariable Long id) {
        return Result.success(userService.removeById(id));
    }

    @PostMapping("/tags")
    public Result<Boolean> updateTags(@RequestBody Map<String, Object> params) {
        // 1. 校验参数
        if (params.get("userId") == null || params.get("tags") == null) {
            return Result.error("参数缺失");
        }

        // 2. 获取参数
        // 注意：前端传来的数字有时候可能是 Integer，转 String 再转 Long 最稳妥
        Long userId = Long.valueOf(params.get("userId").toString());
        String tags = params.get("tags").toString();

        // 3. 构建更新对象
        User user = new User();
        user.setId(userId);
        user.setTags(tags); // 只设置 ID 和 Tags，MP 只会更新 Tags 字段

        // 4. 执行更新
        boolean success = userService.updateById(user);




        // --- 新增：清除推荐缓存 ---
        if (success) {
            String cacheKey = "rec:user:" + userId;
            redisTemplate.delete(cacheKey);
            System.out.println("用户修改标签，已清除缓存: " + cacheKey);
        }

        return Result.success(success);
    }

    @PostMapping("/password")
    public Result<Boolean> updatePassword(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        String oldPass = (String) params.get("oldPass");
        String newPass = (String) params.get("newPass");

        User user = userService.getById(userId);
        // 这里记得加上你用的加密算法，比如 MD5
        // String oldMd5 = DigestUtil.md5Hex(oldPass);

        if (!user.getPassword().equals(oldPass)) { // 如果用了加密，这里比对加密后的
            return Result.error("原密码错误");
        }

        user.setPassword(newPass); // 记得加密 newPass
        return Result.success(userService.updateById(user));
    }

    // 修改基本信息 (昵称、头像)
    @PostMapping("/update")
    public Result<Boolean> updateInfo(@RequestBody User user) {
        return Result.success(userService.updateById(user));
    }
}