package com.luck.rtree;

import java.util.ArrayList;
import java.util.List;


/**
 * @ClassName RTDirNode
 * @Description 非叶节点
 */
public class RTDirNode extends RTNode
{
    /**
     * 孩子结点
     */
    protected List<RTNode> children;

    public RTDirNode(RTree rtree, RTNode parent, int level)
    {
        super(rtree, parent, level);
        children = new ArrayList<RTNode>();
    }

    /**
     * @param index
     * @return 对应索引下的孩子结点
     */
    public RTNode getChild(int index)
    {
        return children.get(index);
    }

    @Override
    public RTDataNode chooseLeaf(Rectangle rectangle)
    {
        int index;

        switch (rtree.getTreeType())
        {
            case Constants.RTREE_LINEAR:

            case Constants.RTREE_QUADRATIC:

            case Constants.RTREE_EXPONENTIAL:
                index = findLeastEnlargement(rectangle);
                break;
            case Constants.RSTAR:
                if(level == 1)//即此结点指向叶节点
                {
                    index = findLeastOverlap(rectangle);
                }else{
                    index = findLeastEnlargement(rectangle);
                }
                break;

            default:
                throw new IllegalStateException("Invalid tree type.");
        }

        insertIndex = index;//记录插入路径的索引

        return getChild(index).chooseLeaf(rectangle);
    }

    /**
     * @param rectangle
     * @return 返回最小重叠面积的结点的索引，若是重叠面积相等则选择加入此Rectangle后面积增量更小的，若是面积增量还相等则选择自身面积更小的
     */
    private int findLeastOverlap(Rectangle rectangle)
    {
        float overlap = Float.POSITIVE_INFINITY;
        int sel = -1;

        for(int i = 0; i < usedSpace; i ++)
        {
            RTNode node = getChild(i);
            float ol = 0;

            for(int j = 0; j < node.datas.length; j ++)
            {
                ol += rectangle.intersectingArea(node.datas[j]);
            }
            if(ol < overlap)
            {
                overlap = ol;//记录重叠面积最小的
                sel = i;//记录第几个孩子的索引
            }else if(ol == overlap)//若是重叠面积相等则选择加入此Rectangle后面积增量更小的,若是面积增量还相等则选择自身面积更小的
            {
                double area1 = datas[i].getUnionRectangle(rectangle).getArea() - datas[i].getArea();
                double area2 = datas[sel].getUnionRectangle(rectangle).getArea() - datas[sel].getArea();

                if(area1 == area2)
                {
                    sel = (datas[sel].getArea() <= datas[i].getArea()) ? sel : i;
                }else{
                    sel = (area1 < area2) ? i : sel;
                }
            }
        }
        return sel;
    }

    /**
     * @param rectangle
     * @return 面积增量最小的结点的索引，若是面积增量相等则选择自身面积更小的
     */
    private int findLeastEnlargement(Rectangle rectangle)
    {
        double area = Double.POSITIVE_INFINITY;
        int sel = -1;

        for(int i = 0; i < usedSpace; i ++)
        {
            double enlargement = datas[i].getUnionRectangle(rectangle).getArea() - datas[i].getArea();
            if(enlargement < area)
            {
                area = enlargement;
                sel = i;
            }else if(enlargement == area)
            {
                sel = (datas[sel].getArea() < datas[i].getArea()) ? sel : i;
            }
        }

        return sel;
    }

    /**
     * 插入新的Rectangle后从插入的叶节点开始向上调整RTree，直到根节点
     * @param node1 引发须要调整的孩子结点
     * @param node2  分裂的结点，若未分裂则为null
     */
    public void adjustTree(RTNode node1, RTNode node2)
    {
        //先要找到指向原来旧的结点（即未添加Rectangle以前）的条目的索引
        datas[insertIndex] = node1.getNodeRectangle();//先用node1覆盖原来的结点
        children.set(insertIndex, node1);//替换旧的结点

        if(node2 != null)
        {
            insert(node2);//插入新的结点

        }else if(! isRoot())//还没到达根节点
        {
            RTDirNode parent = (RTDirNode) getParent();
            parent.adjustTree(this, null);//向上调整直到根节点
        }
    }

    /**
     * @param node
     * @return 若是结点须要分裂则返回true
     */
    protected boolean insert(RTNode node)
    {
        if(usedSpace < rtree.getNodeCapacity())
        {
            datas[usedSpace ++] = node.getNodeRectangle();
            children.add(node);//新加的
            node.parent = this;//新加的
            RTDirNode parent = (RTDirNode) getParent();
            if(parent != null)
            {
                parent.adjustTree(this, null);
            }
            return false;
        }else{//非叶子结点须要分裂
            RTDirNode[] a = splitIndex(node);
            RTDirNode n = a[0];
            RTDirNode nn = a[1];

            if(isRoot())
            {
                //新建根节点，层数加1
                RTDirNode newRoot = new RTDirNode(rtree, Constants.NULL, level + 1);

                //还须要把原来结点的孩子添加到两个分裂的结点n和nn中，此时n和nn的孩子结点还为空
//				for(int i = 0; i < n.usedSpace; i ++)
//				{
//					n.children.add(this.children.get(index));
//				}
//
//				for(int i = 0; i < nn.usedSpace; i ++)
//				{
//					nn.children.add(this.children.get(index));
//				}

                //把两个分裂的结点n和nn添加到根节点
                newRoot.addData(n.getNodeRectangle());
                newRoot.addData(nn.getNodeRectangle());

                newRoot.children.add(n);
                newRoot.children.add(nn);

                //设置两个分裂的结点n和nn的父节点
                n.parent = newRoot;
                nn.parent = newRoot;

                //最后设置rtree的根节点
                rtree.setRoot(newRoot);//新加的
            }else {
                RTDirNode p = (RTDirNode) getParent();
                p.adjustTree(n, nn);
            }
        }
        return true;
    }

    /**
     * 非叶子结点的分裂
     *
     * @param node
     * @return
     */
    private RTDirNode[] splitIndex(RTNode node)
    {
        int[][] group = null;

        switch (rtree.getTreeType())
        {
            case Constants.RTREE_LINEAR:
                break;
            case Constants.RTREE_QUADRATIC:
                group = quadraticSplit(node.getNodeRectangle());
                children.add(node);//新加的
                node.parent = this;//新加的
                break;
            case Constants.RTREE_EXPONENTIAL:
                break;
            case Constants.RSTAR:
                break;
            default:
                throw new IllegalStateException("Invalid tree type.");
        }

        RTDirNode index1 = new RTDirNode(rtree, parent, level);
        RTDirNode index2 = new RTDirNode(rtree, parent, level);

        int[] group1 = group[0];
        int[] group2 = group[1];

        for(int i = 0; i < group1.length; i ++)
        {
            //为index1添加数据和孩子
            index1.addData(datas[group1[i]]);
            index1.children.add(this.children.get(group1[i]));//新加的
            //让index1成为其父节点
            this.children.get(group1[i]).parent = index1;//新加的
        }
        for(int i = 0; i < group2.length; i ++)
        {
            index2.addData(datas[group2[i]]);
            index2.children.add(this.children.get(group2[i]));//新加的
            this.children.get(group2[i]).parent = index2;//新加的
        }

        return new RTDirNode[]{index1,index2};
    }

    @Override
    protected RTDataNode findLeaf(Rectangle rectangle)
    {
        for(int i = 0; i < usedSpace; i ++)
        {
            if(datas[i].enclosure(rectangle))
            {
                deleteIndex = i;//记录搜索路径
                RTDataNode leaf = children.get(i).findLeaf(rectangle);
                if(leaf != null)
                    return leaf;
            }
        }
        return null;
    }

}
