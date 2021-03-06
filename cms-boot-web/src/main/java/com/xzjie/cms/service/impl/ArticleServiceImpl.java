package com.xzjie.cms.service.impl;

import com.xzjie.cms.core.service.AbstractService;
import com.xzjie.cms.dto.CategoryTree;
import com.xzjie.cms.model.Article;
import com.xzjie.cms.model.Category;
import com.xzjie.cms.repository.ArticleRepository;
import com.xzjie.cms.repository.CategoryRepository;
import com.xzjie.cms.service.ArticleService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ArticleServiceImpl extends AbstractService<Article, Long> implements ArticleService {
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    protected JpaRepository getRepository() {
        return articleRepository;
    }

    @Override
    public Article getArticle(Long id) {
        return articleRepository.getOne(id);
    }

    @Override
    public Page<Article> getArticle(Integer page, int size, Article query) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return articleRepository.findAll((root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query == null) {
                return null;
            }
            if (null != query.getRecommendStat()) {
//                String attribute = query.getRecommendStat().getClass().getName();
                predicates.add(criteriaBuilder.equal(root.get("recommendStat").as(String.class), query.getRecommendStat()));
            }
            if (null != query.getCategoryId()) {
                predicates.add(criteriaBuilder.equal(root.get("categoryId").as(String.class), query.getCategoryId()));
            }

            //add add criteria to predicates
//            for (SearchCriteria criteria : list) {
//                if (criteria.getOperation().equals(SearchOperation.GREATER_THAN)) {
//                    predicates.add(builder.greaterThan(
//                            root.get(criteria.getKey()), criteria.getValue().toString()));
//                } else if (criteria.getOperation().equals(SearchOperation.LESS_THAN)) {
//                    predicates.add(builder.lessThan(
//                            root.get(criteria.getKey()), criteria.getValue().toString()));
//                } else if (criteria.getOperation().equals(SearchOperation.GREATER_THAN_EQUAL)) {
//                    predicates.add(builder.greaterThanOrEqualTo(
//                            root.get(criteria.getKey()), criteria.getValue().toString()));
//                } else if (criteria.getOperation().equals(SearchOperation.LESS_THAN_EQUAL)) {
//                    predicates.add(builder.lessThanOrEqualTo(
//                            root.get(criteria.getKey()), criteria.getValue().toString()));
//                } else if (criteria.getOperation().equals(SearchOperation.NOT_EQUAL)) {
//                    predicates.add(builder.notEqual(
//                            root.get(criteria.getKey()), criteria.getValue()));
//                } else if (criteria.getOperation().equals(SearchOperation.EQUAL)) {
//                    predicates.add(builder.equal(
//                            root.get(criteria.getKey()), criteria.getValue()));
//                } else if (criteria.getOperation().equals(SearchOperation.MATCH)) {
//                    predicates.add(builder.like(
//                            builder.lower(root.get(criteria.getKey())),
//                            "%" + criteria.getValue().toString().toLowerCase() + "%"));
//                } else if (criteria.getOperation().equals(SearchOperation.MATCH_END)) {
//                    predicates.add(builder.like(
//                            builder.lower(root.get(criteria.getKey())),
//                            criteria.getValue().toString().toLowerCase() + "%"));
//                } else if (criteria.getOperation().equals(SearchOperation.MATCH_START)) {
//                    predicates.add(builder.like(
//                            builder.lower(root.get(criteria.getKey())),
//                            "%" + criteria.getValue().toString().toLowerCase()));
//                } else if (criteria.getOperation().equals(SearchOperation.IN)) {
//                    predicates.add(builder.in(root.get(criteria.getKey())).value(criteria.getValue()));
//                } else if (criteria.getOperation().equals(SearchOperation.NOT_IN)) {
//                    predicates.add(builder.not(root.get(criteria.getKey())).in(criteria.getValue()));
//                }
//            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        }, pageable);
    }

    @Override
    public Category getCategory(Long id) {
        return categoryRepository.getOne(id);
    }

    @Override
    public List<Category> getCategory() {
        return categoryRepository.findCategoriesByPid(0L);
    }

    @Override
    public Page<Category> getCategory(Integer page, int size, Category query) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
//        return categoryRepository.findAll((root, criteriaQuery, criteriaBuilder) -> {
//            List<Predicate> predicates = new ArrayList<>();
//            if (query == null) {
//                return null;
//            }
//            if (null != query.getPid()) {
//                predicates.add(criteriaBuilder.equal(root.get("pid").as(String.class), query.getPid()));
//            }
//
//            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
//        }, pageable);
        return categoryRepository.findCategoriesByPid(0L, pageable);
    }

    @Override
    public List<CategoryTree> getCategoryTree() {
        List<Category> categories = categoryRepository.findCategoriesByPid(0L);

        List<CategoryTree> categoryTrees = new ArrayList<>();
        BeanUtils.copyProperties(categories, categoryTrees);
        categoryTrees = getTree(categoryTrees);
        return categoryTrees;
    }

    @Override
    public boolean saveCategory(Category category) {
        category.setCreateDate(LocalDateTime.now());
        category.setState(1);
        categoryRepository.save(category);
        return true;
    }

    @Override
    public boolean updateCategory(Category category) {
        Category model = categoryRepository.findById(category.getId()).orElseGet(Category::new);
        model.copy(category);
        categoryRepository.save(model);
        return true;
    }

    @Override
    public boolean deleteCategory(Long id) {
        categoryRepository.deleteById(id);
        return true;
    }

    private List<CategoryTree> getTree(List<CategoryTree> categoryTrees) {
        Map<Long, CategoryTree> dataMap = new HashMap<>();
        for (CategoryTree categoryTree : categoryTrees) {
            dataMap.put(categoryTree.getId(), categoryTree);
        }

        List<CategoryTree> trees = new ArrayList<>();
        for (Map.Entry<Long, CategoryTree> entry : dataMap.entrySet()) {
            CategoryTree categoryTree = entry.getValue();
            if (categoryTree.getParentId() == null) {
                trees.add(categoryTree);
            } else {
                if (dataMap.get(Long.valueOf(categoryTree.getParentId())) != null) {
                    dataMap.get(Long.valueOf(categoryTree.getParentId())).getChildren().add(categoryTree);
                }
            }
        }
        return trees;
    }


    @Override
    public boolean update(Article article) {
        Article model = articleRepository.findById(article.getId()).orElseGet(Article::new);
        model.copy(article);
        model.setUpdateDate(LocalDateTime.now());
        articleRepository.save(model);
        return true;
    }

}
