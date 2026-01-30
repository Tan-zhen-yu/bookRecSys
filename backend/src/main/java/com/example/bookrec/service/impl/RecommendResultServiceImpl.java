package com.example.bookrec.service.impl;

import com.example.bookrec.entity.RecommendResult;
import com.example.bookrec.mapper.RecommendResultMapper;
import com.example.bookrec.service.IRecommendResultService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 推荐结果缓存表 服务实现类
 * </p>
 *
 * @author Admin
 * @since 2026-01-26
 */
@Service
public class RecommendResultServiceImpl extends ServiceImpl<RecommendResultMapper, RecommendResult> implements IRecommendResultService {

}
