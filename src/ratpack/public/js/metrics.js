var timerChartsDataArray = [[],[],[]];

function updateRequestCountChart(data) {
    var requestCount = [];
    requestCount.push(['Url', 'Request Count']);

    $.each(data.timers, function (index, value) {
        requestCount.push([value.name, value.count])
    });

    var chartData = google.visualization.arrayToDataTable(requestCount);
    var options = {
        title: 'Request Count',
        pieHole: 0.4,
        chartArea: {left:0, width:"100%"}
    };
    var chart = new google.visualization.PieChart(document.getElementById('requestCountChart'));
    chart.draw(chartData, options);
}

function getTimerDiv(timerName) {
    var timerId = timerName.replace(/[-!$%^&*()_+|~=`{}\[\]:";'<>?,.\/]/g,'');

    if ($("#" + timerId).length == 0) {
        var newRow = '<h3>' + timerName + '</h3><div class="row" id="' + timerId + '"><div class="col-md-6" style="height: 300px;"></div><div class="col-md-6" style="height: 300px;"></div></div>';
        $('#timerCharts').prepend(newRow);
    }

    return $("#" + timerId);
}

function getTimerArrayPosition(timerName) {
    var timerArrayPosition = $.inArray(timerName, timerChartsDataArray[0]);

    if (timerArrayPosition < 0) {
        timerArrayPosition = timerChartsDataArray[0].length;
        timerChartsDataArray[0][timerArrayPosition] = timerName;

        var chartData1 = new google.visualization.DataTable();
        chartData1.addColumn('date', 'Time');
        chartData1.addColumn('number', 'Rate');
        timerChartsDataArray[1][timerArrayPosition] = chartData1;

        var chartData2 = new google.visualization.DataTable();
        chartData2.addColumn('date', 'Time');
        chartData2.addColumn('number', '95thPercentile');
        chartData2.addColumn({id:'50thPercentile', type:'number', role:'interval'});
        chartData2.addColumn({id:'75thPercentile', type:'number', role:'interval'});
        chartData2.addColumn({id:'99thPercentile', type:'number', role:'interval'});
        chartData2.addColumn({id:'999thPercentile', type:'number', role:'interval'});
        timerChartsDataArray[2][timerArrayPosition] = chartData2;
    }

    return timerArrayPosition
}

function updateTimerCharts(data) {
    var snapshotTime = new Date(data.timestamp);

    $.each(data.timers, function (index, value) {
        var timerDiv = getTimerDiv(value.name);
        var timerArrayPosition = getTimerArrayPosition(value.name);

        // Request per min chart
        var chartData1 = timerChartsDataArray[1][timerArrayPosition];
        chartData1.addRow([snapshotTime,  value.oneMinuteRate]);
        if (chartData1.getNumberOfRows() > 30) {
            chartData1.removeRow(0);
        }

        var options = {
            title: 'Requests per Minute',
            legend: { position: 'none' },
            hAxis: { slantedText: true, textPosition: 'none' },
            vAxis: { viewWindow: {min: 0} },
            chartArea: {left:50, width:'100%'}
        };

        var chart1 = new google.visualization.LineChart(timerDiv.children().eq(0).get(0));
        chart1.draw(chartData1, options);


        // Response time chart
        var chartData2 = timerChartsDataArray[2][timerArrayPosition];
        chartData2.addRow([snapshotTime, value['95thPercentile'], value['50thPercentile'], value['75thPercentile'], value['99thPercentile'], value['999thPercentile']]);
        if (chartData2.getNumberOfRows() > 15) {
            chartData2.removeRow(0);
        }

        var options = {
            title: 'Response Time Percentiles (ms)',
            legend: { position: 'none' },
            curveType:'function',
            series: [{'color': '#F1CA3A'}],
            intervals: { 'style':'area' },
            hAxis: { slantedText: true, textPosition: 'none' },
            vAxis: { viewWindow: {min: 0} },
            chartArea: {left:50, width:'100%'}
        };

        var chart2 = new google.visualization.LineChart(timerDiv.children().eq(1).get(0));
        chart2.draw(chartData2, options);
    });
}

function updateCharts(data) {
    $("#noData").hide();

    var obj = jQuery.parseJSON(data);
    updateRequestCountChart(obj);
    updateTimerCharts(obj);
}

function connectWs() {
    if (!window.ws || window.ws.readyState != WebSocket.OPEN) {
        window.ws = new WebSocket("ws://"+location.host+"/admin/metrics-report");

        window.ws.onopen = function(event) {
            console.log("WebSocket opened");
        };

        window.ws.onmessage = function(event) {
            console.log("WebSocket message received");
            updateCharts(event.data);
        };

        window.ws.onclose = function(event) {
            var timer = setTimeout(function() {
                console.log("Retrying connection...");
                connectWs();
                if (window.ws.readyState == WebSocket.OPEN) {
                    clearTimeout(timer);
                }
            }, 1000);
        };
    }
}

$(document).ready(function() {
    if (!window.WebSocket) {
        alert("This dashboard will not work in your browser, a browser that supports websockets is required.");
    } else {
        console.log("Opening WebSocket");
        connectWs();
    }
});