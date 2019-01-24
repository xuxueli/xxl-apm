$(function() {

    // base
    $('.select2').select2({
        language:'zh-CN'
    });

    // querytime
    $.datetimepicker.setLocale('ch');
    $('#querytime').datetimepicker({
        format: 'Y-m-d H:i',
        step: 60,
        maxDate: 0  // 0 means today
    });

    // appname
    $( "#appname" ).autocomplete({
        source: function( request, response ) {
            $.ajax({
                url: base_url + "/event/findAppNameList",
                dataType: "json",
                data: {
                    "appname": request.term
                },
                success: function( data ) {
                    if(data.code == 200){
                        response(data.data);
                    }
                }
            });
        }
    });

    // search
    $('#searchBtn').click(function () {

        // querytime
        var querytime_input = $('#querytime').val();
        if (!querytime_input) {
            layer.open({
                title: '系统提示' ,
                btn: [ '确定' ],
                content: '请选择查询时间',
                icon: '2'
            });
            return;
        }

        var time = new Date(querytime_input)
        var y = time.getFullYear();
        var m = time.getMonth() + 1;
        if (m < 10) {
            m = "0" + m
        }
        var d = time.getDate();
        if (d < 10) {
            d = "0" + d
        }
        var h = time.getHours();
        if (h < 10) {
            h = "0" + h
        }
        var querytime = y + "" + m + "" + d + "" + h;
        var appname = $('#appname').val()?$('#appname').val().trim():'';
        var type = $('#type').val()?$('#type').val().trim():'';

        if (!appname) {
            layer.open({
                title: '系统提示' ,
                btn: [ '确定' ],
                content: '请输入应用 AppName',
                icon: '2'
            });
            return;
        }

        // redirct
        var redirct_url = base_url + "/event?querytime={querytime}&appname={appname}&type={type}";
        redirct_url = redirct_url.replace('{querytime}', querytime);
        redirct_url = redirct_url.replace('{appname}', appname);
        redirct_url = redirct_url.replace('{type}', type);

        window.location.href = redirct_url;
    });


    // valid data
    if (!eventReportList) {
        appendTips("暂无数据");
        return;
    }

    // tips
    function appendTips(title) {
        var tipsTmp = '<section class="content-header col-md-12"><h1>{title}<small></small></h1></section>'.replace('{title}', title)
        $('#bar-parent').append(tipsTmp);
    }
    // 四舍五入，4位小数
    function toDecimal(x) {
        var f = parseFloat(x);
        f = Math.round(x*10000)/10000;
        f = f.toFixed(4);
        return f;
    }

    // parse data
    var nameMapList = {};           // event item, for each name: { name: {...} }
    function getNameMap(name) {
        var nameMap = nameMapList[name+''];
        if (!nameMap) {
            nameMap = {};
            nameMap.Name = '';
            nameMap.Total = 0;
            nameMap.Failure = 0;
            nameMap.Failure_percent = 0;
            nameMap.QPS = 0;
            nameMap.Percent = 0;

            nameMapList[name+''] = nameMap;
        }
        return nameMap;
    }

    var all_Total = 0;
    var all_Failure = 0;
    for (var index in eventReportList) {
        var eventReport = eventReportList[index];

        // item
        var nameMap_item = getNameMap(eventReport.name);
        nameMap_item.Name += eventReport.name;
        nameMap_item.Total += eventReport.total_count;
        nameMap_item.Failure += eventReport.fail_count;

        // all
        all_Total += eventReport.total_count;
        all_Failure += eventReport.fail_count;
    }

    for(var key in nameMapList) {
        var nameMap = nameMapList[key];
        nameMap.Failure_percent = nameMap.Failure/nameMap.Total;
        nameMap.QPS = nameMap.Total/periodSecond;
        nameMap.Percent = nameMap.Total/all_Total;
        nameMap.LogView = '--';
        nameMap.Chart = '--';
    }

    var nameMapTotal = {};      // event all
    nameMapTotal.Name = '<span class="badge bg-gray">Total</span>';
    nameMapTotal.Total = all_Total;
    nameMapTotal.Failure = all_Failure;
    nameMapTotal.Failure_percent = all_Failure/all_Total;
    nameMapTotal.QPS = all_Total/periodSecond;
    nameMapTotal.Percent = all_Total/all_Total;
    nameMapTotal.LogView = '--';
    nameMapTotal.Chart = '--';


    // write table
    var TableData = [];
    function addTableData(nameMap){
        var nameMapArr = [];
        nameMapArr[0] = nameMap.Name;
        nameMapArr[1] = nameMap.Total;
        nameMapArr[2] = '<span style="color: '+ (nameMap.Failure>0?'red':'black') +';">'+ nameMap.Failure +'</span>';
        nameMapArr[3] = '<span style="color: '+ (nameMap.Failure_percent>0?'red':'black') +';">'+ toDecimal( nameMap.Failure_percent*100 ) +'%</span>';
        nameMapArr[4] = toDecimal( nameMap.QPS );
        nameMapArr[5] = nameMap.LogView;
        nameMapArr[6] = nameMap.Chart;
        nameMapArr[7] = toDecimal( nameMap.Percent*100 ) + '%';

        TableData.push(nameMapArr);
    }
    addTableData(nameMapTotal);
    for (var i in nameMapList) {
        addTableData(nameMapList[i]);
    }

    $('#event-table').dataTable( {
        "data": TableData,
        "paging": false,
        "searching": false,
        "order": [[ 1, 'desc' ]],
        "info": false
    } );

    // chart


});
