package com.sziti.easysocketlib.client.pojo;

import java.util.List;

/**
 * Created by LiTtleBayReal.
 * Date: 2020/1/19
 * Time: 22:54
 * Explain:专门用来解释如何判定是否剔除补发队列的规则
 */
public class CheckRules {
    //索引开始点
    private int[] offset;

    //对照组的
    private List<byte[]> checkBytes;

}
