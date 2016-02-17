var boxPlot = (function() {
  var significanceValues = [];
  var categoryNames = [];
  var chartContainer = "";
  var cutofflabel = "";
  var labelName = "";
  var addPlotLine = function(value) {
    chartContainer.yAxis[0].removePlotLine('plot-line');
    chartContainer.yAxis[0].addPlotLine({
      value: value,
      color: "#0C4F8B",
      width: 3,
      id: 'plot-line',
    });
    cutofflabel.attr({
      text: labelName + value.toFixed(2)
    });
  };

  var addCrossCancerEvents = function() {
    (function(H) {
      Highcharts.Chart.prototype.callbacks.push(function(chart) {

        H.addEvent(chart, 'click', function(e) {
          var flag = e.target.textContent != "";
          if (!flag) {
		  chart.showLoading();
		 
          //  var startTime = new Date();
            //chart.showLoading("Loading...");
            var cutoff = e.yAxis[0].value;
            addPlotLine(e.yAxis[0].value);
            //chart.yAxis[0].plotLinesAndBands[0].label.toFront();
            if (chart.get('significanceAxis') != null) {
              significanceValues = [];
              $.each(chart.series, function(key, val) {
                if ((val.visible == true) && (val.type == 'boxplot')) {
                  var count = 0;
                  $.each(val.linkedSeries[0].yData, function(key1, val1) {
                    if (val1 >= cutoff) count++;
                  });
                  significanceValues.push(((count / val.linkedSeries[0].yData.length) * 100).toFixed(2));
                }
              });
              chart.get('significanceAxis').setCategories(significanceValues);
            }

           // console.log("execution time : " + ((new Date()).getTime() - startTime));
            chart.hideLoading();
          }
        });
      });
    }(Highcharts));

  };

  var initializePlot = function(mainTitle, xAxistitle, yAxisTitle, xAxisCategories, isCrossCancer, dataSeries, secondaryAxisTitle,minY,maxY, pValues) {
    categoryNames = xAxisCategories;
    $('plots_box').html("");
    chartContainer = new Highcharts.Chart({
      chart: {
        renderTo: 'plots_box',
        plotBorderWidth: 1,
        width: 920,
        events: {
          load: function(event) {
            this.myTooltip = new Highcharts.Tooltip(this, this.options.tooltip);
            labelName = 'Median value : ';
            if (isCrossCancer) {
              labelName = 'Cutoff value : ';
              significanceValues = [];
              var cutoff = this.yAxis[0].plotLinesAndBands[0].options.value;
              $.each(this.series, function(key, val) {
                if ((val.visible == true) && (val.type == 'boxplot')) {
                  var count = 0;
                  $.each(val.linkedSeries[0].yData, function(key1, val1) {
                    if (val1 >= cutoff) count++;
                  });
                  significanceValues.push(((count / val.linkedSeries[0].yData.length) * 100).toFixed(2));
                }
              });

              var label = this.renderer.label('Click on the plot to move line', this.chartWidth - 200, this.plotTop)
                .attr({
                  fill: 'rgba(0, 0, 0, 0.75)',
                  padding: 8,
                  r: 5,
                  zIndex: 8
                })
                .css({
                  color: '#FFFFFF',
                })
                .add();

              setTimeout(function() {
                label.fadeOut();
              }, 7000);

            } else {
              significanceValues = pValues;
            }
            cutofflabel = this.renderer.label(labelName + dataSeries[0].data[0].median.toFixed(2), this.plotLeft, this.plotTop)
              .attr({
                fill: 'rgba(0, 0, 0, 0.75)',
                padding: 8,
                r: 5,
                zIndex: 8
              })
              .css({
                color: '#FFFFFF',

                fontSize: '10px',
                fontFamily: 'Verdana, sans-serif',
                fontWeight: 'bold',
                'z-index': 1
              })
              .add();
            if (this.get('significanceAxis') != null) {
              this.get('significanceAxis').remove();
            }
            this.addAxis({ // slave axis
              linkedTo: 0,
              id: 'significanceAxis',
              lineWidth: 0,
              startOnTick: false,
              //min:0,
              tickColor: 'none',
              categories: significanceValues,
              opposite: false,
              title: {
                align: 'low',
                text: secondaryAxisTitle,
                rotation: 0,
                x: -60,
                y: -25
              },
              labels: {
                align: 'center',
                autoRotation: false,
                style: {
                  width: '30px',
                  'max-width': '20px',
                  fontSize: '10px',
                  fontFamily: 'Verdana, sans-serif',
                  height: '20px',
                  textOverflow: 'ellipsis'
                }
              }
            }, true);
          }
        }
      },
	  lang:{
      	loading:""
      },
      loading: {
		hideDuration: 0,
        showDuration: 0,
	  	style: {
			backgroundImage: "url('images/ajax-loader.gif')",
			backgroundColor: 'gray',
			'backgroundRepeat': 'no-repeat',
			'backgroundPosition': 'center', 
			opacity: .8
		 } 
	  },
      credits: {
        enabled: false
      },
      title: {
        text: mainTitle
      },
      xAxis: [{
        title: {
          text: xAxistitle,
          y: 67
        },
        tickInterval: 1,
        categories: xAxisCategories,
        labels: {
          formatter: function() {
            return this.value.replace(" ", '<br />');
          },
          align: 'center',
          autoRotation: false,
          style: {
            width: '30px',
            'max-width': '20px',
            fontSize: '10px',
            fontFamily: 'Verdana, sans-serif',
            height: '20px',
            textOverflow: 'ellipsis'
          }
        },
        min: 0,
        max: 8
      }],
      yAxis: [{
        title: {
          text: yAxisTitle
        },
		min:minY,
		max:maxY,
        endOnTick: true,
        gridLineWidth: 0,
        plotLines: [{
          value: dataSeries[0].data[0].median,
          color: "#0C4F8B",
          width: 3,
          id: 'plot-line',
        }]
      }],
      legend: {
        title: {
          text: 'Normal Tissue<br/><span style="font-size: 9px; color: #666; font-weight: normal">(Click to hide)</span>',
          style: {
            fontStyle: 'italic'
          }
        },
        layout: 'vertical',
        align: 'left',
        verticalAlign: 'top',
        symbolWidth: 0,
        // itemWidth: 80,
        /*itemStyle:{
					width: '80px',
					fontSize: '10px',
					fontFamily: 'Verdana, sans-serif',
					textOverflow: 'ellipsis'
            }*/

      },
      plotOptions: {
        boxplot: {
          grouping: false,
          lineWidth: 2,
          whiskerLength: '60%',
        },
        scatter: {
          marker: {
            radius: 2.5,
            fillColor: '#0C4F8B',
            symbol: 'circle',
            states: {
              hover: {
                radiusPlus: 3,
                fillColor: '#ff1919',
                lineColor: '#e50000'
              }
            }
          },
          states: {
            hover: {
              halo: false
            }
          },
          events: {
            click: function(evt) {
              this.chart.myTooltip.refresh(evt.point, evt);
            },
            mouseOut: function() {
              this.chart.myTooltip.hide();
            }
          }
        },
        series: {
          events: {
            legendItemClick: function() {
              categoryNames = [];
              var location = this.xData;
              var tempsignificanceValues = [];
             // var startTime = new Date();
              this.chart.showLoading();
              if (this.visible) {
                this.setVisible(false, false);
                this.linkedSeries[0].setVisible(false, false);
                $.each(this.chart.series, function(key, val) {
                  if ((val.visible) && (val.type == 'boxplot')) {
                    categoryNames.push(val.name);
                    tempsignificanceValues.push(significanceValues[key]);
                    if (val.xData != location) {
                      if (parseInt(val.xData) > parseInt(location)) {
                        val.data[0].update({
                          x: parseInt(val.xData) - 1
                        }, false);

                        $.each(val.linkedSeries[0].data, function(key1, val1) {
                          val1.update({
                            x: parseFloat(val1.x) - 1
                          }, false);
                        });
                      }
                    }
                  } else if ((val.type == 'scatter')) {
                    return false;
                  }
                });

                this.chart.xAxis[0].setExtremes(0, null, false);
              } else {
                $.each(this.chart.series, function(key, val) {
                  if (((val.visible) || (val.xData == location)) && (val.type == 'boxplot')) {
                    categoryNames.push(val.name);
                    tempsignificanceValues.push(significanceValues[key]);

                    if ((val.visible) && (parseInt(val.xData) >= location)) {

                      val.data[0].update({
                        x: parseInt(val.data[0].x) + 1
                      }, false);

                      $.each(val.linkedSeries[0].data, function(key1, val1) {
                        val1.update({
                          x: parseFloat(val1.x) + 1
                        }, false);
                      });

                    }
                  } else if ((val.type == 'scatter')) {
                    return false;
                  }
                });
                this.setVisible(true, false);
              }
              this.chart.xAxis[0].setCategories(categoryNames, false);
              this.chart.xAxis[1].setCategories(tempsignificanceValues, false);
              this.chart.redraw();
              this.chart.hideLoading();
             // console.log("execution time : " + ((new Date()).getTime() - startTime));
              return false;
            }
          },
        }
      },
      tooltip: {
        useHTML: true,
        hideDelay: 1000,
        enabled: false,
        shared: false,
        borderWidth: 2,
        borderColor: '#e7f3fd',
        shadow: true,
        followTouchMove: true,
        backgroundColor: '#fafcff',
        formatter: function() {
          if (this.series.type == 'scatter') {
            var content = "<font size='2'>";
            content += "mRNA: <strong>" + parseFloat(this.point.y).toFixed(3) + "</strong><br>";
            if (this.series.userOptions.isCancer) {
              content += "Case ID: <strong><a href='tumormap.do?sample_id=" + this.point.name + "&cancer_study_id=" + this.series.userOptions.linkedTo + "' target = '_blank'>" + this.point.name + "</a></strong><br>";
            } else {
              content += "Case ID: <strong>" + this.point.name + "</strong><br>";
            }
            content = content + "</font>";
            return content;
          } else if ((this.series.type == 'boxplot') && (isCrossCancer)) {
            var cutoff = e.chart.yAxis[0].plotLinesAndBands[0].options.value;
            var count = 0;
            $.each(this.series.linkedSeries[0].yData, function(key, val) {
              if (val >= cutoff) count++;
            });
            var content = "%" + (count / this.series.linkedSeries[0].yData.length) * 100 + " significant";
            return content;
          } else return false;

        }
      },
      series: dataSeries,
      scrollbar: {
        enabled: true,
        barBackgroundColor: 'gray',
        barBorderRadius: 3,
        barBorderWidth: 0,
        buttonBackgroundColor: 'gray',
        buttonBorderWidth: 0,
        buttonArrowColor: 'yellow',
        buttonBorderRadius: 3,
        rifleColor: 'yellow',
        trackBackgroundColor: 'white',
        trackBorderWidth: 1,
        trackBorderColor: 'silver',
        trackBorderRadius: 3,

      },
      exporting: {
        filename: mainTitle,
        buttons: {
          contextButton: {
            menuItems: [{
              text: 'Export to PNG image',
              onclick: function() {
                exportTemplate(this, 'image/png');
              }
            }, {
              text: 'Export to JPEG image',
              onclick: function() {
                exportTemplate(this, 'image/jpeg');
              }
            }, {
              text: 'Export to PDF',
              onclick: function() {
                exportTemplate(this, 'application/pdf');
              }
            }, {
              text: 'Export to SVG image',
              onclick: function() {
                exportTemplate(this, 'image/svg+xml');
              }
            }]

          }
        }
      }
    });
  };

  function exportTemplate(chart, type) {
    chart.exportChart({
      sourceWidth: categoryNames.length * 100,
      type: type
    }, {
      xAxis: [{
        title: {
          text: "Tissue Type",
          y: 60
        },
        categories: categoryNames,
        min: 0,
        minRange: categoryNames.length - 1,
        max: categoryNames.length - 1
      }],
      scrollbar: {
        enabled: false
      }
    });

  }

  function applyLog(flag) {
   chartContainer.showLoading();
    if (flag) {
      $.each(chartContainer.series, function(key, val) {
        var val;
        if (val.type == 'boxplot') {
          var temp = val.data[0].options;
          temp.high = Math.log(temp.high) / Math.log(2);
          temp.low = Math.log(temp.low) / Math.log(2);
          temp.q1 = Math.log(temp.q1) / Math.log(2);
          temp.median = Math.log(temp.median) / Math.log(2);
          temp.q3 = Math.log(temp.q3) / Math.log(2);
          val.data[0].update(temp, false);
        } else {
          $.each(val.data, function(key1, val1) {
            var temp = val1.options;
            temp.y = Math.log(temp.y) / Math.log(2);
            val.data[key1].update(temp, false);
          });
        }
      });
      val = Math.log(chartContainer.yAxis[0].plotLinesAndBands[0].options.value) / Math.log(2);
	  chartContainer.yAxis[0].setTitle({ text: chartContainer.options.yAxis[0].title.text + ' (log2)' },false);
	  chartContainer.yAxis[0].setExtremes(Math.log(chartContainer.yAxis[0].getExtremes().dataMin+0.1) / Math.log(2),Math.log(chartContainer.yAxis[0].getExtremes().dataMax) / Math.log(2),false);
    } else {
      $.each(chartContainer.series, function(key, val) {
        if (val.type == 'boxplot') {
          var temp = val.data[0].options;
          temp.high = Math.pow(2, temp.high);
          temp.low = Math.pow(2, temp.low);
          temp.q1 = Math.pow(2, temp.q1);
          temp.median = Math.pow(2, temp.median);
          temp.q3 = Math.pow(2, temp.q3);
          val.data[0].update(temp, false);
        } else {
          $.each(val.data, function(key1, val1) {
            var temp = val1.options;
            temp.y = Math.pow(2, temp.y);
            val.data[key1].update(temp, false);
          });
        }
      });
      val = Math.pow(2, chartContainer.yAxis[0].plotLinesAndBands[0].options.value);
	  var yTitle = chartContainer.options.yAxis[0].title.text;
	  yTitle = yTitle.substring(0,yTitle.indexOf(" (log2)"));
       chartContainer.yAxis[0].setTitle({ text: yTitle},false);
	   chartContainer.yAxis[0].setExtremes(Math.pow(2, chartContainer.yAxis[0].getExtremes().dataMin),Math.pow(2, chartContainer.yAxis[0].getExtremes().dataMax),false);
    }
    addPlotLine(val);
    chartContainer.redraw();
	chartContainer.hideLoading();
  };

  function thresholdChange(thresholdValue) {
   chartContainer.showLoading();
    //var startTime = (new Date()).getTime();
    $.each(chartContainer.series, function(key, val) {
      if (val.type == 'boxplot') {
        if (!val.userOptions.isCancer) {
          var temp = val.data[0].options;
          if (parseFloat(temp.pValue) <= parseFloat(thresholdValue)) {
            temp.color = "#F80000";
          } else {
            temp.color = '#AAAAAA';
          }
          val.update(temp, false);
        }

      } else {
        return false;
      }
    });
    chartContainer.redraw();
	chartContainer.hideLoading();
   // console.log("execution time : " + ((new Date()).getTime() - startTime));
  }

  return {
    initializePlot: initializePlot,
    addCrossCancerEvents: addCrossCancerEvents,
    getChartContainer: function() {
      return chartContainer;
    },
    applyLog: applyLog,
    thresholdChange: thresholdChange
  };

}());
