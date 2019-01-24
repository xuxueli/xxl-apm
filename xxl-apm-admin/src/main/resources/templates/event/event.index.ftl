<!DOCTYPE html>
<html>
<head>
  	<#import "../common/common.macro.ftl" as netCommon>
    <title>应用性能管理平台</title>

    <#-- select2 -->
    <link rel="stylesheet" href="${request.contextPath}/static/adminlte/bower_components/select2/css/select2.min.css">
    <@netCommon.commonStyle />
    <#-- datetimepicker -->
    <link rel="stylesheet" href="${request.contextPath}/static/plugins/datetimepicker/jquery.datetimepicker.min.css">
    <#-- jquery-ui -->
    <link rel="stylesheet" href="${request.contextPath}/static/plugins/jquery-ui/jquery-ui.min.css">

</head>
<body class="hold-transition skin-blue sidebar-mini <#if cookieMap?exists && cookieMap["xxlapm_adminlte_settings"]?exists && "off" == cookieMap["xxlapm_adminlte_settings"].value >sidebar-collapse</#if>">
<div class="wrapper">
	<!-- header -->
	<@netCommon.commonHeader />
	<!-- left -->
	<@netCommon.commonLeft "event" />
	
	<!-- Content Wrapper. Contains page content -->
	<div class="content-wrapper">

		<!-- Main content -->
	    <section class="content">
	    
	    	<div class="row">

                <div class="col-xs-3">
                    <div class="input-group">
                        <span class="input-group-addon">时间</span>
                        <input type="text" class="form-control" id="querytime" value="${querytime?string('yyyy-MM-dd HH:mm')}" readonly >
                    </div>
                </div>

                <div class="col-xs-4">
                    <div class="input-group">
                        <span class="input-group-addon">AppName</span>
                        <input type="text" class="form-control" id="appname" autocomplete="on" placeholder="请输入应用 AppName" value="${appname!''}" maxlength="100" >
                    </div>
                </div>

                <div class="col-xs-4">
                    <div class="input-group">
                        <span class="input-group-addon">Type</span>
                        <select class="form-control select2" style2="width: 100%;" id="type" >
                            <#if typeList?exists >
                                <#list typeList as item>
                                    <option value="${item}" <#if type?exists && type==item>selected="selected"</#if> >${item}</option>
                                </#list>
                            </#if>
                        </select>
                    </div>
                </div>

	            <div class="col-xs-1">
	            	<button class="btn btn-block btn-info" id="searchBtn">GO</button>
	            </div>
          	</div>

			<div class="row">

                <div id="bar-sample" style="display: none;" >
                    <div class="col-md-6 col-xs-12-" >
                        <div class="box box-success">
                            <div class="box-body chart-responsive">
                                <div class="chart bar-item" id="{id}" style="height: 300px;" ></div>
                            </div>
                        </div>
                    </div>
                </div>

				<div id="bar-parent" >
				</div>

                <br>
                <div class="col-md-12 col-xs-12">
                    <div class="box ">
                        <div class="box-body no-padding">
                            <table class="table table-striped">
                                <tr>
                                    <th style="width: 10px">Name</th>
                                    <th>Total</th>
                                    <th>Failure</th>
                                    <th>Failure%</th>
                                    <th>QPS</th>
                                    <th>Percent%</th>
                                    <th>LogView</th>
                                    <th style="width: 40px">Chart</th>
                                </tr>
                                <tr>
                                    <td><b>Total</b></td>
                                    <td>2000</td>
                                    <td>5</td>
                                    <td><span class="badge bg-green">0%</span></td>
                                    <td>26.2</td>
                                    <td>20%</td>
                                    <td>--</td>
                                    <td>Show</td>
                                </tr>
                                <tr>
                                    <td>/user/add</td>
                                    <td>2000</td>
                                    <td>5</td>
                                    <td><span class="badge bg-green">0%</span></td>
                                    <td>26.2</td>
                                    <td>20%</td>
                                    <td>--</td>
                                    <td>Show</td>
                                </tr>
                                <tr>
                                    <td>/user/query</td>
                                    <td>50000</td>
                                    <td>10</td>
                                    <td><span class="badge bg-red">0.3%</span></td>
                                    <td>155.2</td>
                                    <td>80%</td>
                                    <td>--</td>
                                    <td>Show</td>
                                </tr>
                            </table>
                        </div>
                        <!-- /.box-body -->
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
<#-- datetimepicker -->
<script src="${request.contextPath}/static/plugins/datetimepicker/jquery.datetimepicker.full.min.js"></script>
<#-- select2 -->
<script src="${request.contextPath}/static/adminlte/bower_components/select2/js/select2.full.min.js"></script>
<script src="${request.contextPath}/static/adminlte/bower_components/select2/js/i18n/zh-CN.js"></script>
<#-- jquery-ui -->
<script src="${request.contextPath}/static/plugins/jquery-ui/jquery-ui.min.js"></script>

<script>
    var eventReportList;
    <#if eventReportList?exists>
        eventReportList = JSON.parse('${eventReportList}');
    </#if>

</script>
<script src="${request.contextPath}/static/js/event.index.js"></script>

</body>
</html>
