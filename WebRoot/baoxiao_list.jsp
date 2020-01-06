<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"  prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions"  prefix="fn"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>报销管理</title>

    <!-- Bootstrap -->
    <link href="bootstrap/css/bootstrap.css" rel="stylesheet">
    <link href="css/content.css" rel="stylesheet">

    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
    <script src="http://cdn.bootcss.com/html5shiv/3.7.2/html5shiv.min.js"></script>
    <script src="http://cdn.bootcss.com/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->
    <script src="js/jquery.min.js"></script>
    <script src="bootstrap/js/bootstrap.min.js"></script>
</head>
<body>

<!--路径导航-->
<ol class="breadcrumb breadcrumb_nav">
    <li>首页</li>
    <li>报销管理</li>
    <li class="active">我的报销单</li>
</ol>
<!--路径导航-->

<div class="page-content">    
   <form class="form-inline">
        <div class="panel panel-default">
            <div class="panel-heading">报销单列表</div>
            
            <div class="table-responsive">
                <table class="table table-striped table-hover">
                    <thead>
                    <tr>
                        <th width="8%">ID</th>
                        <th width="10%">报销金额</th>
                        <th width="10%">标题</th>
                        <th width="12%">备注</th>
                        <th width="12%">时间</th>
                        <th width="10%">状态</th>
                        <th width="15%">操作</th>
                    </tr>
                    </thead>
                    <tbody>
					<c:forEach var="bx" items="${baoxiaoList}">
	                    <tr>
	                        <td>${bx.id}</td>
	                        <td>${bx.money}</td>
	                        <td>${bx.title}</td>
	                        <td>${bx.remark}</td>
	                        <td>
	                        <fmt:formatDate value="${bx.creatdate}" pattern="yyyy-MM-dd HH:mm:ss"/>
	                        </td>
	                        <c:if test="${bx.state==1}">
	                        <td>审核中</td>
	                        <td>
	                          <a href="viewHisComment?billId=${bx.id}" class="btn btn-success btn-xs"><span class="glyphicon glyphicon-eye-open"></span> 查看审核记录</a>
	                          <a target="_blank" href="viewCurrentImageByBill?billId=${bx.id}" class="btn btn-success btn-xs"><span class="glyphicon glyphicon-eye-open"></span> 查看当前流程图</a>
	                        </td>
	                        </c:if>
	                        <c:if test="${bx.state==2}">
	                        <td>审核完成</td>
	                        <td>
	                          <a href="delBill?billId=${bx.id}" class="btn btn-danger btn-xs"><span class="glyphicon glyphicon-remove"></span> 删除</a>
	                          <a href="viewHisComment?billId=${bx.id}" class="btn btn-success btn-xs"><span class="glyphicon glyphicon-eye-open"></span> 查看审核记录</a>
	                        </td>
	                        </c:if>
	                    </tr>
					</c:forEach>
                    </tbody>
                </table>
            </div>
        <p></p>  
    	<p>
		    <!-- 【当前第${page.pageNum}页，总共${page.total}条记录，总共${page.pages}页】 -->
		    &nbsp;&nbsp;&nbsp;
			<a href="myBaoxiaoBill?pageNum=1" class="btn btn-default">首页</a>
			<a href="myBaoxiaoBill?pageNum=${page.prePage}" class="btn btn-default">上一页</a>
			<a href="myBaoxiaoBill?pageNum=${page.nextPage}" class="btn btn-default">下一页</a>
			<a href="myBaoxiaoBill?pageNum=${page.pages}" class="btn btn-default">尾页</a>
		</p>
        </div>
    </form>
	
</div>
		
</body>
</html>