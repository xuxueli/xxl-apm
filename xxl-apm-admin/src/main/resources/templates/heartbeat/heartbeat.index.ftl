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

		<!-- Main content -->
	    <section class="content">
	    
	    	<div class="row">

                <div class="col-xs-3">
                        <input type="text" class="form-control" id="topic" autocomplete="on" placeholder="${.now}" > ~
                        <input type="text" class="form-control" id="topic" autocomplete="on" placeholder="${.now}" >
                </div>

                <div class="col-xs-3">
                    <input type="text" class="form-control" id="topic" autocomplete="on" placeholder="请输入应用 AppName" >
                </div>

                <div class="col-xs-3">
                    <div class="input-group">
                        <span class="input-group-addon">请选择机器</span>
                        <select class="form-control" id="bizId" >
                            <option value="">全部</option>
                            <option value="127.0.0.1（local.machaing）">无</option>
                        </select>
                    </div>
                </div>

	            <div class="col-xs-2">
	            	<button class="btn btn-block btn-info" id="searchBtn">GO</button>
	            </div>
          	</div>
            <br>
	    	
			<div class="row">

				<div class="col-md-6 col-xs-12-">
					<div class="box">


                        <!-- BAR CHART -->
                        <div class="box box-success">
                            <div class="box-header with-border">
                                <h3 class="box-title">Bar Chart</h3>
                                <div class="box-tools pull-right">
                                    <button type="button" class="btn btn-box-tool" data-widget="collapse"><i class="fa fa-minus"></i>
                                    </button>
                                    <button type="button" class="btn btn-box-tool" data-widget="remove"><i class="fa fa-times"></i></button>
                                </div>
                            </div>
                            <div class="box-body chart-responsive">
                                <div class="chart" id="bar-chart" style="height: 300px;"></div>
                            </div>
                            <!-- /.box-body -->
                        </div>
                        <!-- /.box -->


					</div>
				</div>




			</div>
			
	    </section>
	</div>


	<!-- footer -->
	<@netCommon.commonFooter />
</div>

<@netCommon.commonScript />
<#-- echarts -->
<script src="${request.contextPath}/static/plugins/echarts/echarts.common.min.js"></script>

<script>
</script>
<script src="${request.contextPath}/static/js/heartbeat.index.js"></script>

</body>
</html>
