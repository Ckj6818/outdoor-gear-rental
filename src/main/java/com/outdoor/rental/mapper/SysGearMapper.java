package com.outdoor.rental.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.outdoor.rental.entity.SysGear;
import com.outdoor.rental.vo.AiRagGearVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysGearMapper extends BaseMapper<SysGear> {

    /**
     * 查询已上架且有可用库存的装备（AI 导购 RAG 上下文，仅返回必要字段以节省 Token）。
     */
    @Select("""
            SELECT id,
                   gear_name   AS name,
                   category,
                   daily_rent  AS dailyRent
            FROM gear_info
            WHERE status = 1
              AND available_stock > 0
            ORDER BY category, id
            """)
    List<AiRagGearVO> selectAvailableOnShelfWithStock();
}
