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


        // redirct
        var redirct_url = base_url + "/heartbeat?querytime={querytime}&appname={appname}&ip={ip}";
        redirct_url = redirct_url.replace('{querytime}', querytime);
        redirct_url = redirct_url.replace('{appname}', appname);
        redirct_url = redirct_url.replace('{ip}', ip);

        window.location.href = redirct_url;
    });


    // chart
    var baaSample = $('#bar-sample').html();
    var barNo = 1;
    function makeBar(name, xData, yData){

        var barItemId = 'bar-item-'+(barNo++);
        var fullgcBar = baaSample.replace('{name}', name).replace('{id}', barItemId)

        $('#bar-parent').append(fullgcBar);

        var barOption = {
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
                type: 'category',
                data: xData,
            },
            yAxis: {
                type: 'value'
            },
            series: [{
                name:'count',
                data: yData,
                type: 'bar'
            }]
        };

        var barChart = echarts.init(document.getElementById(barItemId));
        barChart.setOption(barOption);
    }


    // test
    var arr = [];
    var arr2 = [];
    for (var i = 0; i < 60; i++) {
        arr[i] = i;
        arr2[i] = Math.floor((Math.random()*100)+1);
    }
    makeBar('FullGc Count', arr, arr2);
    makeBar('FullGc Count2', arr, arr2);

});
