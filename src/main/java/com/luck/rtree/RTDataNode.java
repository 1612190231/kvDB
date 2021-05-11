package com.luck.rtree;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName RTDataNode
 * @Description
 */
public class RTDataNode extends RTNode
{

    public RTDataNode(RTree rTree, RTNode parent)
    {
        super(rTree, parent, 0);
    }

    /**
     * 在叶节点中插入Rectangle，插入后若是其父节点不为空则须要向上调整树直到根节点；<br>
     * 若插入Rectangle以后超过结点容量则须要分裂结点
     * @param rectangle
     * @return
     */
    public boolean insert(Rectangle rectangle)
    {
        if(usedSpace < rtree.getNodeCapacity())
        {
            datas[usedSpace ++] = rectangle;
            RTDirNode parent = (RTDirNode) getParent();

            if(parent != null)
                parent.adjustTree(this, null);
            return true;

        }else{//超过结点容量
            RTDataNode[] splitNodes = splitLeaf(rectangle);
            RTDataNode l = splitNodes[0];
            RTDataNode ll = splitNodes[1];

            if(isRoot())
            {
                //根节点已满，须要分裂。建立新的根节点
                RTDirNode rDirNode = new RTDirNode(rtree, Constants.NULL, level + 1);
                rtree.setRoot(rDirNode);
                rDirNode.addData(l.getNodeRectangle());
                rDirNode.addData(ll.getNodeRectangle());

                ll.parent = rDirNode;
                l.parent = rDirNode;

                rDirNode.children.add(l);
                rDirNode.children.add(ll);

            }else{//不是根节点
                RTDirNode parentNode = (RTDirNode) getParent();
                parentNode.adjustTree(l, ll);
            }


        }
        return true;
    }

    /**
     * 插入Rectangle以后超过容量须要分裂
     * @param rectangle
     * @return
     */
    public RTDataNode[] splitLeaf(Rectangle rectangle)
    {
        int[][] group = null;

        switch(rtree.getTreeType())
        {
            case Constants.RTREE_LINEAR:
                break;
            case Constants.RTREE_QUADRATIC:
                group = quadraticSplit(rectangle);
                break;
            case Constants.RTREE_EXPONENTIAL:
                break;
            case Constants.RSTAR:
                break;
            default:
                throw new IllegalArgumentException("Invalid tree type.");
        }

        RTDataNode l = new RTDataNode(rtree, parent);
        RTDataNode ll = new RTDataNode(rtree, parent);

        int[] group1 = group[0];
        int[] group2 = group[1];

        for(int i = 0; i < group1.length; i ++)
        {
            l.addData(datas[group1[i]]);
        }

        for(int i = 0; i < group2.length; i ++)
        {
            ll.addData(datas[group2[i]]);
        }
        return new RTDataNode[]{l, ll};
    }


    @Override
    public RTDataNode chooseLeaf(Rectangle rectangle)
    {
        insertIndex = usedSpace;//记录插入路径的索引
        return this;
    }

    /**
     * 从叶节点中删除此条目rectangle<p>
     * 先删除此rectangle，再调用condenseTree()返回删除结点的集合，把其中的叶子结点中的每一个条目从新插入；
     * 非叶子结点就今后结点开始遍历全部结点，而后把全部的叶子结点中的全部条目所有从新插入
     * @param rectangle
     * @return
     */
    protected int delete(Rectangle rectangle)
    {
        for(int i = 0; i < usedSpace; i ++)
        {
            if(datas[i].equals(rectangle))
            {
                deleteData(i);
                List<RTNode> deleteEntriesList = new ArrayList<RTNode>();
                condenseTree(deleteEntriesList);

                //从新插入删除结点中剩余的条目
                for(int j = 0; j < deleteEntriesList.size(); j ++)
                {
                    RTNode node = deleteEntriesList.get(j);
                    if(node.isLeaf())//叶子结点，直接把其上的数据从新插入
                    {
                        for(int k = 0; k < node.usedSpace; k ++)
                        {
                            rtree.insert(node.datas[k]);
                        }
                    }else{//非叶子结点，须要前后序遍历出其上的全部结点
                        List<RTNode> traverseNodes = rtree.traversePostOrder(node);

                        //把其中的叶子结点中的条目从新插入
                        for(int index = 0; index < traverseNodes.size(); index ++)
                        {
                            RTNode traverseNode = traverseNodes.get(index);
                            if(traverseNode.isLeaf())
                            {
                                for(int t = 0; t < traverseNode.usedSpace; t ++)
                                {
                                    rtree.insert(traverseNode.datas[t]);
                                }
                            }
                        }

                    }
                }

                return deleteIndex;
            }//end if
        }//end for
        return -1;
    }

    @Override
    protected RTDataNode findLeaf(Rectangle rectangle)
    {
        for(int i = 0; i < usedSpace; i ++)
        {
            if(datas[i].enclosure(rectangle))
            {
                deleteIndex = i;//记录搜索路径
                return this;
            }
        }
        return null;
    }

}
