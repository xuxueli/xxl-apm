$(function() {

    // base
    $('.select2').select2();

    // filter
    $('#querytime').datetimepicker({
        format: 'Y-m-d H:i',
        lang:"ch"
    });

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
        var ip = $('#ip').val()?$('#ip').val().trim():'';

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
        var redirct_url = base_url + "/heartbeat?querytime={querytime}&appname={appname}&ip={ip}";
        redirct_url = redirct_url.replace('{querytime}', querytime);
        redirct_url = redirct_url.replace('{appname}', appname);
        redirct_url = redirct_url.replace('{ip}', ip);

        window.location.href = redirct_url;
    });

    // valid heartbeat data
    if (!heartbeatList) {
        layer.open({
            title: '系统提示' ,
            btn: [ '确定' ],
            content: '暂无数据',
            icon: '0'
        });
        return;
    }

    // heartbeat chart tool
    var baaSample = $('#bar-sample').html();
    var barNo = 1;
    function makeBar(name, xData, yData, yDataUnit){

        var barItemId = 'bar-item-'+(barNo++);
        var fullgcBar = baaSample.replace('{id}', barItemId)

        $('#bar-parent').append(fullgcBar);

        var barOption = {
            title: {
                text: name
            },
            toolbox: {
                show : true,
                feature : {
                    dataView : {show: true, readOnly: false},
                    magicType : {show: true, type: ['line', 'bar']},
                    restore : {show: true},
                    saveAsImage : {show: true}
                }
            },
            tooltip : {
                trigger: 'axis'
            },
            xAxis: {
                name: 'Min',
                data: xData,
                type: 'category'
            },
            yAxis: {
                type: 'value'
            },
            series: [{
                name: yDataUnit,
                data: yData,
                type: 'bar'
            }]
        };

        var barChart = echarts.init(document.getElementById(barItemId));
        barChart.setOption(barOption);
    }

    // 四舍五入，两位小数
    function toDecimal(x) {
        var f = parseFloat(x);
        f = Math.round(x*100)/100;
        return f;
    }

    // heartbeat chat
    var xData = [];
    var yData = {};

    var kb_mb = 1024;
    function setYData(level_1_obj, level_2_arr, index, indexData){
        if (!yData[level_1_obj]){
            yData[level_1_obj] = {};
        }
        if (!yData[level_1_obj+''][level_2_arr+'']) {
            yData[level_1_obj+''][level_2_arr+''] = [];
        }

        yData[level_1_obj+''][level_2_arr+''][index] = indexData;
    }

    for (var index in heartbeatList) {
        var heartbeat = heartbeatList[index];

        // x-data, ms -> min
        xData[index] = (heartbeat.addtime/(1000*60))%60;

        // memory, km -> mb
        setYData('heap_all', 'used_space', index, toDecimal( heartbeat.heap_all.used_space/kb_mb ) );
        setYData('heap_all', 'used_percent', index, toDecimal( heartbeat.heap_all.used_space*100/heartbeat.heap_all.max_space ) );
        setYData('heap_eden_space', 'used_space', index, toDecimal( heartbeat.heap_eden_space.used_space/kb_mb ) );
        setYData('heap_eden_space', 'used_percent', index, toDecimal( heartbeat.heap_eden_space.used_space*100/heartbeat.heap_eden_space.max_space ) );
    }

    makeBar('heap_all(used_space)', xData, yData.heap_all.used_space, "MB");
    makeBar('heap_all(used_percent)', xData, yData.heap_all.used_percent, "%");

    makeBar('heap_eden_space(used_space)', xData, yData.heap_eden_space.used_space, "MB");
    makeBar('heap_eden_space(used_percent)', xData, yData.heap_eden_space.used_percent, "%");


});
