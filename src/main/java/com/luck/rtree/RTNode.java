package com.luck.rtree;

import java.util.List;

import com.luck.rtree.Constants;

/**
 * @ClassName RTNode
 * @Description
 */
public abstract class RTNode
{
    /**
     * 结点所在的树
     */
    protected RTree rtree;

    /**
     * 结点所在的层
     */
    protected int level;

    /**
     * 至关于条目
     */
    protected Rectangle[] datas;

//	/**
//	 * 结点的容量
//	 */
//	protected int capacity;

    /**
     * 父节点
     */
    protected RTNode parent;

    /**
     * 结点已用的空间
     */
    protected int usedSpace;

    /**
     * 记录插入的搜索路径索引
     */
    protected int insertIndex;

    /**
     * 记录删除的查找路径索引
     */
    protected int deleteIndex;

    public RTNode(RTree rtree, RTNode parent, int level)
    {
        this.rtree = rtree;
        this.parent = parent;
        this.level = level;
//		this.capacity = capacity;
        datas = new Rectangle[rtree.getNodeCapacity() + 1];//多出的一个用于结点分裂
        usedSpace = 0;
    }

    /**
     * @return 返回父节点
     */
    public RTNode getParent()
    {
        return parent;
    }

//	/**
//	 * @return 结点容量
//	 */
//	public int getNodeCapacity()
//	{
//		return capacity;
//	}

    /**
     * 向结点中添加Rectangle，即添加条目
     * @param rectangle
     */
    protected void addData(Rectangle rectangle)
    {
        if(usedSpace == rtree.getNodeCapacity())
        {
            throw new IllegalArgumentException("Node is full.");
        }
        datas[usedSpace ++] = rectangle;
    }

    /**
     * 删除结点中的第i个条目
     * @param i
     */
    protected void deleteData(int i)
    {
        if(datas[i + 1] != null)
        {
            System.arraycopy(datas, i + 1, datas, i, usedSpace - i -1);
            datas[usedSpace - 1] = null;
        }
        else
            datas[i] = null;
        usedSpace --;
    }

    /**
     * 叶节点L中刚刚删除了一个条目，若是这个结点的条目数太少下溢，则删除该结点，同时将该结点中剩余的条目重定位到其余结点中。
     * 若是有必要，要逐级向上进行这种删除，调整向上传递的路径上的全部外包矩形，使其尽量小，直到根节点。
     * @param list 存储删除结点中剩余条目
     */
    protected void condenseTree(List<RTNode> list)
    {
        if(isRoot())
        {
            //根节点只有一个条目了，即只有左孩子或者右孩子
            if(! isLeaf() && usedSpace == 1)
            {
                RTDirNode root = (RTDirNode) this;

                RTNode child = root.getChild(0);
                root.children.remove(this);//gc
                child.parent = null;
//				if(child.level > 0)
//					child.level --;
                rtree.setRoot(child);

//				//全部结点的level减1，即树下降一层
//				List<RTNode> nodes = rtree.traversePostOrder(child);
//				for(int i = 0; i < nodes.size(); i ++)
//				{
//					nodes.get(i).level -= 1;
//				}

            }
        }else{
            RTNode parent = getParent();

            int min = Math.round(rtree.getNodeCapacity() * rtree.getFillFactor());
            if(usedSpace < min)
            {
                parent.deleteData(parent.deleteIndex);//其父节点中删除此条目
                ((RTDirNode)parent).children.remove(this);
                this.parent = null;
                list.add(this);//以前已经把数据删除了
            }else{
                parent.datas[parent.deleteIndex] = getNodeRectangle();
            }
            parent.condenseTree(list);
        }
    }

    /**
     * 分裂结点的平方算法<p>
     * 一、为两个组选择第一个条目--调用算法pickSeeds()来为两个组选择第一个元素，分别把选中的两个条目分配到两个组当中。<br>
     * 二、检查是否已经分配完毕，若是一个组中的条目太少，为避免下溢，将剩余的全部条目所有分配到这个组中，算法终止<br>
     * 三、调用pickNext来选择下一个进行分配的条目--计算把每一个条目加入每一个组以后面积的增量，选择两个组面积增量差最大的条目索引,
     * 	若是面积增量相等则选择面积较小的组，若面积也相等则选择条目数更少的组<br>
     * @param rectangle 致使分裂的溢出Rectangle
     * @return 两个组中的条目的索引
     */
    protected int[][] quadraticSplit(Rectangle rectangle)
    {
        if(rectangle == null)
        {
            throw new IllegalArgumentException("Rectangle cannot be null.");
        }

        datas[usedSpace] = rectangle;	//先添加进去
//		if(this instanceof RTDirNode)
//		{
//			(RTDirNode)(this).children.add()
//		}
        int total = usedSpace + 1;		//结点总数

        //标记访问的条目
        int[] mask = new int[total];
        for(int i = 0; i < total; i ++)
        {
            mask[i] = 1;
        }

        //每一个组只是有total/2个条目
        int c = total/2 + 1;
        //每一个结点最小条目个数
        int minNodeSize = Math.round(rtree.getNodeCapacity() * rtree.getFillFactor());
        //至少有两个
        if(minNodeSize < 2)
            minNodeSize = 2;

        //记录没有被检查的条目的个数
        int rem = total;

        int[] group1 = new int[c];//记录分配的条目的索引
        int[] group2 = new int[c];//记录分配的条目的索引
        //跟踪被插入每一个组的条目的索引
        int i1 = 0, i2 = 0;

        int[] seed = pickSeeds();
        group1[i1 ++] = seed[0];
        group2[i2 ++] = seed[1];
        rem -=2;
        mask[group1[0]] = -1;
        mask[group2[0]] = -1;

        while(rem > 0)
        {
            //将剩余的全部条目所有分配到group1组中，算法终止
            if(minNodeSize - i1 == rem)
            {
                for(int i = 0; i < total; i ++)//总共rem个
                {
                    if(mask[i] != -1)//尚未被分配
                    {
                        group1[i1 ++] = i;
                        mask[i] = -1;
                        rem --;
                    }
                }
                //将剩余的全部条目所有分配到group1组中，算法终止
            }else if(minNodeSize - i2 == rem)
            {
                for(int i = 0; i < total; i ++)//总共rem个
                {
                    if(mask[i] != -1)//尚未被分配
                    {
                        group2[i2 ++] = i;
                        mask[i] = -1;
                        rem --;
                    }
                }
            }else
            {
                //求group1中全部条目的最小外包矩形
                Rectangle mbr1 = (Rectangle) datas[group1[0]].clone();
                for(int i = 1; i < i1; i ++)
                {
                    mbr1 = mbr1.getUnionRectangle(datas[group1[i]]);
                }
                //求group2中全部条目的外包矩形
                Rectangle mbr2 = (Rectangle) datas[group2[0]].clone();
                for(int i = 1; i < i2; i ++)
                {
                    mbr2 = mbr2.getUnionRectangle(datas[group2[i]]);
                }

                //找出下一个进行分配的条目
                double dif = Double.NEGATIVE_INFINITY;
                double areaDiff1 = 0, areaDiff2 = 0;
                int sel = -1;
                for(int i = 0; i < total; i ++)
                {
                    if(mask[i] != -1)//尚未被分配的条目
                    {
                        //计算把每一个条目加入每一个组以后面积的增量，选择两个组面积增量差最大的条目索引
                        Rectangle a = mbr1.getUnionRectangle(datas[i]);
                        areaDiff1 = a.getArea() - mbr1.getArea();

                        Rectangle b = mbr2.getUnionRectangle(datas[i]);
                        areaDiff2 = b.getArea() - mbr2.getArea();

                        if(Math.abs(areaDiff1 - areaDiff2) > dif)
                        {
                            dif = Math.abs(areaDiff1 - areaDiff2);
                            sel = i;
                        }
                    }
                }

                if(areaDiff1 < areaDiff2)//先比较面积增量
                {
                    group1[i1 ++] = sel;
                }else if(areaDiff1 > areaDiff2)
                {
                    group2[i2 ++] = sel;
                }else if(mbr1.getArea() < mbr2.getArea())//再比较自身面积
                {
                    group1[i1 ++] = sel;
                }else if(mbr1.getArea() > mbr2.getArea())
                {
                    group2[i2 ++] = sel;
                }else if(i1 < i2)//最后比较条目个数
                {
                    group1[i1 ++] = sel;
                }else if(i1 > i2)
                {
                    group2[i2 ++] = sel;
                }else {
                    group1[i1 ++] = sel;
                }
                mask[sel] = -1;
                rem --;

            }
        }//end while


        int[][] ret = new int[2][];
        ret[0] = new int[i1];
        ret[1] = new int[i2];

        for(int i = 0; i < i1; i ++)
        {
            ret[0][i] = group1[i];
        }
        for(int i = 0; i < i2; i ++)
        {
            ret[1][i] = group2[i];
        }
        return ret;
    }


    /**
     * 一、对每一对条目E1和E2，计算包围它们的Rectangle J，计算d = area(J) - area(E1) - area(E2);<br>
     * 二、Choose the pair with the largest d
     * @return 返回两个条目若是放在一块儿会有最多的冗余空间的条目索引
     */
    protected int[] pickSeeds()
    {
        double inefficiency = Double.NEGATIVE_INFINITY;
        int i1 = 0, i2 = 0;

        //
        for(int i = 0; i < usedSpace; i ++)
        {
            for(int j = i + 1; j <= usedSpace; j ++)//注意此处的j值
            {
                Rectangle rectangle = datas[i].getUnionRectangle(datas[j]);
                double d = rectangle.getArea() - datas[i].getArea() - datas[j].getArea();

                if(d > inefficiency)
                {
                    inefficiency = d;
                    i1 = i;
                    i2 = j;
                }
            }
        }
        return new int[]{i1, i2};
    }

    /**
     * @return 返回包含结点中全部条目的最小Rectangle
     */
    public Rectangle getNodeRectangle()
    {
        if(usedSpace > 0)
        {
            Rectangle[] rectangles = new Rectangle[usedSpace];
            System.arraycopy(datas, 0, rectangles, 0, usedSpace);
            return Rectangle.getUnionRectangle(rectangles);
        }else {
            return new Rectangle(new Point(new float[]{0,0}), new Point(new float[]{0,0}));
        }
    }

    /**
     * @return 是否根节点
     */
    public boolean isRoot()
    {
        return (parent == Constants.NULL);
    }

    /**
     * @return 是否非叶子结点
     */
    public boolean isIndex()
    {
        return (level != 0);
    }

    /**
     * @return 是否叶子结点
     */
    public boolean isLeaf()
    {
        return (level == 0);
    }


    /**
     * <b>步骤CL1：</b>初始化——记R树的根节点为N。<br>
     * <b>步骤CL2：</b>检查叶节点——若是N是个叶节点，返回N<br>
     * <b>步骤CL3：</b>选择子树——若是N不是叶节点，则从N中全部的条目中选出一个最佳的条目F，
     *		选择的标准是：若是E加入F后，F的外廓矩形FI扩张最小，则F就是最佳的条目。若是有两个
     *		条目在加入E后外廓矩形的扩张程度相等，则在这二者中选择外廓矩形较小的那个。<br>
     * <b>步骤CL4：</b>向下寻找直至达到叶节点——记Fp指向的孩子节点为N，而后返回步骤CL2循环运算，
     * 		直至查找到叶节点。<p>
     * @param Rectangle
     * @return RTDataNode
     */
    protected abstract RTDataNode chooseLeaf(Rectangle rectangle);

    /**
     * R树的根节点为T，查找包含rectangle的叶子结点<p>
     * 一、若是T不是叶子结点，则逐个查找T中的每一个条目是否包围rectangle，若包围则递归调用findLeaf()<br>
     * 二、若是T是一个叶子结点，则逐个检查T中的每一个条目可否匹配rectangle<br>
     * @param rectangle
     * @return 返回包含rectangle的叶节点
     */
    protected abstract RTDataNode findLeaf(Rectangle rectangle);

}
