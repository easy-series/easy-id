package com.leaf.starter.segment.dao.impl;

import com.leaf.starter.segment.dao.IDAllocDao;
import com.leaf.starter.segment.model.LeafAlloc;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ID分配DAO实现类
 */
@Slf4j
public class IDAllocDaoImpl implements IDAllocDao {
    private final DataSource dataSource;

    public IDAllocDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public LeafAlloc getLeafAlloc(String tag) {
        String sql = "SELECT biz_tag, max_id, step, description, update_time FROM leaf_alloc WHERE biz_tag = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tag);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    LeafAlloc leafAlloc = new LeafAlloc();
                    leafAlloc.setBizTag(rs.getString("biz_tag"));
                    leafAlloc.setMaxId(rs.getLong("max_id"));
                    leafAlloc.setStep(rs.getInt("step"));
                    leafAlloc.setDescription(rs.getString("description"));
                    leafAlloc.setUpdateTime(rs.getTimestamp("update_time"));
                    return leafAlloc;
                }
            }
        } catch (SQLException e) {
            log.error("获取号段分配信息失败", e);
        }
        return null;
    }

    @Override
    public int updateMaxId(String tag, int step, long maxId) {
        String sql = "UPDATE leaf_alloc SET max_id = ? WHERE biz_tag = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, maxId);
            stmt.setString(2, tag);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("更新最大ID失败", e);
        }
        return 0;
    }

    @Override
    public int updateMaxIdByCustomStepAndConcurrent(LeafAlloc leafAlloc) {
        String sql = "UPDATE leaf_alloc SET max_id = max_id + step WHERE biz_tag = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, leafAlloc.getBizTag());
            return stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("更新最大ID失败", e);
        }
        return 0;
    }

    @Override
    public List<String> getAllTags() {
        List<String> tags = new ArrayList<>();
        String sql = "SELECT biz_tag FROM leaf_alloc";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tags.add(rs.getString("biz_tag"));
            }
        } catch (SQLException e) {
            log.error("获取所有业务标识失败", e);
        }
        return tags;
    }

    @Override
    public List<LeafAlloc> getAllLeafAllocs() {
        List<LeafAlloc> leafAllocs = new ArrayList<>();
        String sql = "SELECT biz_tag, max_id, step, description, update_time FROM leaf_alloc";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                LeafAlloc leafAlloc = new LeafAlloc();
                leafAlloc.setBizTag(rs.getString("biz_tag"));
                leafAlloc.setMaxId(rs.getLong("max_id"));
                leafAlloc.setStep(rs.getInt("step"));
                leafAlloc.setDescription(rs.getString("description"));
                leafAlloc.setUpdateTime(rs.getTimestamp("update_time"));
                leafAllocs.add(leafAlloc);
            }
        } catch (SQLException e) {
            log.error("获取所有号段分配信息失败", e);
        }
        return leafAllocs;
    }
} 