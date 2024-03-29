package com.zhangxu.agmall.service.impl;

import com.zhangxu.agmall.dao.ProductImgMapper;
import com.zhangxu.agmall.dao.ProductMapper;
import com.zhangxu.agmall.dao.ProductParamsMapper;
import com.zhangxu.agmall.dao.ProductSkuMapper;
import com.zhangxu.agmall.entity.*;
import com.zhangxu.agmall.service.ProductService;
import com.zhangxu.agmall.utils.PageHelper;
import com.zhangxu.agmall.vo.ResStatus;
import com.zhangxu.agmall.vo.ResultVO;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * @author zhangxu
 * @create 2023-04-11
 */
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductImgMapper productImgMapper;

    @Autowired
    private ProductSkuMapper productSkuMapper;

    @Autowired
    private ProductParamsMapper productParamsMapper;

    @Override
    public ResultVO listRecommendProducts() {
        List<ProductVO> productVOS = productMapper.selectRecommendProducts();
        return new ResultVO(ResStatus.OK, "success", productVOS);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public ResultVO getProductBaseInfo(String productId) {
//        商品基本信息
        Example example = new Example(Product.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("productId", productId);
        criteria.andEqualTo("productStatus", 1);
        List<Product> products = productMapper.selectByExample(example);
//当查询到商品基本信息的时候，才可以商品的其他的信息
        if (products.size() > 0) {
            //      商品图片
            Example exampleImg = new Example(ProductImg.class);
            Example.Criteria criteriaImg = exampleImg.createCriteria();
            criteriaImg.andEqualTo("itemId", productId);
            List<ProductImg> productImgs = productImgMapper.selectByExample(exampleImg);
            //        商品套餐
            Example exampleSku = new Example(ProductSku.class);
            Example.Criteria criteriaSku = exampleSku.createCriteria();
            criteriaSku.andEqualTo("productId", productId);
            criteriaSku.andEqualTo("status", 1);
            List<ProductSku> productSkus = productSkuMapper.selectByExample(exampleSku);
            HashMap<String, Object> baseInfo = new HashMap<>();
            baseInfo.put("products", products);
            baseInfo.put("productImgs", productImgs);
            baseInfo.put("productSkus", productSkus);
            return new ResultVO(ResStatus.OK, "success", baseInfo);
        } else {
            return new ResultVO(ResStatus.NO, "您查询的商品不存在", null);
        }
    }

    @Override
    public ResultVO getProductParamsById(String productId) {
        Example example = new Example(ProductParams.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("productId", productId);
        List<ProductParams> productParams = productParamsMapper.selectByExample(example);
        ResultVO resultVO;
        if (productParams.size() > 0) {
            resultVO = new ResultVO(ResStatus.OK, "success", productParams);
        } else {
            resultVO = new ResultVO(ResStatus.NO, "fail", null);
        }
        return resultVO;
    }

    //    通过ID查询商品
    @Override
    public ResultVO getProductById(String productId) {
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product != null) {
            return new ResultVO(ResStatus.OK, "success", product);
        }
        return new ResultVO(ResStatus.NO, "fail", null);
    }

    @Override
    public ResultVO getProductImgById(String productId) {
        List<ProductImg> productImgs =
                productImgMapper.selectProductImgByProductId(Integer.parseInt(productId));
        if (productImgs != null) {
            return new ResultVO(ResStatus.OK, "success", productImgs);
        }
        return new ResultVO(ResStatus.NO, "fail", null);
    }

    @Override
    public ResultVO getProductSkuById(String productId) {
        Example example = new Example(ProductSku.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("productId", productId);
        List<ProductSku> productSkus = productSkuMapper.selectByExample(example);
        if (productSkus != null) {
            return new ResultVO(ResStatus.OK, "success", productSkus);
        }
        return new ResultVO(ResStatus.NO, "fail", null);
    }


    @Override
    public ResultVO getProductsByCategoryId(int categoryId, int pageNum, int limit) {
        int start = (pageNum - 1) * limit;
        List<ProductVO> productVOS = productMapper.selectProductByCategoryId(categoryId, start, limit);
        //2.查询当前类别下的商品的总记永数
        Example example = new Example(Product.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("categoryId", categoryId);
        int count = productMapper.selectCountByExample(example);//3.计算总页数
        int pageCount = count / limit == 0 ? count / limit : count / limit + 1;
        PageHelper<ProductVO> pageHelper = new PageHelper<>(count, pageCount, productVOS);
        return new ResultVO(ResStatus.OK, "SUCCESS", pageHelper);
    }

    @Override
    public ResultVO listBrands(int cid) {
        List<String> brands = productMapper.selectBrandByCategoryId(cid);
        return new ResultVO(ResStatus.OK, "SUCCESS", brands);
    }

    /**
     * @param keyword搜索关键字
     * @param pageNum当前页码
     * @param limit每页的条数
     * @return
     */
    @Override
    public ResultVO searchProduct(String keyword, int pageNum, int limit) {
//
        int start = (pageNum - 1) * limit;
        //查询数据
        keyword = "%" + keyword + "%";
        List<ProductVO> productVOS = productMapper.selectProductByKeyword(keyword, start, limit);
//        查询记录数据
        Example example = new Example(Product.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andLike("productName", keyword);
        int count = productMapper.selectCountByExample(example);
        int pageCount = count % limit == 0 ? count / limit : count / limit + 1;
        PageHelper<ProductVO> pageHelper = new PageHelper<>(count, pageCount, productVOS);
        return new ResultVO(ResStatus.OK, "SUCCESS", pageHelper);
    }

    @Override
    public ResultVO listBrands(String keyword) {
        keyword = "%" + keyword + "%";
        List<String> brandList = productMapper.selectBrandByKeyword(keyword);
        return new ResultVO(ResStatus.OK, "SUCCESS", brandList);
    }

    @Override
    public ResultVO updateProductById(Product product) {
        int i = productMapper.updateByPrimaryKeySelective(product);
        if (i > 0) {
            return new ResultVO(ResStatus.OK, "SUCCESS", null);
        }
        return new ResultVO(ResStatus.NO, "fail", null);
    }

    @Override
    public ResultVO updateProductParamById(ProductParams productParams) {
        Example example = new Example(ProductParams.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("paramId", productParams.getParamId());
        int i = productParamsMapper.updateByExampleSelective(productParams, example);
        if (i > 0) {
            return new ResultVO(ResStatus.OK, "SUCCESS", null);
        }
        return new ResultVO(ResStatus.NO, "fail", null);
    }

    @Override
    public ResultVO getProductParamByPid(String paramId) {
        ProductParams productParams = productParamsMapper.selectByPrimaryKey(paramId);
        if (productParams==null) {
            return new ResultVO(ResStatus.OK, "SUCCESS", productParams);
        }
        return new ResultVO(ResStatus.NO, "fail", null);
    }

    @Override
    public ResultVO addProduct(Product product) {
        product.setCreateTime(new Date());
        product.setUpdateTime(new Date());
        product.setSoldNum(0);

        product.setProductId(Integer.toString(new Random().nextInt(2147483646)));
        int i = productMapper.insert(product);
        if (i > 0) {
            return new ResultVO(ResStatus.OK, "SUCCESS", product.getProductId());
        }
        return new ResultVO(ResStatus.NO, "fail", null);
    }

    @Override
    @Transactional
    public ResultVO deleteProduct(String productId) {
        int i = productMapper.deleteByPrimaryKey(productId);
        Example exampleParams = new Example(ProductParams.class);
        Example exampleImg = new Example(ProductImg.class);
        Example exampleSku = new Example(ProductSku.class);
        Example.Criteria criteriaParams = exampleParams.createCriteria();
        Example.Criteria criteriaImg = exampleImg.createCriteria();
        Example.Criteria criteriaSku = exampleSku.createCriteria();
        criteriaParams.andEqualTo("productId", productId);
        criteriaImg.andEqualTo("itemId", productId);
        criteriaSku.andEqualTo("productId", productId);
        int i1 = productParamsMapper.deleteByExample(exampleParams);
        int i2 = productImgMapper.deleteByExample(exampleImg);
        int i3 = productSkuMapper.deleteByExample(exampleSku);
        return new ResultVO(ResStatus.OK, "SUCCESS", null);
    }

    @Override
    public ResultVO addProductParam(ProductParams productParam) {
        productParam.setCreateTime(new Date());
        productParam.setUpdateTime(new Date());
        productParam.setParamId(Integer.toString(new Random().nextInt(2147483646) + 1));
        int i = productParamsMapper.insert(productParam);
        if (i > 0) {
            return new ResultVO(ResStatus.OK, "SUCCESS", null);
        }
        return new ResultVO(ResStatus.NO, "fail", null);
    }

    @Override
    public ResultVO deleteParam(String paramId) {
        int i = productParamsMapper.deleteByPrimaryKey(paramId);
        if (i > 0) {
            return new ResultVO(ResStatus.OK, "SUCCESS", null);
        }
        return new ResultVO(ResStatus.NO, "fail", null);
    }

}
