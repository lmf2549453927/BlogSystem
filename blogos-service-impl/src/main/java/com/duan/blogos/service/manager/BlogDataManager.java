package com.duan.blogos.service.manager;

import com.duan.blogos.service.dao.BlogCategoryRelaDao;
import com.duan.blogos.service.dao.BlogLabelRelaDao;
import com.duan.blogos.service.dao.BlogCategoryDao;
import com.duan.blogos.service.dao.BlogDao;
import com.duan.blogos.service.dao.BlogLabelDao;
import com.duan.blogos.service.dao.BlogStatisticsDao;
import com.duan.blogos.service.dao.BloggerAccountDao;
import com.duan.blogos.service.dao.BloggerPictureDao;
import com.duan.blogos.service.dao.BloggerProfileDao;
import com.duan.blogos.service.common.dto.blog.BlogListItemDTO;
import com.duan.blogos.service.common.dto.blogger.BloggerDTO;
import com.duan.blogos.service.common.dto.blogger.FavoriteBlogListItemDTO;
import com.duan.blogos.service.entity.BlogCategoryRela;
import com.duan.blogos.service.entity.BlogLabelRela;
import com.duan.blogos.service.entity.Blog;
import com.duan.blogos.service.entity.BlogCategory;
import com.duan.blogos.service.entity.BlogLabel;
import com.duan.blogos.service.entity.BlogStatistics;
import com.duan.blogos.service.entity.BloggerAccount;
import com.duan.blogos.service.entity.BloggerPicture;
import com.duan.blogos.service.entity.BloggerProfile;
import com.duan.blogos.service.common.enums.BloggerPictureCategoryEnum;
import com.duan.blogos.service.common.util.DataConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 2018/9/26.
 *
 * @author DuanJiaNing
 */
@Component
public class BlogDataManager {

    @Autowired
    private BlogCategoryRelaDao categoryRelaDao;

    @Autowired
    private BlogLabelRelaDao labelRelaDao;

    @Autowired
    private BlogCategoryDao categoryDao;

    @Autowired
    private BlogDao blogDao;

    @Autowired
    private BlogLabelDao labelDao;

    @Autowired
    private BloggerAccountDao accountDao;

    @Autowired
    private BloggerProfileDao profileDao;

    @Autowired
    private BloggerPictureDao pictureDao;

    @Autowired
    private StringConstructorManager constructorManager;

    @Autowired
    private BlogStatisticsDao statisticsDao;

    public BlogListItemDTO getBlogListItemDTO(Blog blog, boolean findImg) {
        Long blogId = blog.getId();

        // 找一张图片
        String img = null;
        if (findImg) {
            String content = blog.getContent();
            Pattern pattern = Pattern.compile("<img src=\"(.*)\" .*>");
            Matcher matcher = pattern.matcher(content);
            if (matcher.find())
                img = matcher.group(1);
        }

        BlogCategory[] array = null;
        List<BlogCategoryRela> cts = categoryRelaDao.listAllByBlogId(blogId);
        if (!CollectionUtils.isEmpty(cts)) {
            array = cts.stream()
                    .map(rela -> categoryDao.getCategory(rela.getCategoryId()))
                    .toArray(BlogCategory[]::new);
        }

        BlogLabel[] a2 = null;
        List<BlogLabelRela> lbs = labelRelaDao.listAllByBlogId(blogId);
        if (!CollectionUtils.isEmpty(lbs)) {
            a2 = lbs.stream()
                    .map(rela -> labelDao.getLabel(rela.getLabelId()))
                    .toArray(BlogLabel[]::new);
        }

        BlogStatistics statistics = statisticsDao.getStatistics(blogId);
        return DataConverter.PO2DTO.blogListItemToDTO(statistics, array, a2, blog, img);
    }

    public FavoriteBlogListItemDTO getFavouriteBlogListItemDTO(Long bloggerId, Long blogId, Long id, Timestamp date,
                                                               String reason) {

        // BlogListItemDTO
        Blog blog = blogDao.getBlogById(blogId);
        BlogListItemDTO listItemDTO = getBlogListItemDTO(blog, false);

        // BloggerDTO
        Long authorId = blog.getBloggerId();
        BloggerAccount account = accountDao.getAccountById(authorId);
        BloggerProfile profile = profileDao.getProfileByBloggerId(authorId);
        BloggerPicture avatar = null;
        if (profile != null) {
            avatar = profile.getAvatarId() == null ? null :
                    pictureDao.getPictureById(profile.getAvatarId());
        }

        // 使使用默认的博主头像
        if (avatar == null) {
            avatar = new BloggerPicture();
            avatar.setCategory(BloggerPictureCategoryEnum.PUBLIC.getCode());
            avatar.setBloggerId(authorId);
            avatar.setId(null);
        }

        String url = constructorManager.constructPictureUrl(avatar, BloggerPictureCategoryEnum.DEFAULT_BLOGGER_AVATAR);
        avatar.setPath(url);

        BloggerDTO bloggerDTO = DataConverter.PO2DTO.bloggerAccountToDTO(account, profile, avatar);

        // 结果
        return DataConverter.PO2DTO.favoriteBlogListItemToDTO(bloggerId, id, date, reason,
                listItemDTO, bloggerDTO);
    }

}
