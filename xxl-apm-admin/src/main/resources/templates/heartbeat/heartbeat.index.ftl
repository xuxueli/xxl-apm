<!DOCTYPE html>
<html>
<head>
  	<#import "../common/common.macro.ftl" as netCommon>
    <title>应用性能管理平台</title>
	<@netCommon.commonStyle />
    <!-- DataTables -->
    <link rel="stylesheet" href="${request.contextPath}/static/adminlte/bower_components/datatables.net-bs/css/dataTables.bootstrap.min.css">
</head>
<body class="hold-transition skin-blue sidebar-mini <#if cookieMap?exists && cookieMap["xxlapm_adminlte_settings"]?exists && "off" == cookieMap["xxlapm_adminlte_settings"].value >sidebar-collapse</#if>">
<div class="wrapper">
	<!-- header -->
	<@netCommon.commonHeader />
	<!-- left -->
	<@netCommon.commonLeft "heartbeat" />
	
	<!-- Content Wrapper. Contains page content -->
	<div class="content-wrapper">
		<!-- Content Header (Page header) -->
		<section class="content-header">
			<h1>消息主题管理<small></small></h1>
		</section>
		
		<!-- Main content -->
	    <section class="content">
	    
	    	<div class="row">
                <div class="col-xs-4">
                    <div class="input-group">
                        <span class="input-group-addon">业务线</span>
                        <select class="form-control" id="bizId" >
                            <option value="-1">全部</option>
                            <option value="0">无</option>
                        </select>
                    </div>
                </div>
                <div class="col-xs-4">
                    <div class="input-group">
                        <span class="input-group-addon">消息主题</span>
                        <input type="text" class="form-control" id="topic" autocomplete="on" placeholder="请输入消息主题，模糊匹配" >
                    </div>
                </div>
	            <div class="col-xs-2">
	            	<button class="btn btn-block btn-info" id="searchBtn">搜索</button>
	            </div>
                <div class="col-xs-2">
                    <button class="btn btn-block btn-default" id="topic_add">+新增消息主题</button>
                </div>
          	</div>
	    	
			<div class="row">
				<div class="col-xs-12">
					<div class="box">
			            <#--<div class="box-header">
			            	<h3 class="box-title">消息主题列表</h3>
			            </div>-->
			            <div class="box-body">
			              	<table id="data_list" class="table table-bordered table-striped" width="100%" >
				                <thead>
					            	<tr>
					                	<th name="bizId" >业务线</th>
					                  	<th name="topic" >消息主题</th>
                                        <th name="author" >负责人</th>
                                        <th name="alarmEmails" >告警邮箱</th>
					                  	<th>操作</th>
					                </tr>
				                </thead>
				                <tbody></tbody>
				                <tfoot></tfoot>
							</table>
						</div>
					</div>
				</div>
			</div>
			
	    </section>
	</div>


	<!-- footer -->
	<@netCommon.commonFooter />
</div>

<@netCommon.commonScript />
<!-- DataTables -->
<script src="${request.contextPath}/static/adminlte/bower_components/datatables.net/js/jquery.dataTables.min.js"></script>
<script src="${request.contextPath}/static/adminlte/bower_components/datatables.net-bs/js/dataTables.bootstrap.min.js"></script>
<script src="${request.contextPath}/static/plugins/jquery/jquery.validate.min.js"></script>

<script>
    var bizListObj = {};
    bizListObj['0'] = '无';
</script>
<script src="${request.contextPath}/static/js/heartbeat.index.js"></script>

</body>
</html>
