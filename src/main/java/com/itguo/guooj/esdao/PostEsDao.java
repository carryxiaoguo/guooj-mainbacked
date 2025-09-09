package com.itguo.guooj.esdao;

import com.itguo.guooj.model.dto.post.PostEsDTO;
import java.util.List;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 帖子 ES 操作 类似mybatis的Mapper 主要处理ES
 *
 * 
 */
public interface PostEsDao extends ElasticsearchRepository<PostEsDTO, Long> {

    List<PostEsDTO> findByUserId(Long userId);
}